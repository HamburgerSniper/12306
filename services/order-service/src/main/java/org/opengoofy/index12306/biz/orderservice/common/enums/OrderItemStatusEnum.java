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

package org.opengoofy.index12306.biz.orderservice.common.enums;

import cn.crane4j.annotation.ContainerEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @description 订单明细状态枚举
 */
@Getter
@ContainerEnum(namespace = "OrderItemStatusEnum", key = "status", value = "statusName")
@RequiredArgsConstructor
public enum OrderItemStatusEnum {

    /**
     * @description 待支付
     */
    PENDING_PAYMENT(0, "待支付"),

    /**
     * @description 已支付
     */
    ALREADY_PAID(10, "已支付"),

    /**
     * @description 已进站
     */
    ALREADY_PULL_IN(20, "已进站"),

    /**
     * @description 已取消
     */
    CLOSED(30, "已取消"),

    /**
     * @description 已退票
     */
    REFUNDED(40, "已退票"),

    /**
     * @description 已改签
     */
    RESCHEDULED(50, "已改签");

    private final Integer status;

    private final String statusName;
}
