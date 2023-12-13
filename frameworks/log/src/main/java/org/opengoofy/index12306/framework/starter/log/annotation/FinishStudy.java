package org.opengoofy.index12306.framework.starter.log.annotation;


import org.opengoofy.index12306.framework.starter.log.constants.FinishStudyEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FinishStudy {
    FinishStudyEnum status();
}
