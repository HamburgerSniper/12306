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
 * @description 用户修改请求参数
 */
@Data
public class UserUpdateReqDTO {

    /**
     * @description 用户ID
     */
    private String id;

    /**
     * @description 用户名
     */
    private String username;

    /**
     * @description 邮箱
     */
    private String mail;

    /**
     * @description 旅客类型
     */
    private Integer userType;

    /**
     * @description 邮编
     */
    private String postCode;

    /**
     * @description 地址
     */
    private String address;
}
