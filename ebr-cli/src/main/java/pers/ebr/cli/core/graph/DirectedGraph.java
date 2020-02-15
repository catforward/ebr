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
package pers.ebr.cli.core.graph;

import java.util.Set;

/**
 * <pre>
 * 有序图
 * 需求很简单
 * - 可以修改顶点，边
 * - 可以获取指定顶点的前驱，后继顶点
 * </pre>
 *
 * @author l.gong
 */
public interface DirectedGraph<T> {

    /**
     * <pre>
     * 顶点集合
     * </pre>
     * @return 顶点集合
     */
    Set<T> vertexes();

    /**
     * <pre>
     * 边集合
     * </pre>
     * @return 边集合
     */
    Set<DirectedEdge<T>> edges();

    /**
     * <pre>
     * 给定顶点的前驱顶点
     * </pre>
     * @param vertex 顶点
     * @return 前驱顶点集合
     */
    Set<T> predecessors(T vertex);

    /**
     * <pre>
     * 给定顶点的后继节点
     * </pre>
     * @param vertex 顶点
     * @return 后继顶点集合
     */
    Set<T> successors(T vertex);

    /**
     * <pre>
     *　添加一个节点如果它不存在于此图中
     * 次节点必须不为空，且唯一不重复
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 添加成功时
     */
    boolean addVertex(T vertex);

    /**
     * <pre>
     *　从图中删除一个给定的节点
     * 并将与其关联的边一并删除
     * 如果节点不存在于此图中，则默认返回false
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 删除成功时
     */
    boolean removeVertex(T vertex);

    /**
     * <pre>
     *　添加一个从from节点到to接点的边
     * 如果节点不存在于此图中，默认addNode
     * </pre>
     *
     * @param from 顶点
     * @param to 顶点
     * @return {@code true} 添加成功时
     */
    boolean putEdge(T from, T to);

    /**
     * <pre>
     *　删除一个从from节点到to接点的边
     * 如果边不存在于此图中，则默认返回false
     * </pre>
     *
     * @param from 顶点
     * @param to 顶点
     * @return {@code true} 删除成功时
     */
    boolean removeEdge(T from, T to);

    /**
     * <pre>
     * 如果顶点存在于此图中，则返回true
     * 如果顶点不存在于此图中，则默认返回false
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 顶点存在时
     */
    boolean containsVertex(T vertex);

    /**
     * <pre>
     * 如果边存在于此图中，则默认返回true
     * 如果边不存在于此图中，则默认返回false
     * </pre>
     *
     * @param from 顶点
     * @param to 顶点
     * @return {@code true} 边存在时
     */
    boolean containsEdge(T from, T to);
}
