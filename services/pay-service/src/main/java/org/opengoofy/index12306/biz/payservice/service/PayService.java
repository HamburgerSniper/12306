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

package org.opengoofy.index12306.biz.payservice.service;

import org.opengoofy.index12306.biz.payservice.dto.PayCallbackReqDTO;
import org.opengoofy.index12306.biz.payservice.dto.PayInfoRespDTO;
import org.opengoofy.index12306.biz.payservice.dto.PayRespDTO;
import org.opengoofy.index12306.biz.payservice.dto.RefundReqDTO;
import org.opengoofy.index12306.biz.payservice.dto.RefundRespDTO;
import org.opengoofy.index12306.biz.payservice.dto.base.PayRequest;

/**
 * @description 支付接口层
 */
public interface PayService {

    /**
     * @param requestParam 创建支付单实体
     * @return 支付返回详情
     * @description 创建支付单
     */
    PayRespDTO commonPay(PayRequest requestParam);

    /**
     * @param requestParam 回调支付单实体
     * @description 支付单回调
     */
    void callbackPay(PayCallbackReqDTO requestParam);

    /**
     * @param orderSn 订单号
     * @return 支付单详情
     * @description 跟据订单号查询支付单详情
     */
    PayInfoRespDTO getPayInfoByOrderSn(String orderSn);

    /**
     * @param paySn 支付单流水号
     * @return 支付单详情
     * @description 跟据支付流水号查询支付单详情
     */
    PayInfoRespDTO getPayInfoByPaySn(String paySn);

    /**
     * @param requestParam 退款请求参数
     * @return 退款返回详情
     * @description 公共退款接口
     */
    RefundRespDTO commonRefund(RefundReqDTO requestParam);
}
