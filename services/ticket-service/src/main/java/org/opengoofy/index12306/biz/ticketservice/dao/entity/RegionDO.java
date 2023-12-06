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

/**
 * @description 地区表
 */
@Data
@TableName("t_region")
public class RegionDO extends BaseDO {

    /**
     * @description id
     */
    private Long id;

    /**
     * @description 地区名称
     */
    private String name;

    /**
     * @description 地区全名
     */
    private String fullName;

    /**
     * @description 地区编码
     */
    private String code;

    /**
     * @description 地区首字母
     */
    private String initial;

    /**
     * @description 拼音
     */
    private String spell;

    /**
     * @description 热门标识
     */
    private Integer popularFlag;
}
