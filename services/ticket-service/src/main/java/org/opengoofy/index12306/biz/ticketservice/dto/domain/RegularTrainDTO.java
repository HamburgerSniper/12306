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

package org.opengoofy.index12306.biz.ticketservice.dto.domain;

import lombok.Data;

/**
 * @description 普通车实体
 */
@Data
public class RegularTrainDTO {

    /**
     * @description 软卧数量
     */
    private Integer softSleeperQuantity;

    /**
     * @description 软卧候选标识
     */
    private Boolean softSleeperCandidate;

    /**
     * @description 软卧价格
     */
    private Integer softSleeperPrice;

    /**
     * @description 高级软卧数量
     */
    private Integer deluxeSoftSleeperQuantity;

    /**
     * @description 高级软卧候选标识
     */
    private Boolean deluxeSoftSleeperCandidate;

    /**
     * @description 高级软卧价格
     */
    private Integer deluxeSoftSleeperPrice;

    /**
     * @description 硬卧数量
     */
    private Integer hardSleeperQuantity;

    /**
     * @description 硬卧候选标识
     */
    private Boolean hardSleeperCandidate;

    /**
     * @description 硬卧价格
     */
    private Integer hardSleeperPrice;

    /**
     * @description 硬座数量
     */
    private Integer hardSeatQuantity;

    /**
     * @description 硬座候选标识
     */
    private Boolean hardSeatCandidate;

    /**
     * @description 硬座价格
     */
    private Integer hardSeatPrice;

    /**
     * @description 无座数量
     */
    private Integer noSeatQuantity;

    /**
     * @description 无座候选标识
     */
    private Boolean noSeatCandidate;

    /**
     * @description 无座价格
     */
    private Integer noSeatPrice;
}
