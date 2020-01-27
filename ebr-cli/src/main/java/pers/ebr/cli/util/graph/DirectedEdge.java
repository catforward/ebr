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
package pers.ebr.cli.util.graph;

import java.util.Objects;

import static pers.ebr.cli.util.MiscUtils.checkNotNull;

/**
 * <pre>
 * 有向边
 * </pre>
 * @author l.gong
 */
class DirectedEdge<T> {
    private final T vertexFrom;
    private final T vertexTo;

    DirectedEdge(T vertexFrom, T vertexTo) {
        checkNotNull(vertexFrom);
        checkNotNull(vertexTo);
        if (vertexFrom.equals(vertexTo)) {
            throw new IllegalArgumentException("起点和终点不能相等");
        }
        this.vertexFrom = vertexFrom;
        this.vertexTo = vertexTo;
    }

    T source() {
        return this.vertexFrom;
    }

    T target() {
        return this.vertexTo;
    }

    @Override
    public String toString() {
        return "<" + this.vertexFrom + " -> " + this.vertexTo + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vertexFrom, this.vertexTo);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof DirectedEdge)) {
            return false;
        }

        DirectedEdge edge = (DirectedEdge) o;
        return Objects.equals(this.vertexFrom, edge.vertexFrom) &&
                Objects.equals(this.vertexTo, edge.vertexTo);
    }
}
