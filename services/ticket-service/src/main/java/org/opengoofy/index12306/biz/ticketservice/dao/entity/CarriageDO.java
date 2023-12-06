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

/**
 * @description 车厢实体
 */
@Data
@TableName("t_carriage")
public class CarriageDO extends BaseDO {

    /**
     * @description id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * @description 列车id
     */
    private Long trainId;

    /**
     * @description 车厢号
     */
    private String carriageNumber;

    /**
     * @description 车厢类型
     */
    private Integer carriageType;

    /**
     * @description 座位数
     */
    private Integer seatCount;
}
