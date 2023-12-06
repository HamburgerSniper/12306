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

package org.opengoofy.index12306.biz.payservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @description 支付单回调请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayCallbackReqDTO {

    /**
     * @description 订单号
     */
    private String orderSn;

    /**
     * @description 商户订单号
     */
    private String outOrderSn;

    /**
     * @description 支付渠道
     */
    private String channel;

    /**
     * @description 支付环境
     */
    private String tradeType;

    /**
     * @description 订单标题
     */
    private String subject;

    /**
     * @description 交易凭证号
     */
    private String tradeNo;

    /**
     * @description 交易总金额
     */
    private Integer totalAmount;

    /**
     * @description 付款时间
     */
    private Date gmtPayment;

    /**
     * @description 支付金额
     */
    private Integer payAmount;

    /**
     * @description 支付状态
     */
    private Integer status;

    /**
     * @description 商户订单号
     */
    private String orderRequestId;
}
