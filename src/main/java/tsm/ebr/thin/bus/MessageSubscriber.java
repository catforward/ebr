package tsm.ebr.thin.bus;

/**
 * 消息订阅者
 * @author catforward
 */
public interface MessageSubscriber<M> {

    /**
     * 接受消息
     * @param message 消息体
     */
    void onMessage(M message);
}
