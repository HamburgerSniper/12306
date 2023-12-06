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

package org.opengoofy.index12306.biz.payservice.remote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 车票订单详情返回参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketOrderPassengerDetailRespDTO {

    /**
     * @description 用户id
     */
    private Long userId;

    /**
     * @description 用户名
     */
    private String username;

    /**
     * @description 席别类型
     */
    private Integer seatType;

    /**
     * @description 车厢号
     */
    private String carriageNumber;

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
     * @description 车票类型 0：成人 1：儿童 2：学生 3：残疾军人
     */
    private Integer ticketType;

    /**
     * @description 订单金额
     */
    private Integer amount;

    /**
     * @description 车票状态
     */
    private Integer status;
}
