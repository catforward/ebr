package tsm.ebr.thin.graph;

import java.util.Objects;

import static tsm.ebr.util.MiscUtils.checkNotNull;

/**
 * 有向边
 * @author catforward
 */
class DirectedEdge<V> {
    final V vertexFrom;
    final V vertexTo;

    DirectedEdge(V vertexFrom, V vertexTo) {
        checkNotNull(vertexFrom);
        checkNotNull(vertexTo);
        if (vertexFrom.equals(vertexTo)) {
            throw new IllegalArgumentException("起点和终点不能相等");
        }
        this.vertexFrom = vertexFrom;
        this.vertexTo = vertexTo;
    }

    V source() {
        return this.vertexFrom;
    }

    V target() {
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
