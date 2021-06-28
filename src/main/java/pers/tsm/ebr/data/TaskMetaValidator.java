/**
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
package pers.tsm.ebr.data;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.types.ResultEnum;

/**
 *
 *
 * @author l.gong
 */
public class TaskMetaValidator implements IValidator {
    private static final Logger logger = LoggerFactory.getLogger(TaskMetaValidator.class);

    private static final int MAX_LEN_DESC = 512;

    TaskMetaValidator() {}

    @Override
    public void validate(Task task) {
        requireNonNull(task);
        switch (task.type) {
        case FLOW: {
            validateFlow(task);
            break;
        }
        case GROUP: {
            validateGroup(task);
            break;
        }
        case TASK: {
            validateTask(task);
            break;
        }
        default: break;
        }
    }

    private void validateFlow(Task task) {
        if (!isNull(task.meta.desc) && MAX_LEN_DESC < task.meta.desc.length()) {
            logger.debug("flow[{}]'s parameter[desc]'s length is too long.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (!isNull(task.meta.group) && !task.meta.group.isBlank()) {
            logger.debug("flow[{}]'s parameter[group] existed.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (!isNull(task.meta.depends) && !task.meta.depends.isEmpty()) {
            logger.debug("flow[{}]'s parameter[depends] existed.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (!isNull(task.meta.cmd) && !task.meta.cmd.isBlank()) {
            logger.debug("flow[{}]'s parameter[cmd] existed.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
    }

    private void validateGroup(Task task) {
        if (!isNull(task.meta.desc) && MAX_LEN_DESC < task.meta.desc.length()) {
            logger.debug("group[{}]'s parameter[desc]'s length is too long.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (!isNull(task.meta.cmd) && !task.meta.cmd.isBlank()) {
            logger.debug("group[{}]'s parameter[cmd] existed.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (isNull(task.meta.group) || task.meta.group.isBlank()) {
            logger.debug("group[{}]'s parameter[group] should be set.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
    }

    private void validateTask(Task task) {
        if (!isNull(task.meta.desc) && MAX_LEN_DESC < task.meta.desc.length()) {
            logger.debug("task[{}]'s parameter[desc]'s length is too long.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (isNull(task.meta.cmd) || task.meta.cmd.isBlank()) {
            logger.debug("task[{}]'s parameter[cmd] should be set.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        if (isNull(task.meta.group) || task.meta.group.isBlank()) {
            logger.debug("task[{}]'s parameter[group] should be set.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
    }

}
