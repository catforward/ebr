/*
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.tsm.ebr.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.tsm.ebr.common.AppConsts;
import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.types.ResultEnum;
import pers.tsm.ebr.types.TaskTypeEnum;

import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * <pre>flow's validator(external command)</pre>
 *
 * @author l.gong
 */
public class ExternalScriptAttrValidator implements IValidator {
    private static final Logger logger = LoggerFactory.getLogger(ExternalScriptAttrValidator.class);

    ExternalScriptAttrValidator() {}

    @Override
    public void validate(Task task) {
        requireNonNull(task);
        if (TaskTypeEnum.TASK != task.getType()) {
            return;
        }
        String[] fullCommand = task.meta.script.split(AppConsts.SPACE);
        if (fullCommand.length == 0) {
            logger.debug("task[{}]'s parameter[script] not define.", task.meta.id);
            throw new AppException(ResultEnum.ERR_10104);
        }
        File scriptFile = new File(fullCommand[0]);
        if (!scriptFile.isFile()) {
            logger.debug("task[{}]'s script[[{}] is not existed.", task.meta.id, scriptFile.getAbsolutePath());
            throw new AppException(ResultEnum.ERR_10105);
        } else if (!scriptFile.canExecute()) {
            logger.debug("task[{}]'s script[[{}] is not executables.", task.meta.id, scriptFile.getAbsolutePath());
            throw new AppException(ResultEnum.ERR_10106);
        }
    }

}
