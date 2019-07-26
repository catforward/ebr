package tsm.ebr.task.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.Type;
import tsm.ebr.utils.ConfigUtils;
import tsm.ebr.utils.LogUtils;
import tsm.ebr.utils.PathUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static tsm.ebr.base.Task.Symbols.*;
import static tsm.ebr.utils.ConfigUtils.Item.KEY_INSTANT_TASK;

public class MetaLoadHandler implements IHandler {
    private final static Logger logger = Logger.getLogger(MetaLoadHandler.class.getCanonicalName());

    public MetaLoadHandler() {}

    /**
     * @param context 上下文
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        String filePath = makeDefineFileFullPath(context);
        Meta rootMeta = loadTaskMetaFromDefineFile(filePath);
        context.setNextAction(Symbols.EVT_ACT_TASK_META_CREATED);
        context.addHandlerResult(KEY_ROOT_UNIT, rootMeta);
        return true;
    }


    /**
     * <pre>
     * 生成一个Task定义文件的完整路径
     * </pre>
     *
     * @param context
     */
    private String makeDefineFileFullPath(HandlerContext context) {
        Optional<String> strVal = Optional.ofNullable(ConfigUtils.get(KEY_INSTANT_TASK));
        if (strVal.isEmpty()) {
            throw new RuntimeException("没有发现Task定义文件的路径");
        }
        String filePath = strVal.get();
        return filePath.startsWith("/") ? filePath : PathUtils.getDefPath() + File.separator + filePath;
    }


    /**
     * <pre>
     * 从定义文件创建一个任务节点树
     * </pre>
     *
     * @param filePath Task定义文件的完整路径
     */
    private Meta loadTaskMetaFromDefineFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            return parse(initObjectMapper().readTree(reader));
        } catch (IOException ex) {
            LogUtils.dumpError(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * <pre>
     * 初始化JSON解析器
     * </pre>
     *
     * @return ObjectMapper
     */
    private ObjectMapper initObjectMapper() {
        return new ObjectMapper()
                // 如果为空则不输出
                // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                // 对于空的对象转json的时候不抛出错误
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 禁用序列化日期为timestamps
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 禁用遇到未知属性抛出异常
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 视空字符传为null
                // objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
                // 允许注释
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                // 允许属性名称没有引号
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                // 允许单引号
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                // 取消对非ASCII字符的转码
                .configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
    }

    /**
     * <pre>
     * 创建一个任务节点树
     * </pre>
     *
     * @param jsonRoot Task定义文件转换后的json根节点
     * @return Node
     */
    private Meta parse(JsonNode jsonRoot) {
        HashMap<String, Meta> uidMetaPool = new HashMap<>();
        createTaskUnitTree(null, jsonRoot, uidMetaPool);
        Meta rootMeta = uidMetaPool.get(KEY_ROOT_UNIT);
        updatePredecessors(rootMeta, uidMetaPool);
        uidMetaPool.clear();
        return rootMeta;
    }

    /**
     * <pre>
     * 创建节点树
     * </pre>
     *
     * @param parent       父节点
     * @param jsonNode     当前json节点
     * @param uidMetaPool 所有节点对象池
     */
    private void createTaskUnitTree(Meta parent, JsonNode jsonNode, Map<String, Meta> uidMetaPool) {
        Optional<JsonNode> optValue = Optional.ofNullable(jsonNode.get(KEY_UID));
        if (optValue.isEmpty()) {
            throw new RuntimeException("没有设置uid元素");
        }
        // 使用ID取得或创建Meta
        String uid = optValue.get().asText().trim();
        Meta currentUnit = Optional.ofNullable(uidMetaPool.get(uid)).orElseGet(() -> {
            Meta newMeta = new Meta(uid, parent);
            uidMetaPool.put(uid, newMeta);
            if (newMeta.parent == null) {
                newMeta.type = Type.ROOT;
                uidMetaPool.put(KEY_ROOT_UNIT, newMeta);
            } else {
                parent.children.add(newMeta);
            }
            return newMeta;
        });
        // 描述
        optValue = Optional.ofNullable(jsonNode.get(KEY_DESC));
        if (optValue.isPresent()) {
            currentUnit.raw.put(KEY_DESC, optValue.get().asText().trim());
        }
        // 命令
        optValue = Optional.ofNullable(jsonNode.get(KEY_COMMAND));
        if (optValue.isPresent()) {
            currentUnit.raw.put(KEY_COMMAND, optValue.get().asText().trim());
        }
        // 触发器
        optValue = Optional.ofNullable(jsonNode.get(KEY_PREDECESSORS_LIST));
            if (optValue.isPresent()) {
            JsonNode predecessorsNode = optValue.get();
            int size = predecessorsNode.size();
            if (size != 0) {
                ArrayList<String> predecessorList = new ArrayList<>(size);
                for (int idx = 0; idx < size; ++idx) {
                    predecessorList.add(predecessorsNode.get(idx).asText().trim());
                }
                currentUnit.raw.put(KEY_PREDECESSORS_LIST, predecessorList);
            }
        }
        // 子元素
        optValue = Optional.ofNullable(jsonNode.get(KEY_UNITS));
        if (optValue.isPresent()) {
            if (currentUnit.parent != null && Type.MODULE == currentUnit.parent.type) {
                throw new RuntimeException("MODULE以下TASK不允许拥有子TASK（只允许一层MODULE）");
            }
            int size = optValue.get().size();
            for (int idx = 0; idx < size; ++idx) {
                createTaskUnitTree(currentUnit, optValue.get().get(idx), uidMetaPool);
            }
            // 类型
            if (Type.ROOT != currentUnit.type) {
                currentUnit.type = Type.MODULE;
            }
        } else {
            // 类型
            if (Type.ROOT != currentUnit.type) {
                currentUnit.type = Type.TASK;
            }
        }
    }

    private void updatePredecessors(Meta meta, Map<String, Meta> uidMetaPool) {
        List<String> plist = (List<String>) meta.raw.get(KEY_PREDECESSORS_LIST);
        if (plist != null && !plist.isEmpty()) {
            for (String pUid : plist) {
                Meta pMeta = uidMetaPool.get(pUid);
                if (pMeta != null) {
                    meta.predecessors.add(pMeta);
                }
            }
        }
        for(Meta child : meta.children) {
            updatePredecessors(child, uidMetaPool);
        }
    }
}
