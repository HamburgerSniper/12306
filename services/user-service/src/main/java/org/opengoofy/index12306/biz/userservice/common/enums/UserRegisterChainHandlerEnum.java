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
