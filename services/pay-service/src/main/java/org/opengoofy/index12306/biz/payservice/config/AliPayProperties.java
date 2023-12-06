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

package org.opengoofy.index12306.biz.payservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description 支付宝配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = AliPayProperties.PREFIX)
public class AliPayProperties {

    public static final String PREFIX = "pay.alipay";

    /**
     * @description 开放平台上创建的应用的 ID
     */
    private String appId;

    /**
     * @description 商户私钥
     */
    private String privateKey;

    /**
     * @description 支付宝公钥字符串（公钥模式下设置，证书模式下无需设置）
     */
    private String alipayPublicKey;

    /**
     * @description 网关地址
     * @description 线上：<a href=https://openapi.alipay.com/gateway.do>https://openapi.alipay.com/gateway.do</a>
     * @description 沙箱：<a href=https://openapi.alipaydev.com/gateway.do>https://openapi.alipaydev.com/gateway.do</a>
     */
    private String serverUrl;

    /**
     * @description 支付结果回调地址
     */
    private String notifyUrl;

    /**
     * @description 报文格式，推荐：json
     */
    private String format;

    /**
     * @description 字符串编码，推荐：utf-8
     */
    private String charset;

    /**
     * @description 签名算法类型，推荐：RSA2
     */
    private String signType;
}
