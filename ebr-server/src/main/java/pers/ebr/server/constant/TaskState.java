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
package pers.ebr.server.constant;

/**
 * <pre>
 * Task Type in EBR
 * </pre>
 *
 * @author l.gong
 */
public enum TaskState {
    /** 任务待机时 */
    INACTIVE(1),
    /** 任务执行跳过时 */
    SKIP(2),
    /** 任务执行时 */
    ACTIVE(3),
    /** 任务执行成功时 */
    COMPLETE(4),
    /** 任务执行异常时 */
    FAILED(5);

    private final int state;

    TaskState(int state) {
        this.state = state;
    }
}
