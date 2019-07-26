package tsm.ebr.task.manager;

import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Service;

public class StateManageService extends Service {

    public StateManageService() {
        super();
        StateHolder.init();
    }

    @Override
    public ServiceId id() {
        return ServiceId.TASK_STATE_MANAGENT;
    }

    @Override
    protected void onInit() {
        // 当Meta数据生成后，包装其成为Unit单元树
        registerActionHandler(Symbols.EVT_ACT_TASK_META_CREATED,
                TaskBuildHandler.class);
        // 当生成Flow图之后，启动该Flow
        registerActionHandler(Symbols.EVT_ACT_LAUNCH_TASK_FLOW,
                TaskLaunchHandler.class);
        // 任务状态变更时，检查其前驱后置以及父节点状态
        registerActionHandler(Symbols.EVT_ACT_TASK_UNIT_STATE_CHANGED,
                StateUpdateHandler.class,
                TaskLaunchHandler.class);
    }

    @Override
    protected void onFinish() {
        unregister(Symbols.EVT_ACT_TASK_META_CREATED);
        unregister(Symbols.EVT_ACT_LAUNCH_TASK_FLOW);
        unregister(Symbols.EVT_ACT_TASK_UNIT_STATE_CHANGED);
    }
}
