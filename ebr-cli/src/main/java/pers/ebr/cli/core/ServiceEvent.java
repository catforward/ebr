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

import java.util.Map;

/**
 * <pre>
 * the service event that notice the caller of this lib
 * what event had occurred
 * </pre>
 * @author l.gong
 */
public interface ServiceEvent {
    enum Type {
        /* occurred at job's state changed */
        JOB_STATE_CHANGED,
        /* occurred at all jobs done */
        ALL_JOB_FINISHED,
        /* occurred at all internal service finished */
        SERVICE_SHUTDOWN,
    }
    enum Symbols {
        /* identity url of job that the state changed */
        JOB_URL("JOB_URL"),
        /* the new state of job */
        JOB_STATE("JOB_STATE");

        private final String val;
        Symbols(String newValue) {
            val = newValue;
        }
    }

    /**
     * the type of service event
     * @return Type
     */
    Type type();

    /**
     * the payload within service event
     * @return Map
     */
    Map<Symbols, Object> data();
}
