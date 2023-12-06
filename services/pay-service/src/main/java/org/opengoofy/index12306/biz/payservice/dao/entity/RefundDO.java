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

package org.opengoofy.index12306.biz.payservice.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.opengoofy.index12306.framework.starter.database.base.BaseDO;

import java.util.Date;

/**
 * @description 退款记录实体
 */
@Data
@TableName("t_refund")
public class RefundDO extends BaseDO {

    /**
     * @description id
     */
    private Long id;

    /**
     * @description 支付流水号
     */
    private String paySn;

    /**
     * @description 订单号
     */
    private String orderSn;

    /**
     * @description 三方交易凭证号
     */
    private String tradeNo;

    /**
     * @description 退款金额
     */
    private Integer amount;

    /**
     * @description 用户ID
     */
    private Long userId;

    /**
     * @description 用户名
     */
    private String username;

    /**
     * @description 列车ID
     */
    private Long trainId;

    /**
     * @description 列车车次
     */
    private String trainNumber;

    /**
     * @description 乘车日期
     */
    private Date ridingDate;

    /**
     * @description 出发站点
     */
    private String departure;

    /**
     * @description 到达站点
     */
    private String arrival;

    /**
     * @description 出发时间
     */
    private Date departureTime;

    /**
     * @description 到达时间
     */
    private Date arrivalTime;

    /**
     * @description 座位类型
     */
    private Integer seatType;

    /**
     * @description 证件类型
     */
    private Integer idType;

    /**
     * @description 证件号
     */
    private String idCard;

    /**
     * @description 真实姓名
     */
    private String realName;

    /**
     * @description 订单状态
     */
    private Integer status;

    /**
     * @description 退款时间
     */
    private Date refundTime;
}
