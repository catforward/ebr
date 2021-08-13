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

import io.vertx.core.json.JsonObject;

import static java.util.Objects.isNull;

/**
 * <pre>describing a define file</pre>
 *
 * @author l.gong
 */
public class TaskDefineFileProp {

    private String absolutePath;
    private String flowUrl;
    private String state;
    private JsonObject content;
    private long fileSize;
    private long lastModifiedTime;

    public TaskDefineFileProp() {
        // do nothing
    }

    @Override
    public String toString() {
        return String.format("flowUrl: %s, path: %s, lastModifiedTime: %d", flowUrl, absolutePath, lastModifiedTime);
    }

    public boolean isNewerThan(TaskDefineFileProp other) {
        return isNull(other) || this.lastModifiedTime > other.lastModifiedTime;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absPath) {
        this.absolutePath = absPath;
    }

    public String getFlowUrl() {
        return flowUrl;
    }

    public void setFlowUrl(String flowUrl) {
        this.flowUrl = flowUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public JsonObject getContent() {
        return content;
    }

    public void setContent(JsonObject content) {
        this.content = content;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

}
