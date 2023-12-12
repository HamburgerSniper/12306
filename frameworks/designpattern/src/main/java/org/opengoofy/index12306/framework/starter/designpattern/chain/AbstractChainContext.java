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

package org.opengoofy.index12306.framework.starter.designpattern.chain;

import org.opengoofy.index12306.framework.starter.bases.ApplicationContextHolder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description 抽象责任链上下文
 * @description 实现 {@link CommandLineRunner} 接口，会在SpringBoot应用启动并处理完所有的命令行参数后被执行，通常用于执行一些需要在应用
 * 启动后进行的一次性任务，比如数据导入、数据初始化等
 */
public final class AbstractChainContext<T> implements CommandLineRunner {

    /**
     * @description (beanName, bean)
     */
    private final Map<String, List<AbstractChainHandler>> abstractChainHandlerContainer = new HashMap<>();

    /**
     * @param mark         责任链组件标识
     * @param requestParam 请求参数
     * @description 责任链组件执行
     */
    public void handler(String mark, T requestParam) {
        List<AbstractChainHandler> abstractChainHandlers = abstractChainHandlerContainer.get(mark);
        if (CollectionUtils.isEmpty(abstractChainHandlers)) {
            throw new RuntimeException(String.format("[%s] Chain of Responsibility ID is undefined.", mark));
        }
        abstractChainHandlers.forEach(each -> each.handler(requestParam));
    }

    @Override
    public void run(String... args) throws Exception {
        Map<String, AbstractChainHandler> chainFilterMap = ApplicationContextHolder.getBeansOfType(AbstractChainHandler.class);
        chainFilterMap.forEach(
                (beanName, bean) -> {
                    List<AbstractChainHandler> abstractChainHandlers = abstractChainHandlerContainer.get(bean.mark());
                    if (CollectionUtils.isEmpty(abstractChainHandlers)) {
                        abstractChainHandlers = new ArrayList();
                    }
                    abstractChainHandlers.add(bean);
                    List<AbstractChainHandler> actualAbstractChainHandlers = abstractChainHandlers.stream()
                            .sorted(Comparator.comparing(Ordered::getOrder))
                            .collect(Collectors.toList());
                    abstractChainHandlerContainer.put(bean.mark(), actualAbstractChainHandlers);
                }
        );
    }
}
