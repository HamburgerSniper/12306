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

package org.opengoofy.index12306.biz.ticketservice.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.opengoofy.index12306.framework.starter.database.base.BaseDO;

import java.util.Date;

/**
 * @description 列车站点实体
 */
@Data
@TableName("t_train_station")
public class TrainStationDO extends BaseDO {

    /**
     * @description id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * @description 车次id
     */
    private Long trainId;

    /**
     * @description 车站id
     */
    private Long stationId;

    /**
     * @description 站点顺序
     */
    private String sequence;

    /**
     * @description 出发站点
     */
    private String departure;

    /**
     * @description 到达站点
     */
    private String arrival;

    /**
     * @description 起始城市
     */
    private String startRegion;

    /**
     * @description 终点城市
     */
    private String endRegion;

    /**
     * @description 到站时间
     */
    private Date arrivalTime;

    /**
     * @description 出站时间
     */
    private Date departureTime;

    /**
     * @description 停留时间，单位分
     */
    private Integer stopoverTime;
}
