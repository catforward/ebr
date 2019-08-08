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
import tsm.ebr.base.Event.Symbols;
import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.Type;
import tsm.ebr.util.ConfigUtils;
import tsm.ebr.util.LogUtils;
import tsm.ebr.util.PathUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static tsm.ebr.base.Event.Symbols.EVT_DATA_META_MAP;
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
        Map<String, Meta> urlMetaMap = loadTaskMetaFromDefineFile(filePath);
        context.setNextAction(Symbols.EVT_ACT_TASK_META_CREATED);
        context.addHandlerResult(EVT_DATA_META_MAP, urlMetaMap);
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
    private Map<String, Meta> loadTaskMetaFromDefineFile(String filePath) {
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
    private Map<String, Meta> parse(JsonNode jsonRoot) {
        HashMap<String, Meta> uidMetaPool = new HashMap<>(16);
        createMetaPool(null, jsonRoot, uidMetaPool);
        Meta rootMeta = uidMetaPool.get(KEY_ROOT_UNIT);
        updatePredecessors(rootMeta, uidMetaPool);
        return makeUrlMetaMap(uidMetaPool);
    }

    /**
     *
     */
    private Map<String, Meta> makeUrlMetaMap(HashMap<String, Meta> uidMetaPool) {
        HashMap<String, Meta> urlMetaMap = new HashMap<>(16);
        uidMetaPool.forEach((uid, meta) -> {
            urlMetaMap.put(meta.symbols.get(KEY_UNIT_URL), meta);
        });
        urlMetaMap.put(KEY_ROOT_UNIT, uidMetaPool.get(KEY_ROOT_UNIT));
        return  urlMetaMap;
    }

    /**
     * <pre>
     * 创建节点树
     * </pre>
     *
     * @param mParent       父节点
     * @param jsonNode     当前json节点
     * @param uidMetaPool 所有节点对象池
     */
    private void createMetaPool(Meta mParent, JsonNode jsonNode, Map<String, Meta> uidMetaPool) {
        Optional<JsonNode> optValue = Optional.ofNullable(jsonNode.get(KEY_UID));
        if (optValue.isEmpty()) {
            throw new RuntimeException("没有设置uid元素");
        }
        // 使用ID取得或创建Meta
        String uid = optValue.get().asText().trim();
        Meta currentUnit = Optional.ofNullable(uidMetaPool.get(uid)).orElseGet(() -> {
            Meta newMeta = new Meta();
            uidMetaPool.put(uid, newMeta);
            newMeta.symbols.put(KEY_UID, uid);
            if (mParent == null) {
                newMeta.symbols.put(KEY_UNIT_URL, String.format("/%s", uid));
                newMeta.symbols.put(KEY_UNIT_TYPE, Type.ROOT.name());
                uidMetaPool.put(KEY_ROOT_UNIT, newMeta);
            } else {
                newMeta.symbols.put(KEY_UNIT_URL, String.format("%s/%s", mParent.symbols.get(KEY_UNIT_URL), uid));
                newMeta.symbols.put(KEY_PARENT_UNIT_URL, mParent.symbols.get(KEY_UNIT_URL));
                newMeta.parent = mParent;
                mParent.children.add(newMeta);
            }
            return newMeta;
        });
        // 描述
        optValue = Optional.ofNullable(jsonNode.get(KEY_DESC));
        if (optValue.isPresent()) {
            currentUnit.symbols.put(KEY_DESC, optValue.get().asText().trim());
        }
        // 命令
        optValue = Optional.ofNullable(jsonNode.get(KEY_COMMAND));
        if (optValue.isPresent()) {
            currentUnit.symbols.put(KEY_COMMAND, optValue.get().asText().trim());
        }
        // 触发器
        optValue = Optional.ofNullable(jsonNode.get(KEY_PREDECESSORS));
            if (optValue.isPresent()) {
            JsonNode predecessorsNode = optValue.get();
            int size = predecessorsNode.size();
            if (size != 0) {
                for (int idx = 0; idx < size; ++idx) {
                    // 暂时放uid
                    currentUnit.predecessorUrl.add(predecessorsNode.get(idx).asText().trim());
                }
            }
        }
        // 子元素
        optValue = Optional.ofNullable(jsonNode.get(KEY_UNITS));
        if (optValue.isPresent()) {
            int size = optValue.get().size();
            for (int idx = 0; idx < size; ++idx) {
                createMetaPool(currentUnit, optValue.get().get(idx), uidMetaPool);
            }
            if (!Type.ROOT.name().equals(currentUnit.symbols.get(KEY_UNIT_TYPE))) {
                currentUnit.symbols.put(KEY_UNIT_TYPE, Type.MODULE.name());
            }
        } else {
            if (!Type.ROOT.name().equals(currentUnit.symbols.get(KEY_UNIT_TYPE))) {
                currentUnit.symbols.put(KEY_UNIT_TYPE, Type.TASK.name());
            }
        }
    }

    /**
     *
     */
    private void updatePredecessors(Meta meta, Map<String, Meta> uidMetaPool) {
        if (!meta.predecessorUrl.isEmpty()) {
            ArrayList<String> newUrlList = new ArrayList<>(meta.predecessorUrl.size());
            for (String pUid : meta.predecessorUrl) {
                Meta pMeta = uidMetaPool.get(pUid);
                if (pMeta != null) {
                    newUrlList.add(pMeta.symbols.get(KEY_UNIT_URL));
                }
            }
            // replace the uid by url
            meta.predecessorUrl.clear();
            meta.predecessorUrl.addAll(newUrlList);
        }
        for(Meta child : meta.children) {
            updatePredecessors(child, uidMetaPool);
        }
    }
}
