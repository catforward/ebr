package tsm.ebr.task.manager;

import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Task.State;

public class StateUpdateHandler implements IHandler {
    @Override
    public boolean doHandle(HandlerContext context) {
        String url = (String) context.getParam(Symbols.EVT_DATA_TASK_UNIT_URL);
        State state = (State) context.getParam(Symbols.EVT_DATA_TASK_UNIT_NEW_STATE);
        StateHolder.getUnit(url).updateState(state);
        // 如果任意一个unit执行错误
        // 或者顶层flow都完成了
        // 则通知应用程序退出
        if (State.ERROR == state
                || (State.SUCCEEDED == state && StateHolder.getRootFlow().isComplete())) {
            context.setNoticeAction(Symbols.EVT_ACT_SERVICE_SHUTDOWN);
            return false;
        }
        return true;
    }
}
