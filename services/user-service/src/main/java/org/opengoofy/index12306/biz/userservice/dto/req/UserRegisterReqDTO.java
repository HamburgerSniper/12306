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

package org.opengoofy.index12306.biz.userservice.dto.req;

import lombok.Data;

/**
 * @description 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {

    /**
     * @description 用户名
     */
    private String username;

    /**
     * @description 密码
     */
    private String password;

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
     * @description 手机号
     */
    private String phone;

    /**
     * @description 邮箱
     */
    private String mail;

    /**
     * @description 旅客类型
     */
    private Integer userType;

    /**
     * @description 审核状态
     */
    private Integer verifyState;

    /**
     * @description 邮编
     */
    private String postCode;

    /**
     * @description 地址
     */
    private String address;

    /**
     * @description 国家/地区
     */
    private String region;

    /**
     * @description 固定电话
     */
    private String telephone;
}
