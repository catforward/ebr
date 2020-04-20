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
package pers.ebr.server.base.graph;

/**
 * <pre>
 * The Builder of Graph
 * </pre>
 *
 * @author l.gong
 */
public class GraphBuilder {
    boolean allowsSelfLoops = false;

    private GraphBuilder() {}

    public static GraphBuilder directed() {
        return new GraphBuilder();
    }

    public GraphBuilder setAllowsSelfLoops(boolean allowsSelfLoops) {
        this.allowsSelfLoops = allowsSelfLoops;
        if (this.allowsSelfLoops) {
            throw new UnsupportedOperationException("不好意思哦，还不支持自环图...");
        }
        return this;
    }

    public <V> DirectedGraph<V> build() {
        return new DirectedGraphImpl<>(this);
    }
}
