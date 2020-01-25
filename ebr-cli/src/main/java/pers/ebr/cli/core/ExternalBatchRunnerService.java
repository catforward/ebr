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
package pers.ebr.cli.core;

/**
 * <pre>
 * the interface define the services which provided by ebr lib
 * </pre>
 * @author l.gong
 */
public interface ExternalBatchRunnerService {

    /**
     * <pre>
     * set a instance of ServiceEventListener
     * when you focus on some internal event of ebr lib
     * </pre>
     * @param listener the given listener object
     */
    void setServiceEventListener(ServiceEventListener listener);

    /**
     * <pre>
     * create a job flow and return the url of the job flow
     * by given root task
     * </pre>
     * @param root the root task
     * @return String the url of a job flow witch created from the root task
     */
    String createJobFlow(Task root);

    /**
     * <pre>
     * launch the given job flow by url
     * </pre>
     * @param url the url of target job flow
     */
    void launchJobFlow(String url);

}
