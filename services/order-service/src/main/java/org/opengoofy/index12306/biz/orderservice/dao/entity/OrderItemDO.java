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

package org.opengoofy.index12306.biz.orderservice.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengoofy.index12306.framework.starter.database.base.BaseDO;

/**
 * @description 订单明细数据库实体
 */
@Data
@TableName("t_order_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDO extends BaseDO {

    /**
     * @description id
     */
    private Long id;

    /**
     * @description 订单号
     */
    private String orderSn;

    /**
     * @description 用户id
     */
    private String userId;

    /**
     * @description 用户名
     */
    private String username;

    /**
     * @description 列车id
     */
    private Long trainId;

    /**
     * @description 车厢号
     */
    private String carriageNumber;

    /**
     * @description 座位类型
     */
    private Integer seatType;

    /**
     * @description 座位号
     */
    private String seatNumber;

    /**
     * @description 真实姓名
     */
    private String realName;

    /**
     * @description 证件类型
     */
    private Integer idType;

    /**
     * @description 证件号
     */
    private String idCard;

    /**
     * @description 手机号
     */
    private String phone;

    /**
     * @description 订单状态
     */
    private Integer status;

    /**
     * @description 订单金额
     */
    private Integer amount;

    /**
     * @description 车票类型
     */
    private Integer ticketType;
}
