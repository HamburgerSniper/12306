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

package org.opengoofy.index12306.biz.ticketservice.canal;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.ticketservice.common.enums.CanalExecuteStrategyMarkEnum;
import org.opengoofy.index12306.biz.ticketservice.common.enums.SeatStatusEnum;
import org.opengoofy.index12306.biz.ticketservice.mq.event.CanalBinlogEvent;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.opengoofy.index12306.framework.starter.designpattern.strategy.AbstractExecuteStrategy;
import org.opengoofy.index12306.framework.starter.log.annotation.FinishStudy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.opengoofy.index12306.biz.ticketservice.common.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;
import static org.opengoofy.index12306.framework.starter.log.annotation.FinishStudy.FinishStudyEnum.TRUE;

/**
 * @description 列车余票缓存更新组件
 */
@Component
@RequiredArgsConstructor
@FinishStudy(status = TRUE)
public class TicketAvailabilityCacheUpdateHandler implements AbstractExecuteStrategy<CanalBinlogEvent, Void> {

    private final DistributedCache distributedCache;

    @FinishStudy(status = TRUE)
    @Override
    public void execute(CanalBinlogEvent message) {
        List<Map<String, Object>> messageDataList = new ArrayList<>();
        List<Map<String, Object>> actualOldDataList = new ArrayList<>();
        // 1.数据过滤：坚挺了t_seat表的binlog数据变化，但实际只关注对t_seat表中seat_status字段的更新
        // 例如由 可售状态0 转变为 锁定状态1 只有这种数据变化才需要更新相应的余票缓存，其他字段变化不出发后续逻辑
        for (int i = 0; i < message.getOld().size(); i++) {
            Map<String, Object> oldDataMap = message.getOld().get(i);
            if (oldDataMap.get("seat_status") != null && StrUtil.isNotBlank(oldDataMap.get("seat_status").toString())) {
                Map<String, Object> currentDataMap = message.getData().get(i);
                if (StrUtil.equalsAny(currentDataMap.get("seat_status").toString(), String.valueOf(SeatStatusEnum.AVAILABLE.getCode()), String.valueOf(SeatStatusEnum.LOCKED.getCode()))) {
                    actualOldDataList.add(oldDataMap);
                    messageDataList.add(currentDataMap);
                }
            }
        }
        if (CollUtil.isEmpty(messageDataList) || CollUtil.isEmpty(actualOldDataList)) {
            return;
        }

        // 2. 更新列车站余票缓存逻辑
        // 检测座位状态是否在 0（可售状态）和 1（锁定状态）之间进行变化。当从 0 到 1 变化时，表示生成了订单需要减少库存。若由 1 变为 0，则表示订单被取消或超时关闭，需要增加列车库存。
        // 由于 Binlog 可能一次触发多个数据变更，我们将这些操作整理至一个 Map，以进行统一操作。为方便逻辑查阅，目前在代码中对缓存进行增减。如追求最大性能，应将所有操作放入 LUA 脚本中，或通过管道命令执行，以最大化性能表现
        Map<String, Map<Integer, Integer>> cacheChangeKeyMap = new HashMap<>();
        for (int i = 0; i < messageDataList.size(); i++) {
            Map<String, Object> each = messageDataList.get(i);
            Map<String, Object> actualOldData = actualOldDataList.get(i);
            String seatStatus = actualOldData.get("seat_status").toString();
            int increment = Objects.equals(seatStatus, "0") ? -1 : 1;
            String trainId = each.get("train_id").toString();
            String hashCacheKey = TRAIN_STATION_REMAINING_TICKET + trainId + "_" + each.get("start_station") + "_" + each.get("end_station");
            Map<Integer, Integer> seatTypeMap = cacheChangeKeyMap.get(hashCacheKey);
            if (CollUtil.isEmpty(seatTypeMap)) {
                seatTypeMap = new HashMap<>();
            }
            Integer seatType = Integer.parseInt(each.get("seat_type").toString());
            Integer num = seatTypeMap.get(seatType);
            seatTypeMap.put(seatType, num == null ? increment : num + increment);
            cacheChangeKeyMap.put(hashCacheKey, seatTypeMap);
        }
        StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
        cacheChangeKeyMap.forEach((cacheKey, cacheVal) -> cacheVal.forEach((seatType, num) -> instance.opsForHash().increment(cacheKey, String.valueOf(seatType), num)));
    }

    @FinishStudy(status = TRUE)
    @Override
    public String mark() {
        return CanalExecuteStrategyMarkEnum.T_SEAT.getActualTable();
    }
}
