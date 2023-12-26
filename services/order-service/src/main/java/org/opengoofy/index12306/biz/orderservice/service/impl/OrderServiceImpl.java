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

package org.opengoofy.index12306.biz.orderservice.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.StrBuilder;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.opengoofy.index12306.biz.orderservice.common.enums.OrderCanalErrorCodeEnum;
import org.opengoofy.index12306.biz.orderservice.common.enums.OrderItemStatusEnum;
import org.opengoofy.index12306.biz.orderservice.common.enums.OrderStatusEnum;
import org.opengoofy.index12306.biz.orderservice.dao.entity.OrderDO;
import org.opengoofy.index12306.biz.orderservice.dao.entity.OrderItemDO;
import org.opengoofy.index12306.biz.orderservice.dao.entity.OrderItemPassengerDO;
import org.opengoofy.index12306.biz.orderservice.dao.mapper.OrderItemMapper;
import org.opengoofy.index12306.biz.orderservice.dao.mapper.OrderMapper;
import org.opengoofy.index12306.biz.orderservice.dto.domain.OrderStatusReversalDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.CancelTicketOrderReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderCreateReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderItemCreateReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderPageQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderSelfPageQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailSelfRespDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.mq.event.DelayCloseOrderEvent;
import org.opengoofy.index12306.biz.orderservice.mq.event.PayResultCallbackOrderEvent;
import org.opengoofy.index12306.biz.orderservice.mq.produce.DelayCloseOrderSendProduce;
import org.opengoofy.index12306.biz.orderservice.remote.UserRemoteService;
import org.opengoofy.index12306.biz.orderservice.remote.dto.UserQueryActualRespDTO;
import org.opengoofy.index12306.biz.orderservice.service.OrderItemService;
import org.opengoofy.index12306.biz.orderservice.service.OrderPassengerRelationService;
import org.opengoofy.index12306.biz.orderservice.service.OrderService;
import org.opengoofy.index12306.biz.orderservice.service.orderid.OrderIdGeneratorManager;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.framework.starter.convention.page.PageResponse;
import org.opengoofy.index12306.framework.starter.convention.result.Result;
import org.opengoofy.index12306.framework.starter.database.toolkit.PageUtil;
import org.opengoofy.index12306.frameworks.starter.user.core.UserContext;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description 订单服务接口层实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemService orderItemService;
    private final OrderPassengerRelationService orderPassengerRelationService;
    private final RedissonClient redissonClient;
    private final DelayCloseOrderSendProduce delayCloseOrderSendProduce;
    private final UserRemoteService userRemoteService;

    @Override
    public TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn) {
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderSn, orderSn);
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        TicketOrderDetailRespDTO result = BeanUtil.convert(orderDO, TicketOrderDetailRespDTO.class);
        LambdaQueryWrapper<OrderItemDO> orderItemQueryWrapper = Wrappers.lambdaQuery(OrderItemDO.class)
                .eq(OrderItemDO::getOrderSn, orderSn);
        List<OrderItemDO> orderItemDOList = orderItemMapper.selectList(orderItemQueryWrapper);
        result.setPassengerDetails(BeanUtil.convert(orderItemDOList, TicketOrderPassengerDetailRespDTO.class));
        return result;
    }

    @Override
    public PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getUserId, requestParam.getUserId())
                .in(OrderDO::getStatus, buildOrderStatusList(requestParam))
                .orderByDesc(OrderDO::getOrderTime);
        IPage<OrderDO> orderPage = orderMapper.selectPage(PageUtil.convert(requestParam), queryWrapper);
        return PageUtil.convert(orderPage, each -> {
            TicketOrderDetailRespDTO result = BeanUtil.convert(each, TicketOrderDetailRespDTO.class);
            LambdaQueryWrapper<OrderItemDO> orderItemQueryWrapper = Wrappers.lambdaQuery(OrderItemDO.class)
                    .eq(OrderItemDO::getOrderSn, each.getOrderSn());
            List<OrderItemDO> orderItemDOList = orderItemMapper.selectList(orderItemQueryWrapper);
            result.setPassengerDetails(BeanUtil.convert(orderItemDOList, TicketOrderPassengerDetailRespDTO.class));
            return result;
        });
    }


    /*
     * 假设一家人出去玩，你在自己账号通过添加乘车人购买了车票，注意这个时候家里人还没有注册 12306。
     * 购买票后，家里人注册了 12306 账号，这个时候要通过查看本人车票能看到出行的车票数据记录。
     * 回顾一下，咱们订单和订单明细表是按照用户 ID 后六位进行分库分表的，如果是购票用户查看自己的本人车票，通过用户 ID 就能查询。但是，购票时乘车人可能是没有账号的，这个怎么解决？
     *
     * 因为乘车人数据中唯一能起到标识作用的也就是证件号，而注册用户证件号也是必须的，通过证件号进行数据关联。如果要实现这个需求，只能在订单表中按照证件号和订单进行关联。
     * 为了满足查看本人车票订单的功能，可以通过添加路由表t_order_item_passenger的方式完成这个查询。路由表中通过证件号绑定订单号，再关联订单表和订单明细表，就能查看到乘车人购票的本人车票了。
     */

    /*
     * 用户发起订单后，如果长时间未支付，则需要将订单关闭 --> 延时关闭订单 在本项目中采用RocketMQ作为延时关闭订单的技术实现，有关于延时关闭订单的技术选型主要有以下几种：
     * 1.定时任务：定时任务的是一种常见的订单延迟关闭解决方案。可以同构调度平台来实现定时任务的执行，具体任务是根据订单创建时间扫描所有到期的订单，并执行关闭订单的操作。
     *   这种方案的优点在于易于实现，但是定时任务方案会存在以下几个问题：
         (1)延迟时间不精确：使用定时任务执行订单关闭逻辑，无法保证订单在十分钟后准确地关闭。如果任务执行器在关闭订单的具体时间点出现问题，可能导致订单关闭的时间延后。
         (2)不适合高并发场景：定时任务执行的频率通常是固定的，无法根据实际订单的情况来灵活调整。在高并发场景下，可能导致大量的定时任务同时执行，造成系统负载过大。
         (3)分库分表问题：拿 12306 来说，订单表按照用户标识和订单号进行了分库分表，那这样的话，和上面说的根据订单创建时间去扫描一批订单进行关闭，自然就行不通。因为根据创建时间查询没有携带分片键，存在读扩散问题。
     * 2.RabbitMQ：通过使用 RabbitMQ 的延时消息特性，我们可以轻松实现订单十分钟延时关闭功能。首先，我们需要在 RabbitMQ 服务器上启用延时特性，通常通过安装 rabbitmq_delayed_message_exchange 插件来支持延时消息功能。
     *   然后，我们创建两个队列：订单队列和死信队列。订单队列用于存储需要延时关闭的订单消息，而死信队列则用于存储延时时间到达后的订单消息。在创建订单队列时，我们要为队列配置延时特性，指定订单消息的延时时间，比如十分钟。这样，当有新的订单需要延时关闭时，我们只需要将订单消息发送到订单队列，并设置消息的延时时间。
         接下来，在订单队列中设置死信交换机和死信队列，当订单消息的延时时间到达后，消息会自动转发到死信队列，从而触发关闭订单的操作。在死信队列中，我们可以监听消息，并执行关闭订单的逻辑。为了确保消息的可靠性，可以在关闭订单操作前添加适当的幂等性措施，这样即使消息重复处理，也不会对系统产生影响。
         通过以上步骤，我们就成功实现了订单的十分钟延时关闭功能。当有新的订单需要延时关闭时，将订单消息发送到订单队列，并设置延时时间。在延时时间到达后，订单消息会自动进入死信队列，从而触发关闭订单的操作。这种方式既简单又可靠，保证了系统的稳定性和可用性。
     *   从整体来说 RabbitMQ 实现延时关闭订单功能是比较合适的，但也存在几个问题：
     *   (1) 延时精度：RabbitMQ 的延时消息特性是基于消息的 TTL（Time-To-Live）来实现的，因此消息的延时时间并不是完全准确的，可能会有一定的误差。在处理订单十分钟延时关闭时，可能会有一些订单的关闭时间略晚于预期时间。
         (2) 高并发问题：如果系统中有大量的订单需要延时关闭，而订单关闭操作非常复杂耗时，可能会导致消息队列中的消息堆积。这样就可能导致延时关闭操作无法及时处理，影响订单的实际关闭时间。
         (3) 重复消息问题：由于网络原因或其他不可预知的因素，可能会导致消息重复发送到订单队列。如果没有处理好消息的幂等性，可能会导致订单重复关闭的问题，从而造成数据不一致或其他异常情况。
         (4) 可靠性问题：RabbitMQ 是一个消息中间件，它是一个独立的系统。如果 RabbitMQ 本身出现故障或宕机，可能会导致订单延时关闭功能失效。因此，在使用 RabbitMQ 实现延时关闭功能时，需要考虑如何保证 RabbitMQ 的高可用性和稳定性。
     * 3.Redis过期监听：可以借助 Redis 的过期消息监听机制实现延时关闭功能。
     *   首先，在订单创建时，将订单信息存储到 Redis，并设置过期时间为十分钟。同时，在 Redis 中存储一个过期消息监听的键值对，键为订单号，值为待处理订单的标识。
         其次，编写一个消息监听器，持续监听 Redis 的过期事件。监听器使用 Redis 的 PSUBSCRIBE 命令订阅过期事件，并在监听到过期事件时触发相应的处理逻辑。当订单过期时间到达时，Redis 会自动触发过期事件，消息监听器捕获到该事件，并获取到过期的订单号。
         接着，监听器执行订单关闭的逻辑，如更新订单状态为关闭状态，释放相关资源等，实现订单的十分钟延时关闭功能。
         需要注意的是，消息监听器应该是一个长期运行的任务，确保持续监听 Redis 的过期事件。为了保证系统的稳定性和可靠性，可以在实现订单关闭逻辑时添加容错机制，以应对 Redis 可能发生故障或重启的情况。
     *   Redis 过期消息也存在几个问题：
     *   (1) 不够精确：Redis 的过期时间是通过定时器实现的，可能存在一定的误差，导致订单的关闭时间不是精确的十分钟。这对于某些对时间要求较高的场景可能不适用。
         (2) Redis 宕机：如果 Redis 宕机或重启，那些已经设置了过期时间但还未过期的订单信息将会丢失，导致这部分订单无法正确关闭。需要考虑如何处理这种异常情况。
         (3) 可靠性：依赖 Redis 的过期时间来实现订单关闭功能，需要确保 Redis 的高可用性和稳定性。如果 Redis 发生故障或网络问题，可能导致订单关闭功能失效。
         (4) 版本问题：Redis 5.0 之前是不保证延迟消息持久化的，如果客户端消费过程中宕机或者重启，这个消息不会重复投递。5.0 之后推出了 Stream 功能，有了持久化等比较完善的延迟消息功能。
     * 4.Redisson：通过 Redisson 的 RDelayedQueue 功能可以实现订单十分钟延时关闭的功能。
         首先，我们需要创建一个 RDelayedQueue 对象，用于存放需要延时关闭的订单信息。当用户创建订单时，我们将订单信息添加到 RDelayedQueue 中，并设置订单的延时时间为十分钟。Redisson 提供了监听功能，可以实现对 RDelayedQueue 中订单信息的监听。\
         一旦订单到达设定的延时时间，Redisson 会触发监听事件。在监听到订单的延时事件后，我们可以编写相应的处理逻辑，即关闭对应的订单。在处理订单关闭时，我们可以根据订单号或订单创建时间等信息，来找到对应的订单进行关闭操作。
         不过这种方式也不推荐使用，基本上 Redis 过期监听消息存在的问题，RDelayedQueue 也都会有，因为 RDelayedQueue 本质上也是依赖 Redis 实现。
     * 5.RocketMQ
     *   在订单生成时，我们将订单关闭消息发送到 RocketMQ，并设置消息的延迟时间为十分钟。RocketMQ 支持设置消息的延迟时间，可以通过设置消息的 delayLevel 来指定延迟级别，每个级别对应一种延迟时间。这样，订单关闭消息将在十分钟后自动被消费者接收到。
         需要注意，RocketMQ 5.0 之后已经支持了自定义时间的延迟，而不仅是延迟级别范围内的时间。为了处理订单关闭消息，我们需要在消费者端创建一个消息监听器。当消息监听器接收到订单关闭消息时，触发订单关闭操作，将订单状态设置为关闭状态。
         需要注意的是，RocketMQ 的消息传递机制保证了消息的可靠性传递，因此消息可能会进行多次重试。为了确保订单关闭操作的幂等性，即多次执行不会产生副作用，我们需要在订单关闭逻辑中进行幂等性的处理。
     */

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createTicketOrder(TicketOrderCreateReqDTO requestParam) {
        // 通过基因法将用户 ID 融入到订单号
        String orderSn = OrderIdGeneratorManager.generateId(requestParam.getUserId());
        // 订单相关记录需要插入数据库中，有三类订单数据，分别是：主订单表、订单明细表以及订单乘车人明细表
        // 主订单表
        OrderDO orderDO = OrderDO.builder().orderSn(orderSn)
                .orderTime(requestParam.getOrderTime())
                .departure(requestParam.getDeparture())
                .departureTime(requestParam.getDepartureTime())
                .ridingDate(requestParam.getRidingDate())
                .arrivalTime(requestParam.getArrivalTime())
                .trainNumber(requestParam.getTrainNumber())
                .arrival(requestParam.getArrival())
                .trainId(requestParam.getTrainId())
                .source(requestParam.getSource())
                .status(OrderStatusEnum.PENDING_PAYMENT.getStatus())
                .username(requestParam.getUsername())
                .userId(String.valueOf(requestParam.getUserId()))
                .build();
        orderMapper.insert(orderDO);
        // 订单明细表以及订单乘车人明细表
        List<TicketOrderItemCreateReqDTO> ticketOrderItems = requestParam.getTicketOrderItems();
        List<OrderItemDO> orderItemDOList = new ArrayList<>();
        List<OrderItemPassengerDO> orderPassengerRelationDOList = new ArrayList<>();
        ticketOrderItems.forEach(each -> {
            OrderItemDO orderItemDO = OrderItemDO.builder()
                    .trainId(requestParam.getTrainId())
                    .seatNumber(each.getSeatNumber())
                    .carriageNumber(each.getCarriageNumber())
                    .realName(each.getRealName())
                    .orderSn(orderSn)
                    .phone(each.getPhone())
                    .seatType(each.getSeatType())
                    .username(requestParam.getUsername()).amount(each.getAmount()).carriageNumber(each.getCarriageNumber())
                    .idCard(each.getIdCard())
                    .ticketType(each.getTicketType())
                    .idType(each.getIdType())
                    .userId(String.valueOf(requestParam.getUserId()))
                    .status(0)
                    .build();
            orderItemDOList.add(orderItemDO);
            OrderItemPassengerDO orderPassengerRelationDO = OrderItemPassengerDO.builder()
                    .idType(each.getIdType())
                    .idCard(each.getIdCard())
                    .orderSn(orderSn)
                    .build();
            orderPassengerRelationDOList.add(orderPassengerRelationDO);
        });
        orderItemService.saveBatch(orderItemDOList);
        orderPassengerRelationService.saveBatch(orderPassengerRelationDOList);

        try {
            // 发送 RocketMQ 延时消息，指定时间后取消订单
            DelayCloseOrderEvent delayCloseOrderEvent = DelayCloseOrderEvent.builder()
                    .trainId(String.valueOf(requestParam.getTrainId()))
                    .departure(requestParam.getDeparture())
                    .arrival(requestParam.getArrival())
                    .orderSn(orderSn)
                    .trainPurchaseTicketResults(requestParam.getTicketOrderItems())
                    .build();
            // 创建订单并支付后延时关闭订单消息怎么办？详情查看：https://nageoffer.com/12306/question
            SendResult sendResult = delayCloseOrderSendProduce.sendMessage(delayCloseOrderEvent);
            if (!Objects.equals(sendResult.getSendStatus(), SendStatus.SEND_OK)) {
                throw new ServiceException("投递延迟关闭订单消息队列失败");
            }
        } catch (Throwable ex) {
            log.error("延迟关闭订单消息队列发送错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }
        return orderSn;
    }

    @Override
    public boolean closeTickOrder(CancelTicketOrderReqDTO requestParam) {
        String orderSn = requestParam.getOrderSn();
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderSn, orderSn)
                .select(OrderDO::getStatus);
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        if (Objects.isNull(orderDO) || orderDO.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            return false;
        }
        // 原则上订单关闭和订单取消这两个方法可以复用，为了区分未来考虑到的场景，这里对方法进行拆分但复用逻辑
        return cancelTickOrder(requestParam);
    }

    @Override
    public boolean cancelTickOrder(CancelTicketOrderReqDTO requestParam) {
        String orderSn = requestParam.getOrderSn();
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderSn, orderSn);
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        if (orderDO == null) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_UNKNOWN_ERROR);
        } else if (orderDO.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_STATUS_ERROR);
        }
        RLock lock = redissonClient.getLock(StrBuilder.create("order:canal:order_sn_").append(orderSn).toString());
        if (!lock.tryLock()) {
            throw new ClientException(OrderCanalErrorCodeEnum.ORDER_CANAL_REPETITION_ERROR);
        }
        try {
            OrderDO updateOrderDO = new OrderDO();
            updateOrderDO.setStatus(OrderStatusEnum.CLOSED.getStatus());
            updateOrderDO.setOrderSn(orderSn);
            LambdaUpdateWrapper<OrderDO> updateWrapper = Wrappers.lambdaUpdate(OrderDO.class)
                    .eq(OrderDO::getOrderSn, orderSn);
            int updateResult = orderMapper.update(updateOrderDO, updateWrapper);
            if (updateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_ERROR);
            }
            OrderItemDO updateOrderItemDO = new OrderItemDO();
            updateOrderItemDO.setStatus(OrderItemStatusEnum.CLOSED.getStatus());
            updateOrderItemDO.setOrderSn(orderSn);
            LambdaUpdateWrapper<OrderItemDO> updateItemWrapper = Wrappers.lambdaUpdate(OrderItemDO.class)
                    .eq(OrderItemDO::getOrderSn, orderSn);
            int updateItemResult = orderItemMapper.update(updateOrderItemDO, updateItemWrapper);
            if (updateItemResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_ERROR);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public void statusReversal(OrderStatusReversalDTO requestParam) {
        LambdaQueryWrapper<OrderDO> queryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                .eq(OrderDO::getOrderSn, requestParam.getOrderSn());
        OrderDO orderDO = orderMapper.selectOne(queryWrapper);
        if (orderDO == null) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_UNKNOWN_ERROR);
        } else if (orderDO.getStatus() != OrderStatusEnum.PENDING_PAYMENT.getStatus()) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_CANAL_STATUS_ERROR);
        }
        RLock lock = redissonClient.getLock(StrBuilder.create("order:status-reversal:order_sn_").append(requestParam.getOrderSn()).toString());
        if (!lock.tryLock()) {
            log.warn("订单重复修改状态，状态反转请求参数：{}", JSON.toJSONString(requestParam));
        }
        try {
            OrderDO updateOrderDO = new OrderDO();
            updateOrderDO.setStatus(requestParam.getOrderStatus());
            LambdaUpdateWrapper<OrderDO> updateWrapper = Wrappers.lambdaUpdate(OrderDO.class)
                    .eq(OrderDO::getOrderSn, requestParam.getOrderSn());
            int updateResult = orderMapper.update(updateOrderDO, updateWrapper);
            if (updateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_STATUS_REVERSAL_ERROR);
            }
            OrderItemDO orderItemDO = new OrderItemDO();
            orderItemDO.setStatus(requestParam.getOrderItemStatus());
            LambdaUpdateWrapper<OrderItemDO> orderItemUpdateWrapper = Wrappers.lambdaUpdate(OrderItemDO.class)
                    .eq(OrderItemDO::getOrderSn, requestParam.getOrderSn());
            int orderItemUpdateResult = orderItemMapper.update(orderItemDO, orderItemUpdateWrapper);
            if (orderItemUpdateResult <= 0) {
                throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_STATUS_REVERSAL_ERROR);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void payCallbackOrder(PayResultCallbackOrderEvent requestParam) {
        OrderDO updateOrderDO = new OrderDO();
        updateOrderDO.setPayTime(requestParam.getGmtPayment());
        updateOrderDO.setPayType(requestParam.getChannel());
        LambdaUpdateWrapper<OrderDO> updateWrapper = Wrappers.lambdaUpdate(OrderDO.class)
                .eq(OrderDO::getOrderSn, requestParam.getOrderSn());
        int updateResult = orderMapper.update(updateOrderDO, updateWrapper);
        if (updateResult <= 0) {
            throw new ServiceException(OrderCanalErrorCodeEnum.ORDER_STATUS_REVERSAL_ERROR);
        }
    }

    @Override
    public PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam) {
        Result<UserQueryActualRespDTO> userActualResp = userRemoteService.queryActualUserByUsername(UserContext.getUsername());
        LambdaQueryWrapper<OrderItemPassengerDO> queryWrapper = Wrappers.lambdaQuery(OrderItemPassengerDO.class)
                .eq(OrderItemPassengerDO::getIdCard, userActualResp.getData().getIdCard())
                .orderByDesc(OrderItemPassengerDO::getCreateTime);
        IPage<OrderItemPassengerDO> orderItemPassengerPage = orderPassengerRelationService.page(PageUtil.convert(requestParam), queryWrapper);
        return PageUtil.convert(orderItemPassengerPage, each -> {
            LambdaQueryWrapper<OrderDO> orderQueryWrapper = Wrappers.lambdaQuery(OrderDO.class)
                    .eq(OrderDO::getOrderSn, each.getOrderSn());
            OrderDO orderDO = orderMapper.selectOne(orderQueryWrapper);
            LambdaQueryWrapper<OrderItemDO> orderItemQueryWrapper = Wrappers.lambdaQuery(OrderItemDO.class)
                    .eq(OrderItemDO::getOrderSn, each.getOrderSn())
                    .eq(OrderItemDO::getIdCard, each.getIdCard());
            OrderItemDO orderItemDO = orderItemMapper.selectOne(orderItemQueryWrapper);
            TicketOrderDetailSelfRespDTO actualResult = BeanUtil.convert(orderDO, TicketOrderDetailSelfRespDTO.class);
            BeanUtil.convertIgnoreNullAndBlank(orderItemDO, actualResult);
            return actualResult;
        });
    }

    private List<Integer> buildOrderStatusList(TicketOrderPageQueryReqDTO requestParam) {
        List<Integer> result = new ArrayList<>();
        switch (requestParam.getStatusType()) {
            case 0 -> result = ListUtil.of(
                    OrderStatusEnum.PENDING_PAYMENT.getStatus()
            );
            case 1 -> result = ListUtil.of(
                    OrderStatusEnum.ALREADY_PAID.getStatus(),
                    OrderStatusEnum.PARTIAL_REFUND.getStatus(),
                    OrderStatusEnum.FULL_REFUND.getStatus()
            );
            case 2 -> result = ListUtil.of(
                    OrderStatusEnum.COMPLETED.getStatus()
            );
        }
        return result;
    }
}
