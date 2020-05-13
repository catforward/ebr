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
package pers.ebr.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.server.base.repo.Repository;
import pers.ebr.server.base.repo.RepositoryException;
import pers.ebr.server.model.TaskFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * The TaskItemPersistService
 *
 * @author l.gong
 */
public class TaskItemPersistService {

    private final static Logger logger = LoggerFactory.getLogger(TaskItemPersistService.class);

    public boolean save(TaskFlow flow) {
        try {
            Repository.get().saveFlow(flow.flowId().orElseThrow(), flow.toJsonString());
        } catch (RepositoryException ex) {
            logger.error("procedure [saveTaskFlow] error...");
            return false;
        }
        return true;
    }

    public List<String> getAllTaskFlowId() {
        List<String> arr = null;
        try {
            arr = Repository.get().loadAllFlowId();
        } catch(RepositoryException ex) {
            arr = new ArrayList<>();
            logger.error("procedure [saveTaskFlow] error...");
        } finally {
            return arr;
        }
    }

    public String getTaskFlowDefineById(String flowId) {
        String flowDefine = "";
        try {
            flowDefine = Repository.get().getTaskFlowDefineById(flowId);
        } catch (RepositoryException ex) {
            logger.error("procedure [getTaskFlowDefineById] error...");
        } finally {
            return flowDefine;
        }
    }

}
