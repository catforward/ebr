/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.thin.graph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <pre>
 * 有序图
 * 需求很简单
 * - 可以修改顶点，边
 * - 可以获取指定顶点的前驱，后继顶点
 * </pre>
 * @author catforward
 */
public class DirectedGraphImpl<V> implements DirectedGraph<V> {
    private static class EdgeSet<V> {
        final Set<DirectedEdge<V>> in = new CopyOnWriteArraySet<>();
        final Set<DirectedEdge<V>> out = new CopyOnWriteArraySet<>();
    }
    /** 顶点和边集合的映射 */
    private final Map<V, EdgeSet<V>> vertexEdgesMap;
    private final GraphBuilder builder;

    DirectedGraphImpl(GraphBuilder builder) {
        this.builder = builder;
        //if (this.builder.insertionOrder) {
        //    this.vertexEdgesMap = new ConcurrentSkipListMap<>(); //FIXME
        //} else {
            this.vertexEdgesMap = new ConcurrentHashMap<>();
        //}
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("vertex: ");
        sb.append(this.vertexes().toString());
        sb.append(" edges: [");
        for (DirectedEdge<V> edge : this.edges()) {
            sb.append(edge.toString()).append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * <pre>
     * 顶点集合
     * </pre>
     *
     * @return 顶点集合
     */
    @Override
    public Set<V> vertexes() {
        return Set.copyOf(vertexEdgesMap.keySet());
    }

    /**
     * <pre>
     * 边集合
     * </pre>
     *
     * @return 边集合
     */
    @Override
    public Set<DirectedEdge<V>> edges() {
        HashSet<DirectedEdge<V>> set = new HashSet<>();
        for (EdgeSet<V> edgeSet : this.vertexEdgesMap.values()) {
            edgeSet.in.forEach(vDirectedEdge -> {
                if (!set.contains(vDirectedEdge)) {
                    set.add(vDirectedEdge);
                }
            });
            edgeSet.out.forEach(vDirectedEdge -> {
                if (!set.contains(vDirectedEdge)) {
                    set.add(vDirectedEdge);
                }
            });
        }
        return Set.copyOf(set);
    }

    /**
     * <pre>
     * 给定顶点的前驱顶点
     * </pre>
     *
     * @param vertex 顶点
     * @return 前驱顶点集合
     */
    @Override
    public Set<V> predecessors(V vertex) {
        HashSet<V> set = new HashSet<>();
        EdgeSet<V> edgeSet = vertexEdgesMap.getOrDefault(vertex, null);
        if (edgeSet != null) {
            edgeSet.in.forEach(directedEdge -> {
                set.add(directedEdge.source());
            });
        }
        return Set.copyOf(set);
    }

    /**
     * <pre>
     * 给定顶点的后继节点
     * </pre>
     *
     * @param vertex 顶点
     * @return 后继顶点集合
     */
    @Override
    public Set<V> successors(V vertex) {
        HashSet<V> set = new HashSet<>();
        EdgeSet<V> edgeSet = vertexEdgesMap.getOrDefault(vertex, null);
        if (edgeSet != null) {
            edgeSet.out.forEach(directedEdge -> {
                set.add(directedEdge.target());
            });
        }
        return Set.copyOf(set);
    }

    /**
     * <pre>
     * 添加一个节点如果它不存在于此图中
     * 次节点必须不为空，且唯一不重复
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 添加成功时
     */
    @Override
    public boolean addVertex(V vertex) {
        if (!vertexEdgesMap.containsKey(vertex)) {
            vertexEdgesMap.put(vertex, new EdgeSet<>());
            return true;
        }
        return false;
    }

    /**
     * <pre>
     * 从图中删除一个给定的节点
     * 并将与其关联的边一并删除
     * 如果节点不存在于此图中，则默认返回false
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 删除成功时
     */
    @Override
    public boolean removeVertex(V vertex) {
        if (!this.vertexEdgesMap.containsKey(vertex)) {
            return false;
        }
        Set<V> predecessors = this.predecessors(vertex);
        for (V vPre : predecessors) {
            DirectedEdge<V> edge = new DirectedEdge<>(vPre, vertex);
            EdgeSet<V> edgeSet = this.vertexEdgesMap.get(vPre);
            edgeSet.out.remove(edge);
        }
        Set<V> successors = this.successors(vertex);
        for (V vSuc : successors) {
            DirectedEdge<V> edge = new DirectedEdge<>(vertex, vSuc);
            EdgeSet<V> edgeSet = this.vertexEdgesMap.get(vSuc);
            edgeSet.in.remove(edge);
        }
        EdgeSet<V> edgeSet = this.vertexEdgesMap.get(vertex);
        edgeSet.in.clear();
        edgeSet.out.clear();
        this.vertexEdgesMap.remove(vertex);
        return true;
    }

    /**
     * <pre>
     * 添加一个从from节点到to接点的边
     * 如果节点不存在于此图中，默认addNode
     * </pre>
     *
     * @param from 顶点
     * @param to   顶点
     * @return {@code true} 添加成功时
     */
    @Override
    public boolean putEdge(V from, V to) {
        DirectedEdge<V> edge = new DirectedEdge<>(from, to);
        EdgeSet<V> fromEdgeSet = vertexEdgesMap.getOrDefault(from, null);
        EdgeSet<V> toEdgeSet = vertexEdgesMap.getOrDefault(to, null);
        if (fromEdgeSet == null) {
            fromEdgeSet = new EdgeSet<>();
            vertexEdgesMap.put(from, fromEdgeSet);
        }
        if (toEdgeSet == null) {
            toEdgeSet = new EdgeSet<>();
            vertexEdgesMap.put(to, toEdgeSet);
        }
        if (toEdgeSet.out.contains(new DirectedEdge<>(to, from))) {
           return false;
        } else {
            fromEdgeSet.out.add(edge);
            toEdgeSet.in.add(edge);
        }
        if (!this.builder.allowsSelfLoops) {
            selfLoopsCheck();
        }
        return true;
    }

    /**
     * <pre>
     * 删除一个从from节点到to接点的边
     * 如果边不存在于此图中，则默认返回true
     * </pre>
     *
     * @param from 顶点
     * @param to   顶点
     * @return {@code true} 删除成功时
     */
    @Override
    public boolean removeEdge(V from, V to) {
        boolean delFlg = false;
        DirectedEdge<V> edge = new DirectedEdge<>(from, to);
        EdgeSet<V> edgeSet = vertexEdgesMap.getOrDefault(from, null);
        if (edgeSet != null && edgeSet.out.contains(edge)) {
            edgeSet.out.remove(edge);
            delFlg = true;
        }
        edgeSet = vertexEdgesMap.getOrDefault(to, null);
        if (edgeSet != null && edgeSet.in.contains(edge)) {
            edgeSet.in.remove(edge);
            delFlg = true;
        }
        return delFlg;
    }

    /**
     * <pre>
     * 如果顶点存在于此图中，则返回true
     * 如果顶点不存在于此图中，则默认返回false
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 顶点存在时
     */
    @Override
    public boolean containsVertex(V vertex) {
        return this.vertexEdgesMap.containsKey(vertex);
    }

    /**
     * <pre>
     * 如果边存在于此图中，则默认返回true
     * 如果边不存在于此图中，则默认返回false
     * </pre>
     *
     * @param from 顶点
     * @param to   顶点
     * @return {@code true} 边存在时
     */
    @Override
    public boolean containsEdge(V from, V to) {
        DirectedEdge<V> edge = new DirectedEdge<>(from, to);
        for (EdgeSet<V> edgeSet : this.vertexEdgesMap.values()) {
            if (edgeSet.in.contains(edge) || edgeSet.out.contains(edge)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <pre>
     * 检查是否存在自环
     * </pre>
     * @throws IllegalArgumentException 当出现自环时抛出错误参数异常
     */
    private void selfLoopsCheck() {
        for (var entry : this.vertexEdgesMap.entrySet()) {
            DFSCheck(entry.getKey(), entry.getValue());
        }
    }

    /**
     * <pre>
     *　查找边集合中会不会回到起始顶点
     * </pre>
     * @param vertex 起始顶点
     * @param edgeSet　需要检查的边集合
     * @throws IllegalArgumentException 当某条边的终点等于起始顶点时视为发生自环
     */
    private void DFSCheck(V vertex, EdgeSet<V> edgeSet) {
        for (DirectedEdge<V> edge : edgeSet.out) {
            if (vertex.equals(edge.target())) {
                throw new IllegalArgumentException("self loop");
            } else {
                DFSCheck(vertex, this.vertexEdgesMap.get(edge.target()));
            }
        }
    }

}
