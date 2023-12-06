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

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.opengoofy.index12306.framework.starter.database.base.BaseDO;

import java.util.Date;

/**
 * @description 列车实体
 */
@Data
@TableName("t_train")
public class TrainDO extends BaseDO {

    /**
     * @description id
     */
    private Long id;

    /**
     * @description 列车车次
     */
    private String trainNumber;

    /**
     * @description 列车类型 0：高铁 1：动车 2：普通车
     */
    private Integer trainType;

    /**
     * @description 列车标签 0：复兴号 1：智能动车组 2：静音车厢 3：支持选铺
     */
    private String trainTag;

    /**
     * @description 列车品牌类型 0：GC-高铁/城际 1：D-动车 2：Z-直达 3：T-特快 4：K-快速 5：其他 6：复兴号 7：智能动车组
     */
    private String trainBrand;

    /**
     * @description 起始站
     */
    private String startStation;

    /**
     * @description 终点站
     */
    private String endStation;

    /**
     * @description 起始城市
     */
    private String startRegion;

    /**
     * @description 终点城市
     */
    private String endRegion;

    /**
     * @description 销售时间
     */
    private Date saleTime;

    /**
     * @description 销售状态 0：可售 1：不可售 2：未知
     */
    private Integer saleStatus;

    /**
     * @description 出发时间
     */
    private Date departureTime;

    /**
     * @description 到达时间
     */
    private Date arrivalTime;
}
