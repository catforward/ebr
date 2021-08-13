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
package pers.tsm.ebr.common;

import pers.tsm.ebr.data.VerticleProp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;

/**
 * <pre>app's init context</pre>
 *
 * @author l.gong
 */
public final class AppContext {

    private final List<VerticleProp> verticleDescList = new CopyOnWriteArrayList<>();
    /**
     * key: api url value: service id
     */
    private final Map<String, String> apiServiceMap = new ConcurrentHashMap<>();

    private static class InstanceHolder {
        private static final AppContext INSTANCE = new AppContext();
    }

    private AppContext() {}

    public static void addApiServiceMapping(String url, String serviceId) {
        requireNonNull(url);
        requireNonNull(serviceId);
        InstanceHolder.INSTANCE.apiServiceMap.put(url, serviceId);
    }

    public static void addVerticle(VerticleProp prop) {
        requireNonNull(prop);
        InstanceHolder.INSTANCE.verticleDescList.add(prop);
    }

    public static Map<String, String> getApiServiceMapping() {
        return Map.copyOf(InstanceHolder.INSTANCE.apiServiceMap);
    }

    public static List<VerticleProp> getVerticleDescList() {
        return List.copyOf(InstanceHolder.INSTANCE.verticleDescList);
    }

}
