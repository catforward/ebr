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
package pers.tsm.ebr.types;

/**
 *
 *
 * @author l.gong
 */
public enum TaskStateEnum {
    UNKNOWN(0, "unknown"),
    STANDBY(1, "standby"),
    RUNNING(2, "running"),
    PAUSED(3, "paused"),
    SKIPPED(4, "skipped"),
    ERROR(5, "error"),
    FINISHED(6, "finished"),
    ;

    private final int state;
    private final String name;

    TaskStateEnum(int state, String name) {
        this.state = state;
        this.name = name;
    }

    public int getState() {
        return this.state;
    }
    
    public String getName() {
        return this.name;
    }

}
