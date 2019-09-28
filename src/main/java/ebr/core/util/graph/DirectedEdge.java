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
package ebr.core.util.graph;

import java.util.Objects;

import static ebr.core.util.MiscUtils.checkNotNull;

/**
 * <pre>
 * 有向边
 * </pre>
 * @author catforward
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
