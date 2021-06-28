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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import pers.tsm.ebr.common.AppException;
import pers.tsm.ebr.types.ResultEnum;

/**
 *
 *
 * @author l.gong
 */
class DAGFlowValidator implements IValidator {
    private static final Logger logger = LoggerFactory.getLogger(DAGFlowValidator.class);

    private Map<String, MutableGraph<Task>> urlGraphMapping;

    DAGFlowValidator() {
        urlGraphMapping = new HashMap<>();
    }

    @Override
    public void validate(Task root) {
        requireNonNull(root);
        try {
            createTaskGraph(root);
            dumpFlowInfo();
        } catch (IllegalArgumentException ex) {
            logger.error("graph error: ", ex);
            throw new AppException(ResultEnum.ERR_10103);
        }
    }

    private MutableGraph<Task> createEmptyGraph() {
        return GraphBuilder.directed().allowsSelfLoops(false).build();
    }

    private void createTaskGraph(Task task) {
        String graphUrl = isNull(task.parent) ? task.url : task.parent.url;
        MutableGraph<Task> graph = urlGraphMapping.get(graphUrl);
        if (isNull(graph)) {
            graph = createEmptyGraph();
            urlGraphMapping.put(graphUrl, graph);
        }
        for (Task predecessor : task.depends) {
            if (!isNull(task.parent) && task.parent.url.equals(predecessor.url)) {
                continue;
            }
            graph.putEdge(predecessor, task);
        }
        for (Task child : task.children) {
            createTaskGraph(child);
        }
    }

    private void dumpFlowInfo() {
        urlGraphMapping.forEach((url, graph) -> {
            logger.debug("url:{} -> {}", url, graph.toString());
        });
    }

}
