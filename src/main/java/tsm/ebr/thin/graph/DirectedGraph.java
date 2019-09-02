/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package tsm.ebr.thin.graph;

import java.util.Set;

/**
 * <pre>
 * 有序图
 * 需求很简单
 * - 可以修改顶点，边
 * - 可以获取指定顶点的前驱，后继顶点
 * </pre>
 * @author catforward
 */
public interface DirectedGraph<V> {

    /**
     * <pre>
     * 顶点集合
     * </pre>
     * @return 顶点集合
     */
    Set<V> vertexes();

    /**
     * <pre>
     * 边集合
     * </pre>
     * @return 边集合
     */
    Set<DirectedEdge<V>> edges();

    /**
     * <pre>
     * 给定顶点的前驱顶点
     * </pre>
     * @param vertex 顶点
     * @return 前驱顶点集合
     */
    Set<V> predecessors(V vertex);

    /**
     * <pre>
     * 给定顶点的后继节点
     * </pre>
     * @param vertex 顶点
     * @return 后继顶点集合
     */
    Set<V> successors(V vertex);

    /**
     * <pre>
     *　添加一个节点如果它不存在于此图中
     * 次节点必须不为空，且唯一不重复
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 添加成功时
     */
    boolean addVertex(V vertex);

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
    boolean removeVertex(V vertex);

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
    boolean putEdge(V from, V to);

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
    boolean removeEdge(V from, V to);

    /**
     * <pre>
     * 如果顶点存在于此图中，则返回true
     * 如果顶点不存在于此图中，则默认返回false
     * </pre>
     *
     * @param vertex 顶点
     * @return {@code true} 顶点存在时
     */
    boolean containsVertex(V vertex);

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
    boolean containsEdge(V from, V to);
}
