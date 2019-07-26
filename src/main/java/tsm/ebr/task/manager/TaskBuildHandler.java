package tsm.ebr.task.manager;

import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.Type;
import tsm.ebr.task.manager.Item.Flow;
import tsm.ebr.task.manager.Item.Unit;

import static tsm.ebr.base.Task.Symbols.KEY_ROOT_UNIT;


public class TaskBuildHandler implements IHandler {

    /**
     * @param context 上下文
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        Meta meta = (Meta) context.getParam(KEY_ROOT_UNIT);
        Unit rootUnit = Unit.copyOf(meta);
        addTaskUnit(rootUnit);
        addTaskFlow(rootUnit);
        context.setNextAction(Symbols.EVT_ACT_LAUNCH_TASK_FLOW);
        context.addHandlerResult(Symbols.EVT_DATA_TASK_FLOW_URL,
                StateHolder.getRootFlow().rootUnit.url);
        return true;
    }

    private void addTaskUnit(Unit unit) {
        StateHolder.addUnit(unit);
        for (Unit child : unit.children) {
            StateHolder.addUnit(child);
            if (!child.children.isEmpty()) {
                addTaskUnit(child);
            }
        }
    }

    private void addTaskFlow(Unit unit) {
        Flow flow = Flow.makeFrom(unit);
        flow.standby();
        StateHolder.addFlow(flow);
        for (Unit child : unit.children) {
            if (Type.MODULE == child.type) {
                addTaskFlow(child);
            }
        }
    }
}
