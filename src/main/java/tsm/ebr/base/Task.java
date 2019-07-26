package tsm.ebr.base;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 跨模块使用的公共数据结构以及常量定义
 * @author catforward
 */
public final class Task {

    public enum State {
        SUCCEEDED, ERROR, STANDBY, RUNNING;
    }

    public enum Type {
        ROOT, TASK, MODULE;
    }

    public static class Symbols {
        public final static String KEY_ROOT_UNIT = "root_unit";
        public final static String KEY_UID = "uid";
        public final static String KEY_DESC = "desc";
        public final static String KEY_COMMAND = "command";
        public final static String KEY_UNITS = "units";
        public final static String KEY_PREDECESSORS_LIST = "predecessors";
    }

    /**
     * <pre>
     *     封装JSON定义的原始数据
     * </pre>
     */
    public static class Meta {
        public final HashMap<String, Object> raw;
        public final ArrayList<Meta> children;
        public final ArrayList<Meta> predecessors;
        public final Meta parent;
        public final String url;
        public Type type;

        public Meta(String unitId, Meta parentTask) {
            parent = parentTask;
            url = (parent == null) ?
                    String.format("/%s", unitId) :
                    String.format("%s/%s", parent.url, unitId);
            children = new ArrayList<>();
            predecessors = new ArrayList<>();
            raw = new HashMap<>();
            raw.put(Symbols.KEY_UID, unitId);
        }
    }

    public static class PerformableTask {
        public final String url;
        public final String command;

        public PerformableTask(String newUrl, String newCmd) {
            url = newUrl;
            command = newCmd;
        }
    }
}
