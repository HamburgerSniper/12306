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

package org.opengoofy.index12306.biz.orderservice.dto.resp;

import cn.crane4j.annotation.Disassemble;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description 车票订单详情返回参数
 */
@Data
public class TicketOrderDetailRespDTO {

    /**
     * @description 订单号
     */
    private String orderSn;

    /**
     * @description 列车 ID
     */
    private Long trainId;

    /**
     * @description 出发站点
     */
    private String departure;

    /**
     * @description 到达站点
     */
    private String arrival;

    /**
     * @description 乘车日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date ridingDate;

    /**
     * @description 订票日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderTime;

    /**
     * @description 列车车次
     */
    private String trainNumber;

    /**
     * @description 出发时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date departureTime;

    /**
     * @description 到达时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date arrivalTime;

    /**
     * @description 乘车人订单详情
     */
    @Disassemble
    private List<TicketOrderPassengerDetailRespDTO> passengerDetails;
}
