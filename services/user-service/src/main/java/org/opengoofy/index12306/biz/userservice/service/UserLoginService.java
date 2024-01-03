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

package org.opengoofy.index12306.biz.userservice.service;

import org.opengoofy.index12306.biz.userservice.common.annotation.FinishStudy;
import org.opengoofy.index12306.biz.userservice.dto.req.UserDeletionReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserLoginReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserLoginRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;

import static org.opengoofy.index12306.biz.userservice.common.annotation.FinishStudy.FinishStudyEnum.TRUE;

/**
 * @description 用户登录接口
 */
@FinishStudy(status = TRUE)
public interface UserLoginService {

    /**
     * @param requestParam 用户登录入参
     * @return 用户登录返回结果
     * @description 用户登录接口
     */
    @FinishStudy(status = TRUE)
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * @param accessToken 用户登录 Token 凭证
     * @return 用户是否登录返回结果
     * @description 通过 Token 检查用户是否登录
     */
    @FinishStudy(status = TRUE)
    UserLoginRespDTO checkLogin(String accessToken);

    /**
     * @param accessToken 用户登录 Token 凭证
     * @description 用户退出登录
     */
    @FinishStudy(status = TRUE)
    void logout(String accessToken);

    /**
     * @param username 用户名
     * @return 用户名是否存在返回结果(username能用则返回true, username不能用则返回false)
     * @description 用户名是否存在
     */
    @FinishStudy(status = TRUE)
    Boolean hasUsername(String username);

    /**
     * @param requestParam 用户注册入参
     * @return 用户注册返回结果
     * @description 用户注册
     */
    @FinishStudy(status = TRUE)
    UserRegisterRespDTO register(UserRegisterReqDTO requestParam);

    /**
     * @param requestParam 注销用户入参
     * @description 注销用户
     */
    @FinishStudy(status = TRUE)
    void deletion(UserDeletionReqDTO requestParam);
}
