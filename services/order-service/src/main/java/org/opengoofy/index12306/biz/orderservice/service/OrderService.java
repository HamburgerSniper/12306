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

package org.opengoofy.index12306.biz.orderservice.service;

import org.opengoofy.index12306.biz.orderservice.dto.domain.OrderStatusReversalDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.CancelTicketOrderReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderCreateReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderPageQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.req.TicketOrderSelfPageQueryReqDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailRespDTO;
import org.opengoofy.index12306.biz.orderservice.dto.resp.TicketOrderDetailSelfRespDTO;
import org.opengoofy.index12306.biz.orderservice.mq.event.PayResultCallbackOrderEvent;
import org.opengoofy.index12306.framework.starter.convention.page.PageResponse;

/**
 * @description 订单接口层
 */
public interface OrderService {

    /**
     * @param orderSn 订单号
     * @return 订单详情
     * @description 跟据订单号查询车票订单
     */
    TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn);

    /**
     * @param requestParam 跟据用户 ID 分页查询对象
     * @return 订单分页详情
     * @description 跟据用户名分页查询车票订单
     */
    PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam);

    /**
     * @param requestParam 商品订单入参
     * @return 订单号
     * @description 创建火车票订单
     */
    String createTicketOrder(TicketOrderCreateReqDTO requestParam);

    /**
     * @param requestParam 关闭火车票订单入参
     * @description 关闭火车票订单
     */
    boolean closeTickOrder(CancelTicketOrderReqDTO requestParam);

    /**
     * @param requestParam 取消火车票订单入参
     * @description 取消火车票订单
     */
    boolean cancelTickOrder(CancelTicketOrderReqDTO requestParam);

    /**
     * @param requestParam 请求参数
     * @description 订单状态反转
     */
    void statusReversal(OrderStatusReversalDTO requestParam);

    /**
     * @param requestParam 请求参数
     * @description 支付结果回调订单
     */
    void payCallbackOrder(PayResultCallbackOrderEvent requestParam);

    /**
     * @param requestParam 请求参数
     * @return 本人车票订单集合
     * @description 查询本人车票订单
     */
    PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam);
}
