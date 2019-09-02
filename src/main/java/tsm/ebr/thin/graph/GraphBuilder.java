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

/**
 * <pre>
 * 图工厂
 * </pre>
 * @author catforward
 */
public class GraphBuilder {
    boolean allowsSelfLoops = false;
    boolean insertionOrder = false;

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

    public GraphBuilder setInsertionOrder(boolean insertionOrder) {
        this.insertionOrder = insertionOrder;
        return this;
    }

    public <V> DirectedGraph<V> build() {
        return new DirectedGraphImpl<>(this);
    }
}
