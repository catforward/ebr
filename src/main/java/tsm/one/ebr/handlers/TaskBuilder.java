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
package tsm.one.ebr.handlers;

import static tsm.one.ebr.base.Handler.HandlerEvent.Const.ACT_LOAD_DEF_FILE;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.ACT_MANAGEMENT_APPEND;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_PATH;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_GRAPH;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.FLG;
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.FLG_AUTO_START;
import static tsm.one.ebr.base.HandlerId.TASK_APP;
import static tsm.one.ebr.base.HandlerId.TASK_BUILDER;
import static tsm.one.ebr.base.HandlerId.TASK_MANAGER;
import static tsm.one.ebr.base.utils.ConfigUtils.Item.KEY_INSTANT_TASK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.Subscribe;

import tsm.one.ebr.base.Application;
import tsm.one.ebr.base.Handler;
import tsm.one.ebr.base.data.TaskGraph;
import tsm.one.ebr.base.data.TaskUnit;
import tsm.one.ebr.base.data.TaskUnit.Symbols;
import tsm.one.ebr.base.data.TaskUnit.Type;
import tsm.one.ebr.base.utils.ConfigUtils;
import tsm.one.ebr.base.utils.PathUtils;

/**
 * <pre>
 * 任务构建处理类
 * - 从json的定义文件构建一个tasknet对象
 * - 对构建的tasknet对象进行以下验证
 *  - 必须的定义项是否完整定义
 *  - 存在逻辑先后关系的task是否存在
 *  - 逻辑图不允许出现自环
 * - tasknet构建成功后通过事件总线通知任务管理处理类
 * </pre>
 * 
 * @author catforward
 */
public class TaskBuilder extends Handler {
	private final Logger logger = Logger.getLogger(TaskBuilder.class.getName());

	public TaskBuilder(Application app) {
		super(app);
	}

	@Override
	protected void onInit() {
		application.getEventBus().register(this);
		logger.fine("init OK");
	}

	@Override
	protected void onStart() {
		// 通知自己读取定义文件
		postMessage(new HandlerEvent()
				.setSrc(TASK_BUILDER)
				.setDst(TASK_BUILDER)
				.setAct(ACT_LOAD_DEF_FILE));
		logger.fine("start OK");
	}

	@Override
	protected void onFinish() {
		application.getEventBus().unregister(this);
		logger.fine("finish OK");
	}

	/**
	 * 处理以下事件
	 *  - 目标为当前处理类的事件
	 *  - 广播事件
	 * @param event 事件类实例
	 */
	@Subscribe
	public void onHandlerEvent(HandlerEvent event) {
		if (TASK_APP != event.getDst() && TASK_BUILDER != event.getDst()) {
			return;
		}
		try {
			switch (event.getAct()) {
			case ACT_LOAD_DEF_FILE: {
				onLoadDefineFile(event);
				break;
			}
			case ACT_SERV_SHUTDOWN: {
				finish();
				break;
			}
			default:
				break;
			}
		} catch (Exception ex) {
			logger.severe(ex.getLocalizedMessage());
			finishNoticeFrom(TASK_BUILDER);
		}
	}

	/**
	 * <pre>
	 * 构建一个TaskNet实例
	 * 构建成功时立即启动此TaskNet
	 * </pre>
	 * 
	 * @param event 事件类实例
	 * @throws IOException
	 */
	private void onLoadDefineFile(HandlerEvent event) throws IOException {
		String filePath = makeDefFileFullPath(event);

		Map<String, TaskUnit> unitPool = TaskItemBuilder.loadUnitFromDefineFile(filePath);
		if (!TaskValidator.validateUnitDefine(unitPool)) {
			logger.info("Task的JSON(NodeTree)定义不合法");
			finishNoticeFrom(TASK_BUILDER);
			return;
		}

		TaskGraph taskGraph = new TaskGraph(unitPool.get(Symbols.KEY_ROOT_UNIT));
		try {
			taskGraph.build(unitPool);
		} catch (Exception ex) {
			logger.info("Task的JSON(Net)定义不合法");
			finishNoticeFrom(TASK_BUILDER);
			return;
		}

		postMessage(new HandlerEvent()
				.setSrc(TASK_BUILDER)
				.setDst(TASK_MANAGER)
				.setAct(ACT_MANAGEMENT_APPEND)
				.addParam(FLG, FLG_AUTO_START)
				.addParam(DATA_TASK_GRAPH, taskGraph));
	}

	/**
	 * <pre>
	 * 生成一个Task定义文件的完整路径
	 * </pre>
	 * 
	 * @param event 事件类实例
	 */
	private String makeDefFileFullPath(HandlerEvent event) {
		if (!event.hasParam(DATA_PATH)) {
			Optional<String> strVal = Optional.ofNullable(ConfigUtils.get(KEY_INSTANT_TASK));
			if (strVal.isEmpty()) {
				throw new RuntimeException("没有发现Task定义文件的路径");
			}

			String filePath = strVal.get();
			return filePath.startsWith("/") ? filePath : PathUtils.getDefPath() + File.separator + filePath;
		} else {
			return (String) event.getParam(DATA_PATH);
		}
	}
}

/**
 * <pre>
 * 任务对象构建工具类
 * - 从json的定义文件构建一个tasknet对象
 * </pre>
 * 
 * @author catforward
 */
class TaskItemBuilder {

	/**
	 * <pre>
	 * 从定义文件创建一个任务节点树
	 * </pre>
	 * 
	 * @param filePath Task定义文件的完整路径
	 * @throws IOException
	 */
	static Map<String, TaskUnit> loadUnitFromDefineFile(String filePath) throws IOException {
		try (FileInputStream fis = new FileInputStream(filePath);
			InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			BufferedReader reader = new BufferedReader(isr)) {
			return parse(initObjectMapper().readTree(reader));
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * <pre>
	 * 初始化JSON解析器
	 * </pre>
	 * 
	 * @return ObjectMapper
	 */
	private static ObjectMapper initObjectMapper() {
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
	private static Map<String, TaskUnit> parse(JsonNode jsonRoot) {
		HashMap<String, TaskUnit> taskUnitPool = new HashMap<>();
		createUnitTree(null, jsonRoot, taskUnitPool);
		return taskUnitPool;
	}

	/**
	 * <pre>
	 * 创建节点树
	 * </pre>
	 * 
	 * @param parent 父节点
	 * @param jsonNode 当前json节点
	 * @param taskNodePool 所有节点对象池
	 * 
	 */
	private static void createUnitTree(TaskUnit parent, JsonNode jsonNode,
											Map<String, TaskUnit> taskUnitPool) {
		Optional<JsonNode> optValue = Optional.ofNullable(jsonNode.get(Symbols.KEY_UID));
		if (optValue.isEmpty()) {
			throw new RuntimeException("没有设置uid元素");
		}
		// 使用ID取得或创建Unit
		String uid = optValue.get().asText();
		TaskUnit currentUnit = Optional.ofNullable(taskUnitPool.get(uid)).orElseGet(() -> {
			TaskUnit newUnit = new TaskUnit(uid, parent);
			taskUnitPool.put(uid, newUnit);
			if (newUnit.parent == null) {
				newUnit.setType(Type.ROOT);
				newUnit.setUrl(String.format("/%s", uid));
				taskUnitPool.put(Symbols.KEY_ROOT_UNIT, newUnit);
			} else {
				newUnit.setUrl(String.format("%s/%s", parent.getUrl(), uid));
				parent.children.add(newUnit);
			}
			return newUnit;
		});
		// 描述
		optValue = Optional.ofNullable(jsonNode.get(Symbols.KEY_DESC));
		if (optValue.isPresent()) {
			currentUnit.meta.put(Symbols.KEY_DESC, optValue.get().asText());
		}
		// 命令
		optValue = Optional.ofNullable(jsonNode.get(Symbols.KEY_COMMAND));
		if (optValue.isPresent()) {
			currentUnit.meta.put(Symbols.KEY_COMMAND, optValue.get().asText());
		}
		// 触发器
		optValue = Optional.ofNullable(jsonNode.get(Symbols.KEY_PREDECESSORS_LIST));
		if (optValue.isPresent()) {
			JsonNode predecessorsNode = optValue.get();
			int size = predecessorsNode.size();
			if (size != 0) {
				ArrayList<String> predecessorList = new ArrayList<>(size);
				for (int idx = 0; idx < size; ++idx) {
					predecessorList.add(predecessorsNode.get(idx).asText());
				}
				currentUnit.meta.put(Symbols.KEY_PREDECESSORS_LIST, predecessorList);
			}
		}
		// 子元素
		optValue = Optional.ofNullable(jsonNode.get(Symbols.KEY_UNITS));
		if (optValue.isPresent()) {
			int size = optValue.get().size();
			for (int idx = 0; idx < size; ++idx) {
				createUnitTree(currentUnit, optValue.get().get(idx), taskUnitPool);
			}
			// 类型
			if (Type.ROOT != currentUnit.getType()) {
				currentUnit.setType(Type.MODULE);
			}
		}
	}
}

/**
 * <pre>
 * 任务验证工具类
 * - 对构建的tasknet对象进行以下验证
 *  -- 必须的定义项是否完整定义
 *  -- 存在逻辑先后关系的task是否存在
 * </pre>
 * 
 * @author catforward
 */
class TaskValidator {

	private final static Logger logger = Logger.getLogger(TaskValidator.class.getName());

	/**
	 * <pre>
	 * 验证一个任务单元是否符合以下想定
	 * - 单元类型为任务单元时必要要存在命令定义属性
	 * - 存在前驱任务定义时，前驱必须存在
	 * </pre>
	 * 
	 * @param unitPool 任务定义集
	 * @return boolean true:验证通过 false:验证失败
	 */
	static boolean validateUnitDefine(Map<String, TaskUnit> unitPool) {
		for (var entry : unitPool.entrySet()) {
			TaskUnit unit = entry.getValue();
			if (Type.TASK == unit.getType() && unit.getCommand().isEmpty()) {
				logger.warning(String.format("uid:[%s]没有定义command阿！老哥！", unit.getUid()));
				return false;
			}
			for (String predecessorId : unit.getPredecessorsId()) {
				if (!unitPool.containsKey(predecessorId)) {
					logger.warning(String.format("uid:[%s]的前驱[%s]没有定义阿！老哥！", unit.getUid(), predecessorId));
					return false;
				}
			}
		}
		return true;
	}
}
