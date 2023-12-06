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

package org.opengoofy.index12306.biz.ticketservice.mq.event;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description Canal Binlog 监听触发时间
 */
@Data
public class CanalBinlogEvent {

    /**
     * @description 变更数据
     */
    private List<Map<String, Object>> data;

    /**
     * @description 数据库名称
     */
    private String database;

    /**
     * @description es 是指 Mysql Binlog 里原始的时间戳，也就是数据原始变更的时间
     * @description Canal 的消费延迟 = ts - es
     */
    private Long es;

    /**
     * @description 递增 ID，从 1 开始
     */
    private Long id;

    /**
     * @description 当前变更是否是 DDL 语句
     */
    private Boolean isDdl;

    /**
     * @description 表结构字段类型
     */
    private Map<String, Object> mysqlType;

    /**
     * @description UPDATE 模式下旧数据
     */
    private List<Map<String, Object>> old;

    /**
     * @description 主键名称
     */
    private List<String> pkNames;

    /**
     * @description SQL 语句
     */
    private String sql;

    /**
     * @description SQL 类型
     */
    private Map<String, Object> sqlType;

    /**
     * @description 表名
     */
    private String table;

    /**
     * @description ts 是指 Canal 收到这个 Binlog，构造为自己协议对象的时间
     * @description 应用消费的延迟 = now - ts
     */
    private Long ts;

    /**
     * @description INSERT（新增）、UPDATE（更新）、DELETE（删除）等等
     */
    private String type;
}
