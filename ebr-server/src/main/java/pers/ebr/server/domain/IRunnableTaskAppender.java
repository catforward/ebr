/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.server.domain;

/**
 * <p>
 * 向本接口的实现者中添加可运行的任务对象引用
 * </p>
 *
 * @author l.gong
 */
public interface IRunnableTaskAppender {
    /**
     * 追加一个新的任务对象引用
     * @param taskflow 待追加的任务所在任务流
     * @param task     待追加的任务
     */
    void append(ITaskflow taskflow, IExternalCommandTask task);
}
