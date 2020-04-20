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
package pers.ebr.server.base;

import org.junit.Assert;
import org.junit.Test;
import pers.ebr.server.base.graph.DirectedGraph;
import pers.ebr.server.base.graph.GraphBuilder;

import java.util.Set;

/**
 * 图测试
 * @author l.gong
 */
public class DirectedGraphImplTest {

    /**
     * <pre>
     * 测试添加顶点
     * </pre>
     */
    @Test
    public void  DirectedGraphImpl_Test01() {
        DirectedGraph<Integer> graph = GraphBuilder.directed()
                .setAllowsSelfLoops(false).build();
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
     * <pre>
     * 测试添加边
     * </pre>
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
     * <pre>
     * 测试没添加顶点直接添加边
     * </pre>
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
     * <pre>
     * 测试添加顶点后删除顶点
     * </pre>
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
     * <pre>
     * 测试添加顶点和边后删除边
     * </pre>
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
     * <pre>
     * 测试删除不存在的顶点和边
     * </pre>
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
     * <pre>
     * 测试重复添加顶点和边
     * </pre>
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
     * <pre>
     * 测试重复删除顶点和边
     * </pre>
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
     * <pre>
     * 测试出现自环
     * </pre>
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
     * <pre>
     * 测试未出现自环
     * </pre>
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
     * <pre>
     * 测试出现无向边
     * </pre>
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
     * <pre>
     * 测试取得前驱节点
     * </pre>
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
     * <pre>
     * 测试取得后继节点
     * </pre>
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