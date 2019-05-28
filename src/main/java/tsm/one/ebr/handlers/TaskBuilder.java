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
import static tsm.one.ebr.base.Handler.HandlerEvent.Const.DATA_TASK_NET;
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
import java.util.HashMap;
import java.util.List;
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
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import tsm.one.ebr.base.Application;
import tsm.one.ebr.base.Handler;
import tsm.one.ebr.base.Task.Meta;
import tsm.one.ebr.base.Task.Net;
import tsm.one.ebr.base.Task.Node;
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
				buildTaskNet(event);
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
	private void buildTaskNet(HandlerEvent event) throws IOException {
		String filePath = makeDefFileFullPath(event);

		Node rootNode = TaskItemBuilder.loadDefineFileFromDisk(filePath);
		if (!TaskValidator.validateNodeTree(rootNode)) {
			logger.info("Task的JSON(NodeTree)定义不合法");
			finishNoticeFrom(TASK_BUILDER);
			return;
		}

		Net taskNet = new Net(rootNode);
		if (!TaskValidator.validateTaskNet(taskNet)) {
			logger.info("Task的JSON(Net)定义不合法");
			finishNoticeFrom(TASK_BUILDER);
			return;
		}

		postMessage(new HandlerEvent()
				.setSrc(TASK_BUILDER)
				.setDst(TASK_MANAGER)
				.setAct(ACT_MANAGEMENT_APPEND)
				.addParam(FLG, FLG_AUTO_START)
				.addParam(DATA_TASK_NET, taskNet));
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
	static Node loadDefineFileFromDisk(String filePath) throws IOException {
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
	private static Node parse(JsonNode jsonRoot) {
		HashMap<String, Node> taskNodePool = new HashMap<>();
		createTaskNodeTree(null, jsonRoot, taskNodePool);
		Node nRoot = taskNodePool.get(Node.ROOT_NODE);
		taskNodePool.clear();
		return nRoot;
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
	private static void createTaskNodeTree(Node parent, JsonNode jsonNode, Map<String, Node> taskNodePool) {
		// 基本项目
		Optional<JsonNode> value = Optional.ofNullable(jsonNode.get(Meta.KEY_ID));
		if (value.isEmpty()) {
			throw new RuntimeException("没有设置id元素");
		}
		// 使用ID取得或创建Meta
		String id = value.get().asText();
		Node currentNode = getTaskNode(id, taskNodePool);
		// 类型
		value = Optional.ofNullable(jsonNode.get(Meta.KEY_TYPE));
		if (value.isPresent()) {
			currentNode.getMeta().setType(value.get().asText());
		}
		// 描述
		value = Optional.ofNullable(jsonNode.get(Meta.KEY_DESC));
		if (value.isPresent()) {
			currentNode.getMeta().setDesc(value.get().asText());
		}
		// 命令
		if (Meta.VALUE_TYPE_TASK.equalsIgnoreCase(currentNode.getMeta().getType())) {

			value = Optional.ofNullable(jsonNode.get(Meta.KEY_COMMAND));
			if (value.isPresent()) {
				currentNode.getMeta().setCommand(value.get().asText());
			}
			value = Optional.ofNullable(jsonNode.get(Meta.KEY_ARGS));
			if (value.isPresent()) {
				currentNode.getMeta().setArgs(value.get().asText());
			}
		}
		// 触发器,前驱后继关系
		value = Optional.ofNullable(jsonNode.get(Meta.KEY_TRIGGER));
		if (value.isPresent()) {
			Optional<JsonNode> subValue = Optional.ofNullable(value.get().get(Meta.KEY_PREDECESSORS));
			if (subValue.isPresent()) {
				JsonNode prevTasks = subValue.get();
				int size = prevTasks.size();
				for (int idx = 0; idx < size; ++idx) {
					Node prevNode = getTaskNode(prevTasks.get(idx).asText(), taskNodePool);
					prevNode.addSuccessors(currentNode);
					currentNode.addPredecessors(prevNode);
				}
			}
		}
		// 与父节点关系
		if (Meta.VALUE_TYPE_NET.equalsIgnoreCase(currentNode.getMeta().getType())) {
			if (taskNodePool.containsKey(Node.ROOT_NODE)) {
				throw new RuntimeException("不允许存在复数个类型为NET元素的声明");
			}
			currentNode.setUrl(String.format("/%s", currentNode.getMeta().getId()));
			taskNodePool.remove(currentNode.getMeta().getId());
			taskNodePool.put(Node.ROOT_NODE, currentNode);
		} else {
			currentNode.setParent(parent);
			currentNode.setUrl(String.format("%s/%s", parent.getUrl(), currentNode.getMeta().getId()));
			parent.addChild(currentNode);
		}
		// 子元素
		value = Optional.ofNullable(jsonNode.get(Meta.KEY_SUB_TASKS));
		if (value.isPresent()) {
			int size = value.get().size();
			for (int idx = 0; idx < size; ++idx) {
				createTaskNodeTree(currentNode, value.get().get(idx), taskNodePool);
			}
		}
	}

	/**
	 * <pre>
	 * 取得一个指定ID的节点实例
	 * 如果指定ID的节点实例不存在则创建，保存入节点实例池并返回
	 * </pre>
	 * 
	 * @param id 节点ID
	 * @param taskNodePool 节点实例池
	 * @return Node
	 */
	private static Node getTaskNode(String id, Map<String, Node> taskNodePool) {
		return Optional.ofNullable(taskNodePool.get(id)).orElseGet(() -> {
			Node newNode = new Node(new Meta(id));
			taskNodePool.put(id, newNode);
			return newNode;
		});
	}
}

/**
 * <pre>
 * 任务验证工具类
 * - 对构建的tasknet对象进行以下验证
 *  -- 必须的定义项是否完整定义
 *  -- 存在逻辑先后关系的task是否存在
 *  -- 逻辑图不允许出现自环
 * </pre>
 * 
 * @author catforward
 */
class TaskValidator {

	private final static Logger logger = Logger.getLogger(TaskValidator.class.getName());

	/**
	 * <pre>
	 * 验证一个节点树是否符合以下想定
	 * - 必要的属性是否设定
	 * - 存在子节点时对子节点作同样验证
	 * </pre>
	 * 
	 * @param rootNode 根节点
	 * @return boolean true:验证通过 false:验证失败
	 */
	static boolean validateNodeTree(Node rootNode) {
		return checkTaskNode(rootNode);
	}

	/**
	 * <pre>
	 * 一个节点集合类似一个图结构，此图结构否符合以下想定
	 * - 可以没有前驱后继
	 * - 可以存在多前驱，多后继
	 * - 可以存在只有前驱的节点或只有后继的节点
	 * - 不允许出现自环
	 * </pre>
	 * 
	 * @param net 节点集
	 * @return boolean true:验证通过 false:验证失败
	 */
	static boolean validateTaskNet(Net net) {
		boolean validated = true;
		MutableGraph<Node> taskGraph = GraphBuilder.directed() // 指定为有向图
				.nodeOrder(ElementOrder.<Node>insertion()) // 节点按插入顺序输出
				// (还可以取值无序unordered()、节点类型的自然顺序natural())
				// .expectedNodeCount(NODE_COUNT) //预期节点数
				.allowsSelfLoops(false) // 不允许自环
				.build();
		try {
			net.getHeadNodes().forEach(headNode -> {
				fillTaskGraph(headNode, taskGraph);
			});
		} catch (Exception ex) {
			// TODO
			ex.printStackTrace();
			validated = false;
		}
		return validated;
	}

	/**
	 * <pre>
	 * 验证一个给定的节点的以下属性是否被设置
	 * - Type属性
	 * - Command属性
	 * </pre>
	 * 
	 * @param taskNode 验证对象节点
	 * @return boolean true:验证通过 false:验证失败
	 */
	private static boolean checkTaskNode(Node taskNode) {
		boolean validated = true;
		String type = "";
		Optional<String> value = Optional.ofNullable(taskNode.getMeta().getType());
		if (value.isEmpty()) {
			logger.severe(String.format("Task:[%s]的Type属性未设定", taskNode.getMeta().getId()));
			validated = false;
		} else {
			type = value.get();
		}
		// 执行命令的定义
		value = Optional.ofNullable(taskNode.getMeta().getCommand());
		if (Meta.VALUE_TYPE_TASK.equalsIgnoreCase(type) && value.isEmpty()) {
			logger.severe(String.format("Task:[%s]的Command未设定", taskNode.getMeta().getId()));
			validated = false;
		}
		// 子Task存在检查
		Optional<List<Node>> childrenOpt = Optional.ofNullable(taskNode.getChildren());
		if (childrenOpt.isPresent()) {
			for (Node child : childrenOpt.get()) {
				if (!checkTaskNode(child)) {
					validated = false;
				}
			}
		}
		return validated;
	}

	/**
	 * <pre>
	 * 将创建的任务节点信息填充至图结构
	 * 出现自环时验证失败
	 * </pre>
	 * 
	 * @param taskNode 节点
	 * @param taskGraph 节点图
	 */
	private static void fillTaskGraph(Node taskNode, MutableGraph<Node> taskGraph) {
		taskGraph.addNode(taskNode);
		taskNode.getPredecessors().forEach(predecessor -> {
			taskGraph.putEdge(predecessor, taskNode);
		});
		for (Node childNode : taskNode.getChildren()) {
			fillTaskGraph(childNode, taskGraph);
		}
	}
}
