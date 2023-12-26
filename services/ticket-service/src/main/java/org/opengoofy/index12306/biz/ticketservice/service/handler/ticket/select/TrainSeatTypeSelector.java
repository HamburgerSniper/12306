/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.select;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.ticketservice.common.enums.VehicleSeatTypeEnum;
import org.opengoofy.index12306.biz.ticketservice.common.enums.VehicleTypeEnum;
import org.opengoofy.index12306.biz.ticketservice.dao.entity.TrainStationPriceDO;
import org.opengoofy.index12306.biz.ticketservice.dao.mapper.TrainStationPriceMapper;
import org.opengoofy.index12306.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import org.opengoofy.index12306.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import org.opengoofy.index12306.biz.ticketservice.remote.UserRemoteService;
import org.opengoofy.index12306.biz.ticketservice.remote.dto.PassengerRespDTO;
import org.opengoofy.index12306.biz.ticketservice.service.SeatService;
import org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.dto.SelectSeatDTO;
import org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import org.opengoofy.index12306.framework.starter.convention.exception.RemoteException;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.framework.starter.convention.result.Result;
import org.opengoofy.index12306.framework.starter.designpattern.strategy.AbstractStrategyChoose;
import org.opengoofy.index12306.frameworks.starter.user.core.UserContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @description 购票时列车座位选择器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class TrainSeatTypeSelector {

    private final SeatService seatService;
    private final UserRemoteService userRemoteService;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final AbstractStrategyChoose abstractStrategyChoose;
    private final ThreadPoolExecutor selectSeatThreadPoolExecutor;

    /**
     * @param trainType    火车类型 高铁/动车/普通车 {@link VehicleTypeEnum}
     * @param requestParam 购票请求
     * @return 购票信息 包括具体的座位信息/优惠类型/座位票价
     * @description
     * @description 座位分配背景 :
     * @description 当一家四口人出去玩在 12306 进行购票时，假定四个人都选择了商务座，你是想一家人被安排到各个不同的车厢还是在一个车厢不同座位还是一个车厢相邻座位？肯定是后者最好，但实现这个座位分配算法并不容易。
     * @description 为了能更专注基本的座位分配算法，以下流程不包含购票人数大于等于三人（大于等于三人就要进行拆座位）以及在线选座等流程。
     * @description 给定如下的座位分配条件：
     * @description (1)如果购票人数为两人，购买同一车厢，座位优先检索两人相邻座位并排分配。
     * @description (2)假设当前正在检索的车厢不满足两人并排，就执行搜索全部满足两人并排的车厢。
     * @description (3)如果搜索了所有车厢还是没有两人并排做的座位，那么执行同车厢不相邻座位。
     * @description (4)如果所有车厢都是仅有一个座位，就开始执行最后降级操作，不同车厢分配。
     */
    public List<TrainPurchaseTicketRespDTO> select(Integer trainType, PurchaseTicketReqDTO requestParam) {
        // 获取本次购票请求中的乘车人信息 包括乘车人ID以及乘车人选择的座位类型
        List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
        // 根据乘车人中选择的座位类型的不同，对请求中的乘车人进行分组
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = passengerDetails.stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        // 购票选座的票务信息 采用CopyOnWriteArrayList实现的原因是需要保证并发环境下 每个座只被分配给一个人
        List<TrainPurchaseTicketRespDTO> actualResult = new CopyOnWriteArrayList<>();
        if (seatTypeMap.size() > 1) {
            // 这里是指在一个购票请求中，乘车人购买了不同等级的座位
            List<Future<List<TrainPurchaseTicketRespDTO>>> futureResults = new ArrayList<>();
            // 在这一个购票请求中，不同乘车人购买了不同等级的座位，而不同等级的座位分配算法完成时间可能不同，因此采用Future等待全部返回结果
            seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
                // 线程池参数如何设置？详情查看：https://nageoffer.com/12306/question
                Future<List<TrainPurchaseTicketRespDTO>> completableFuture =
                        selectSeatThreadPoolExecutor.submit(
                                // 异步流程获取 提交任务 等待抽象策略模式实现的座位分配算法返回值
                                () -> distributeSeats(trainType, seatType, requestParam, passengerSeatDetails)
                        );
                // 将返回值加入FutureTask结果集中
                futureResults.add(completableFuture);
            });
            // 并行流极端情况下有坑，详情参考：https://nageoffer.com/12306/question
            futureResults.parallelStream().forEach(completableFuture -> {
                try {
                    actualResult.addAll(completableFuture.get());
                } catch (Exception e) {
                    throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
                }
            });
        } else {
            // 在这次购票请求中，所有乘车人购买的都是同一等级的座位
            seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
                // 由于所有乘车人购买的都是同一等级的座位，因此不需要使用Future，只需要等待执行完即可
                List<TrainPurchaseTicketRespDTO> aggregationResult = distributeSeats(trainType, seatType, requestParam, passengerSeatDetails);
                actualResult.addAll(aggregationResult);
            });
        }

        // 没分配到座位或实际分配的座位数与乘车人数量不符
        if (CollUtil.isEmpty(actualResult) || !Objects.equals(actualResult.size(), passengerDetails.size())) {
            throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
        }

        List<String> passengerIds = actualResult.stream()
                .map(TrainPurchaseTicketRespDTO::getPassengerId)
                .collect(Collectors.toList());
        Result<List<PassengerRespDTO>> passengerRemoteResult;
        List<PassengerRespDTO> passengerRemoteResultList;
        try {
            // 查询该订票用户在这次座位分配请求中，所圈选的乘车人的详细信息
            passengerRemoteResult = userRemoteService.listPassengerQueryByIds(UserContext.getUsername(), passengerIds);
            if (!passengerRemoteResult.isSuccess() || CollUtil.isEmpty(passengerRemoteResultList = passengerRemoteResult.getData())) {
                throw new RemoteException("用户服务远程调用查询乘车人相信信息错误");
            }
        } catch (Throwable ex) {
            if (ex instanceof RemoteException) {
                log.error("用户服务远程调用查询乘车人相信信息错误，当前用户：{}，请求参数：{}", UserContext.getUsername(), passengerIds);
            } else {
                log.error("用户服务远程调用查询乘车人相信信息错误，当前用户：{}，请求参数：{}", UserContext.getUsername(), passengerIds, ex);
            }
            throw ex;
        }
        actualResult.forEach(each -> {
            String passengerId = each.getPassengerId();
            // 乘车人信息 写入
            passengerRemoteResultList.stream()
                    .filter(item -> Objects.equals(item.getId(), passengerId))
                    .findFirst()
                    .ifPresent(passenger -> {
                        each.setIdCard(passenger.getIdCard());
                        each.setPhone(passenger.getPhone());
                        each.setUserType(passenger.getDiscountType());
                        each.setIdType(passenger.getIdType());
                        each.setRealName(passenger.getRealName());
                    });

            LambdaQueryWrapper<TrainStationPriceDO> lambdaQueryWrapper = Wrappers.lambdaQuery(TrainStationPriceDO.class)
                    .eq(TrainStationPriceDO::getTrainId, requestParam.getTrainId())
                    .eq(TrainStationPriceDO::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationPriceDO::getArrival, requestParam.getArrival())
                    .eq(TrainStationPriceDO::getSeatType, each.getSeatType())
                    .select(TrainStationPriceDO::getPrice);
            TrainStationPriceDO trainStationPriceDO = trainStationPriceMapper.selectOne(lambdaQueryWrapper);
            // TODO: 2023/12/9 不同类型的passenger，对其优惠类型进行折扣
            each.setAmount(trainStationPriceDO.getPrice());
        });
        // 购买列车中间站点余票如何更新？详细查看：https://nageoffer.com/12306/question
        // 锁定沿途车票状态
        seatService.lockSeat(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival(), actualResult);
        // 返回座位分配结果
        return actualResult;
    }

    /**
     * @param trainType            火车等级 高铁/动车/普通车/汽车/飞机 {@link VehicleTypeEnum}
     * @param seatType             座位等级 商务/一等/二等 {@link VehicleSeatTypeEnum}
     * @param requestParam         购票请求
     * @param passengerSeatDetails 座位对应的乘车人集合
     * @return 针对requestParam中passenger，为其分配到的座位信息
     */
    private List<TrainPurchaseTicketRespDTO> distributeSeats(Integer trainType, Integer seatType, PurchaseTicketReqDTO requestParam, List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails) {
        // 构建策略标识 trainType + seatType
        String buildStrategyKey = VehicleTypeEnum.findNameByCode(trainType) + VehicleSeatTypeEnum.findNameByCode(seatType);
        // 分配座位 信息实体类
        SelectSeatDTO selectSeatDTO = SelectSeatDTO.builder()
                .seatType(seatType)
                .passengerSeatDetails(passengerSeatDetails)
                .requestParam(requestParam)
                .build();
        try {
            // 抽象策略模式 通过策略标识以及信息实体类，完成座位的分配
            return abstractStrategyChoose.chooseAndExecuteResp(buildStrategyKey, selectSeatDTO);
        } catch (ServiceException ex) {
            // TODO: 2023/12/9 请购买 --> 推荐系统 车次
            throw new ServiceException("当前车次列车类型暂未适配，请购买G35或G39车次");
        }
    }
}
