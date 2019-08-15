package tsm.ebr.thin.graph;

/**
 * 图工厂
 * @author catforward
 */
public class GraphBuilder {
    boolean allowsSelfLoops = false;

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

    public <V> DirectedGraph<V> build() {
        return new DirectedGraphImpl<V>(this);
    }
}
