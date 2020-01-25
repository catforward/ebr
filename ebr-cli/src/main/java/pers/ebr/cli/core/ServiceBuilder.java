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

import pers.ebr.cli.core.base.ExternalBatchRunner;

/**
 * <pre>
 * the builder of EBR Service
 * </pre>
 *
 * @author l.gong
 */
public class ServiceBuilder {

    private boolean devMode = false;

    public static ServiceBuilder createExternalBatchRunnerBuilder() {
        return new ServiceBuilder();
    }

    public boolean getDevMode() {
        return this.devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public ExternalBatchRunnerService buildExternalBatchRunnerService() {
        return ExternalBatchRunner.getInstance().init(this);
    }
}
