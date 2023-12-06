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

import java.util.Date;

/**
 * @description 订单数据库实体
 */
@Data
@TableName("t_order")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDO extends BaseDO {

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
     * @description 列车车次
     */
    private String trainNumber;

    /**
     * @description 出发站点
     */
    private String departure;

    /**
     * @description 到达站点
     */
    private String arrival;

    /**
     * @description 订单来源
     */
    private Integer source;

    /**
     * @description 订单状态
     */
    private Integer status;

    /**
     * @description 下单时间
     */
    private Date orderTime;

    /**
     * @description 支付方式
     */
    private Integer payType;

    /**
     * @description 支付时间
     */
    private Date payTime;

    /**
     * @description 乘车日期
     */
    private Date ridingDate;

    /**
     * @description 出发时间
     */
    private Date departureTime;

    /**
     * @description 出发时间
     */
    private Date arrivalTime;
}
