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

package org.opengoofy.index12306.biz.userservice.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @description 用户注册责任链枚举类
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UserRegisterChainHandlerEnum {
    PARAM_NOTNULL(0, "检查必填参数项是否为空"),
    HAS_USERNAME(1, "检验用户名是否重复注册"),
    CHECK_DELETION(2, "检查证件号是否多次注销");
    private Integer code;
    private String description;
}
