package tsm.ebr.task.persistence;

import tsm.ebr.base.Service;

import static tsm.ebr.base.Event.Symbols.EVT_ACT_LOAD_DEF_FILE;

public class PersistenceService extends Service {

    public PersistenceService() {
        super();
    }

    @Override
    public ServiceId id() {
        return ServiceId.TASK_META_PERSISTENCE;
    }

    @Override
    protected void onInit() {
        // 从定义文件读取任务定义
        registerActionHandler(EVT_ACT_LOAD_DEF_FILE, MetaLoadHandler.class);
    }

    @Override
    protected void onFinish() {
        unregister(EVT_ACT_LOAD_DEF_FILE);
    }
}
