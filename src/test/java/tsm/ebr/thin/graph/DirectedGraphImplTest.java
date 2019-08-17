package tsm.ebr.thin.graph;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class DirectedGraphImplTest {

    /**
     * 测试添加顶点
     */
    @Test
    public void  DirectedGraphImpl_Test01() {
        DirectedGraph<Integer> graph = GraphBuilder.directed()
                .setAllowsSelfLoops(false).setInsertionOrder(true).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        graph.putEdge(1,5);
        graph.putEdge(3,5);
        Assert.assertTrue(graph.containsVertex(3));
        Assert.assertFalse(graph.containsVertex(10));
        Assert.assertTrue(graph.vertexes().size() == 5);
        Assert.assertTrue(graph.edges().size() == 5);
    }

    /**
     * 测试添加边
     */
    @Test
    public void  DirectedGraphImpl_Test02() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        graph.putEdge(1,5);
        graph.putEdge(3,5);
        Assert.assertTrue(graph.containsEdge(1,5));
        Assert.assertFalse(graph.containsEdge(1,3));
    }

    /**
     * 测试没添加顶点直接添加边
     */
    @Test
    public void  DirectedGraphImpl_Test03() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        Assert.assertTrue(graph.containsVertex(1));
        Assert.assertTrue(graph.containsVertex(2));
        Assert.assertTrue(graph.containsVertex(3));
        Assert.assertTrue(graph.containsVertex(4));
        Assert.assertTrue(graph.containsEdge(1,4));
        Assert.assertTrue(graph.containsEdge(2,3));
        Assert.assertTrue(graph.vertexes().size() == 4);
        Assert.assertTrue(graph.edges().size() == 2);
    }

    /**
     * 测试添加顶点后删除顶点
     */
    @Test
    public void  DirectedGraphImpl_Test04() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        graph.removeVertex(3);
        Assert.assertTrue(graph.containsVertex(1));
        Assert.assertTrue(graph.containsVertex(2));
        Assert.assertFalse(graph.containsVertex(3));
        Assert.assertTrue(graph.containsVertex(4));
        Assert.assertTrue(graph.containsEdge(1,4));
        Assert.assertTrue(graph.containsEdge(2,4));
        Assert.assertFalse(graph.containsEdge(2,3));
        Assert.assertTrue(graph.vertexes().size() == 3);
        Assert.assertTrue(graph.edges().size() == 2);
    }

    /**
     * 测试添加顶点和边后删除边
     */
    @Test
    public void  DirectedGraphImpl_Test05() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        graph.removeEdge(2,3);
        Assert.assertTrue(graph.containsVertex(1));
        Assert.assertTrue(graph.containsVertex(2));
        Assert.assertTrue(graph.containsVertex(3));
        Assert.assertTrue(graph.containsVertex(4));
        Assert.assertTrue(graph.containsEdge(1,4));
        Assert.assertFalse(graph.containsEdge(2,3));
        Assert.assertTrue(graph.containsEdge(2,4));
        Assert.assertTrue(graph.vertexes().size() == 4);
        Assert.assertTrue(graph.edges().size() == 2);
    }

    /**
     * 测试删除不存在的顶点和边
     */
    @Test
    public void  DirectedGraphImpl_Test06() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        Assert.assertFalse(graph.containsVertex(10));
        Assert.assertFalse(graph.removeVertex(10));
        Assert.assertFalse(graph.containsEdge(10,3));
        Assert.assertFalse(graph.removeEdge(10,3));
        Assert.assertTrue(graph.vertexes().size() == 4);
        Assert.assertTrue(graph.edges().size() == 3);
    }

    /**
     * 测试重复添加顶点和边
     */
    @Test
    public void  DirectedGraphImpl_Test07() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(3);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        Assert.assertTrue(graph.vertexes().size() == 4);
        Assert.assertTrue(graph.edges().size() == 3);
    }

    /**
     * 测试重复删除顶点和边
     */
    @Test
    public void  DirectedGraphImpl_Test08() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(2,4);
        Assert.assertTrue(graph.vertexes().size() == 4);
        Assert.assertTrue(graph.edges().size() == 3);
        graph.removeVertex(2);
        Assert.assertTrue(graph.vertexes().size() == 3);
        Assert.assertTrue(graph.edges().size() == 1);
        graph.removeVertex(2);
        graph.removeEdge(2,3);
        Assert.assertTrue(graph.vertexes().size() == 3);
        Assert.assertTrue(graph.edges().size() == 1);
    }

    /**
     * 测试出现自环
     */
    @Test
    public void  DirectedGraphImpl_Test10() {
        boolean selfLoop = false;
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(4,3);
        try {
            graph.putEdge(3, 1);
        } catch (IllegalArgumentException ex) {
            selfLoop = true;
        }
        Assert.assertTrue(selfLoop);
    }

    /**
     * 测试未出现自环
     */
    @Test
    public void  DirectedGraphImpl_Test11() {
        boolean selfLoop = false;
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(4,3);
        try {
            graph.putEdge(3, 5);
        } catch (IllegalArgumentException ex) {
            selfLoop = true;
        }
        Assert.assertFalse(selfLoop);
    }

    /**
     * 测试出现无向边
     */
    @Test
    public void  DirectedGraphImpl_Test12() {
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(4,3);
        graph.putEdge(3,2);
        Assert.assertTrue(graph.vertexes().size() == 5);
        Assert.assertTrue(graph.edges().size() == 3);
    }

    /**
     * 测试取得前驱节点
     */
    @Test
    public void  DirectedGraphImpl_Test20() {
        boolean selfLoop = false;
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(1,4);
        graph.putEdge(2,3);
        graph.putEdge(4,3);
        graph.putEdge(5,3);
        Set<Integer> pred = graph.predecessors(3);
        Assert.assertFalse(pred.contains(1));
        Assert.assertTrue(pred.contains(2));
        Assert.assertFalse(pred.contains(3));
        Assert.assertTrue(pred.contains(4));
        Assert.assertTrue(pred.contains(5));
    }

    /**
     * 测试取得后继节点
     */
    @Test
    public void  DirectedGraphImpl_Test21() {
        boolean selfLoop = false;
        DirectedGraph<Integer> graph = GraphBuilder.directed().setAllowsSelfLoops(false).build();
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);
        graph.addVertex(5);
        graph.putEdge(2,4);
        graph.putEdge(2,3);
        graph.putEdge(2,5);
        graph.putEdge(5,3);
        Set<Integer> suc = graph.successors(2);
        Assert.assertFalse(suc.contains(1));
        Assert.assertFalse(suc.contains(2));
        Assert.assertTrue(suc.contains(3));
        Assert.assertTrue(suc.contains(4));
        Assert.assertTrue(suc.contains(5));
    }
}
