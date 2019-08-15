package tsm.ebr.thin.graph;

import java.util.Set;

/**
 * 有序图
 * 需求很简单
 * - 可以修改顶点，边
 * - 可以获取指定顶点的前驱，后继顶点
 * @author catforward
 */
public interface DirectedGraph<V> {

    /**
     * 顶点集合
     * @return 顶点集合
     */
    Set<V> vertexes();

    /**
     * 边集合
     * @return 边集合
     */
    Set<DirectedEdge<V>> edges();

    /**
     * 给定顶点的前驱顶点
     * @param vertex 顶点
     * @return 前驱顶点集合
     */
    Set<V> predecessors(V vertex);

    /**
     * 给定顶点的后继节点
     * @param vertex 顶点
     * @return 后继顶点集合
     */
    Set<V> successors(V vertex);

    /**
     *　添加一个节点如果它不存在于此图中
     * 次节点必须不为空，且唯一不重复
     *
     * @param vertex 顶点
     * @return {@code true} 添加成功时
     */
    boolean addVertex(V vertex);

    /**
     *　从图中删除一个给定的节点
     * 并将与其关联的边一并删除
     * 如果节点不存在于此图中，则默认返回false
     *
     * @param vertex 顶点
     * @return {@code true} 删除成功时
     */
    boolean removeVertex(V vertex);

    /**
     *　添加一个从from节点到to接点的边
     * 如果节点不存在于此图中，默认addNode
     *
     * @param from 顶点
     * @param to 顶点
     * @return {@code true} 添加成功时
     */
    boolean putEdge(V from, V to);

    /**
     *　删除一个从from节点到to接点的边
     * 如果边不存在于此图中，则默认返回false
     *
     * @param from 顶点
     * @param to 顶点
     * @return {@code true} 删除成功时
     */
    boolean removeEdge(V from, V to);

    /**
     * 如果顶点存在于此图中，则返回true
     * 如果顶点不存在于此图中，则默认返回false
     *
     * @param vertex 顶点
     * @return {@code true} 顶点存在时
     */
    boolean containsVertex(V vertex);

    /**
     * 如果边存在于此图中，则默认返回true
     * 如果边不存在于此图中，则默认返回false
     *
     * @param from 顶点
     * @param to 顶点
     * @return {@code true} 边存在时
     */
    boolean containsEdge(V from, V to);
}
