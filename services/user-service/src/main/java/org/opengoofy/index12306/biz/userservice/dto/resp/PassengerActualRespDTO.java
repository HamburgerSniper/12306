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

package org.opengoofy.index12306.biz.userservice.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @description 乘车人真实返回参数，不包含脱敏信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PassengerActualRespDTO {

    /**
     * @description 乘车人id
     */
    private String id;

    /**
     * @description 用户名
     */
    private String username;

    /**
     * @description 真实姓名
     */
    private String realName;

    /**
     * @description 证件类型
     */
    private Integer idType;

    /**
     * @description 证件号码
     */
    private String idCard;

    /**
     * @description 优惠类型
     */
    private Integer discountType;

    /**
     * @description 手机号
     */
    private String phone;

    /**
     * @description 添加日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;

    /**
     * @description 审核状态
     */
    private Integer verifyStatus;
}
