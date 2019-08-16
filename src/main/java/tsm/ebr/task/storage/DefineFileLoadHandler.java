/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.task.storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import tsm.ebr.base.Const;
import tsm.ebr.base.Message.Symbols;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Unit;
import tsm.ebr.base.Task.Type;
import tsm.ebr.util.ConfigUtils;
import tsm.ebr.util.LogUtils;
import tsm.ebr.util.PathUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static tsm.ebr.base.Message.Symbols.MSG_DATA_TASK_ROOT_UNIT;
import static tsm.ebr.base.Task.Symbols.*;
import static tsm.ebr.util.ConfigUtils.Item.KEY_INSTANT_TASK;

/**
 * 从磁盘读取任务流配置文件并构建任务定义树
 * @author catforward
 */
public class DefineFileLoadHandler implements IHandler {
    private final static Logger logger = Logger.getLogger(DefineFileLoadHandler.class.getCanonicalName());

    public DefineFileLoadHandler() {}

    /**
     * @param context 上下文
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        String filePath = makeDefineFileFullPath(context);
        Unit rootUnit = loadTaskMetaFromDefineFile(filePath);
        context.setNextAction(Symbols.MSG_ACT_TASK_META_CREATED);
        context.addHandlerResult(MSG_DATA_TASK_ROOT_UNIT, rootUnit);
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
    private Unit loadTaskMetaFromDefineFile(String filePath) {
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
    private Unit parse(JsonNode jsonRoot) {
        HashMap<String, Unit> uidUnitPool = new HashMap<>(Const.INIT_CAP);
        HashMap<String, List<String>> urlPredecessorIdMap = new HashMap<>(Const.INIT_CAP);
        createMetaPool(null, jsonRoot, uidUnitPool, urlPredecessorIdMap);
        Unit rootUnit = uidUnitPool.get(KEY_ROOT_UNIT);
        updatePredecessors(rootUnit, uidUnitPool, urlPredecessorIdMap);
        return rootUnit;
    }

    /**
     *
     */
//    private Map<String, Unit> makeUrlMetaMap(HashMap<String, Unit> uidUnitPool) {
//        HashMap<String, Unit> urlUnitMap = new HashMap<>(Const.INIT_CAP);
//        uidUnitPool.forEach((uid, unit) -> {
//            urlUnitMap.put(unit.url, unit);
//        });
//        urlUnitMap.put(KEY_ROOT_UNIT, uidUnitPool.get(KEY_ROOT_UNIT));
//        return  urlUnitMap;
//    }

    /**
     * <pre>
     * 创建节点树
     * </pre>
     *
     * @param mParent       父节点
     * @param jsonNode     当前json节点
     * @param uidUnitPool 所有节点对象池
     */
    private void createMetaPool(Unit mParent, JsonNode jsonNode,
                                Map<String, Unit> uidUnitPool,
                                Map<String, List<String>> urlPredecessorIdMap) {
        Optional<JsonNode> optValue = Optional.ofNullable(jsonNode.get(KEY_UID));
        if (optValue.isEmpty()) {
            throw new RuntimeException("没有设置uid元素");
        }
        // 使用ID取得或创建Meta
        String uid = optValue.get().asText().trim();
        Unit currentUnit = Optional.ofNullable(uidUnitPool.get(uid)).orElseGet(() -> {
            Unit newUnit = new Unit(uid, mParent);
            uidUnitPool.put(uid, newUnit);
            if (newUnit.parent == null) {
                newUnit.url = String.format("/%s", uid);
                newUnit.type = Type.ROOT;
                uidUnitPool.put(KEY_ROOT_UNIT, newUnit);
            } else {
                newUnit.url = String.format("%s/%s", mParent.url, uid);
                mParent.children.add(newUnit);
            }
            return newUnit;
        });
        // 描述
        optValue = Optional.ofNullable(jsonNode.get(KEY_DESC));
        if (optValue.isPresent()) {
            currentUnit.desc = optValue.get().asText().trim();
        }
        // 命令
        optValue = Optional.ofNullable(jsonNode.get(KEY_COMMAND));
        if (optValue.isPresent()) {
            currentUnit.command = optValue.get().asText().trim();
        }
        // 触发器
        optValue = Optional.ofNullable(jsonNode.get(KEY_PREDECESSORS));
        if (optValue.isPresent()) {
            JsonNode predecessorsNode = optValue.get();
            int size = predecessorsNode.size();
            if (size != 0) {
                List<String> pIds = urlPredecessorIdMap.get(currentUnit.url);
                if (pIds == null) {
                    pIds = new ArrayList<>(Const.INIT_CAP);
                    urlPredecessorIdMap.put(currentUnit.url, pIds);
                }
                for (int idx = 0; idx < size; ++idx) {
                    pIds.add(predecessorsNode.get(idx).asText().trim());
                }
            }
        }
        // 子元素
        optValue = Optional.ofNullable(jsonNode.get(KEY_UNITS));
        if (optValue.isPresent()) {
            int size = optValue.get().size();
            for (int idx = 0; idx < size; ++idx) {
                createMetaPool(currentUnit, optValue.get().get(idx),
                                uidUnitPool, urlPredecessorIdMap);
            }
            if (Type.ROOT != currentUnit.type) {
                currentUnit.type = Type.MODULE;
            }
        } else {
            if (Type.ROOT != currentUnit.type) {
                currentUnit.type = Type.TASK;
            }
        }
    }

    /**
     *
     */
    private void updatePredecessors(Unit unit,
                                    Map<String, Unit> uidUnitPool,
                                    Map<String, List<String>> urlPredecessorIdMap) {
        List<String> pIds = urlPredecessorIdMap.get(unit.url);
        if (pIds != null && !pIds.isEmpty()) {
            pIds.forEach(id -> {
                Unit pUnit = uidUnitPool.get(id);
                if (pUnit != null) {
                    unit.preconditions.add(pUnit);
                }
            });
        }
        for(Unit child : unit.children) {
            updatePredecessors(child, uidUnitPool, urlPredecessorIdMap);
        }
    }
}
