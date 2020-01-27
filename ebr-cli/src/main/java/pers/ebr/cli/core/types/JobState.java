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
package pers.ebr.cli.core.types;

/**
 * <pre>
 * define the state of job
 * INACTIVE -> [Start processing] -> ACTIVE
 * ACTIVE -> [Complete] -> COMPLETE
 * ACTIVE -> [Failure] -> FAILED
 * </pre>
 * @author l.gong
 */
public enum JobState {
    /** 任务待机时 */
    INACTIVE,
    /** 任务执行时 */
    ACTIVE,
    /** 任务执行成功时 */
    COMPLETE,
    /** 任务执行异常时 */
    FAILED,
}
