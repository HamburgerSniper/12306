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

package org.opengoofy.index12306.biz.payservice.dto.base;

import lombok.Getter;
import lombok.Setter;
import org.opengoofy.index12306.framework.starter.distributedid.toolkit.SnowflakeIdUtil;

/**
 * @description 抽象支付入参实体
 */
public abstract class AbstractPayRequest implements PayRequest {

    /**
     * @description 交易环境，H5、小程序、网站等
     */
    @Getter
    @Setter
    private Integer tradeType;

    /**
     * @description 订单号
     */
    @Getter
    @Setter
    private String orderSn;

    /**
     * @description 支付渠道
     */
    @Getter
    @Setter
    private Integer channel;

    /**
     * @description 商户订单号
     * 由商家自定义，64个字符以内，仅支持字母、数字、下划线且需保证在商户端不重复
     */
    @Getter
    @Setter
    private String orderRequestId = SnowflakeIdUtil.nextIdStr();

    @Override
    public AliPayRequest getAliPayRequest() {
        return null;
    }

    @Override
    public String getOrderRequestId() {
        return orderRequestId;
    }

    @Override
    public String buildMark() {
        return null;
    }
}
