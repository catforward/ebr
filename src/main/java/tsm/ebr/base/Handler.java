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
package tsm.ebr.base;

import static tsm.ebr.base.Handler.HandlerEvent.Const.ACT_SERV_SHUTDOWN;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <pre>
 * 抽象处理父类，真实处理逻辑由子类提供
 * 此抽象类只提供以下功能
 * - 保持处理类状态
 * - 向子类提供发送事件的能力
 * 
 * </pre>
 * 
 * @author catforward
 */
public abstract class Handler {

	/**
	 * <pre>
	 * 处理状态枚举
	 * </pre>
	 */
	public enum HandlerStatus {
		CREATED("CREATED", "SERVICE CREATED"),
		INITIALIZED("INITIALIZED", "SERVICE INITIALIZED"),
		RUNNING("RUNNING", "SERVICE RUNNING"),
		FINISHED("FINISHED", "SERVICE FINISHED");

		private String name;
		private String desc;

		/**
		 * 枚举构造函数
		 *
		 * @param name 枚举名称
		 * @param desc 枚举描述
		 */
		HandlerStatus(String newName, String newDesc) {
			name = newName;
			desc = newDesc;
		}

		@Override
		public String toString() {
			return String.format("%s:%s", name, desc);
		}
	}

	/**
	 * <pre>
	 * 消息体定义
	 * </pre>
	 */
	public static final class HandlerEvent {

		/**
		 * <pre>
		 * 服务事件中传送参数的预定义名称
		 * </pre>
		 */
		public enum Const {
			UNKNOWN("unknown"),
			SRC("src"),
			DST("dst"),
			ACT("act"),
			FLG("flg"),

			ACT_SERV_SHUTDOWN("act_serv_shutdown"),
			ACT_LOAD_DEF_FILE("act_load_def_file"),
			ACT_MANAGEMENT_APPEND("act_management_append"),
			ACT_LAUNCH_TASK_GRAPH("act_launch_task_graph"),
			ACT_LAUNCH_TASK_UNIT("act_launch_task_unit"),
			ACT_TASK_UNIT_STATE_CHANGED("act_task_unit_state_changed"),
			ACT_TASK_GRAPH_STATE_CHANGED("act_task_graph_state_changed"),

			DATA_PATH("data_path"),
			DATA_TASK_GRAPH("data_task_graph"),
			DATA_TASK_GRAPH_NEW_STATE("data_task_graph_new_state"),
			DATA_TASK_UNIT_URL("data_task_unit_url"),
			DATA_TASK_UNIT_COMMAND("data_task_unit_command"),
			DATA_TASK_UNIT_NEW_STATE("data_task_node_new_state"),

			FLG_AUTO_START("flg_auto_start");

			private String value;

			private Const(String newValue) {
				value = newValue;
			}

			@Override
			public String toString() {
				return String.format("%s", value);
			}
		}

		protected final Object emptyObj;
		protected final Map<Const, Object> paramMap;

		public HandlerEvent() {
			emptyObj = new Object();
			paramMap = new HashMap<>();
		}

		/**
		 * 获得指定名字的传送参数
		 * 
		 * @param name 参数名
		 * @return Object 参数值
		 */
		public Object getParam(Const name) {
			return Optional.ofNullable(paramMap.get(name)).orElse(emptyObj);
		}

		/**
		 * 获得指定名字的传送参数
		 * 
		 * @param name  参数名
		 * @param value 参数值
		 * @return ServiceEvent 事件实例
		 */
		public HandlerEvent addParam(Const name, Object value) {
			paramMap.put(name, value);
			return this;
		}

		/**
		 * 获得指定的Action定义
		 * 
		 * @return Const Action定义
		 */
		public Const getAct() {
			return Optional.ofNullable((Const) getParam(Const.ACT)).orElse(Const.UNKNOWN);
		}

		/**
		 * 设定指定的Action定义
		 * 
		 * @param actConst Action定义
		 * @return HandlerEvent 事件实例
		 */
		public HandlerEvent setAct(Const actConst) {
			addParam(Const.ACT, actConst);
			return this;
		}

		/**
		 * 判断消息体中是否包含指定名称的参数
		 * 
		 * @param name 参数名
		 * @return boolean true：存在 false：不存在
		 */
		public boolean hasParam(Const name) {
			return paramMap.containsKey(name);
		}

		/**
		 * 判断给定的一个值是否是消息体定义的空值
		 * 
		 * @param obj 判断对象
		 * @return boolean true：空值 false：非空值
		 */
		public boolean isEmptyValue(Object obj) {
			return emptyObj.equals(obj);
		}

		/**
		 * 设定发送源的HandlerID
		 * 
		 * @param srcHandlerID 发送源
		 * @return HandlerEvent 事件实例
		 */
		public HandlerEvent setSrc(HandlerId srcHandlerID) {
			addParam(Const.SRC, srcHandlerID);
			return this;
		}

		/**
		 * 设定发送目标的HandlerID
		 * 
		 * @param dstHandlerId 发送目标
		 * @return HandlerEvent 事件实例
		 */
		public HandlerEvent setDst(HandlerId dstHandlerId) {
			addParam(Const.DST, dstHandlerId);
			return this;
		}

		/**
		 * 取得发送源的HandlerID
		 * 
		 * @return HandlerId 发送源
		 */
		public HandlerId getSrc() {
			return Optional.ofNullable((HandlerId) getParam(Const.SRC)).orElse(HandlerId.TASK_APP);
		}

		/**
		 * 取得发送目标的HandlerID
		 * 
		 * @return HandlerId 发送目标
		 */
		public HandlerId getDst() {
			return Optional.ofNullable((HandlerId) getParam(Const.DST)).orElse(HandlerId.TASK_APP);
		}

	}

	/** 服务状态 */
	protected HandlerStatus serviceStatus;
	/** 应用程序实例 */
	protected final Application application;

	/**
	 * 构造函数
	 * 
	 * @param app 应用程序实例
	 */
	protected Handler(Application app) {
		application = app;
		serviceStatus = HandlerStatus.CREATED;
	}

	/**
	 * 服务初始化
	 */
	public void init() {
		onInit();
		serviceStatus = HandlerStatus.INITIALIZED;
	}

	/**
	 * 服务开始
	 */
	public void start() {
		serviceStatus = HandlerStatus.RUNNING;
		onStart();
	}

	/**
	 * 服务结束
	 */
	public void finish() {
		onFinish();
		serviceStatus = HandlerStatus.FINISHED;
	}

	/**
	 * 服务状态
	 * 
	 * @return {@link #serviceStatus}
	 */
	public HandlerStatus status() {
		return serviceStatus;
	}

	/**
	 * (子类实现)服务初始化
	 */
	protected abstract void onInit();

	/**
	 * (子类实现)服务开始
	 */
	protected abstract void onStart();

	/**
	 * (子类实现)服务结束
	 */
	protected abstract void onFinish();

	/**
	 * 发送消息
	 * 
	 * @param event 消息体
	 */
	protected void postMessage(HandlerEvent event) {
		application.getEventBus().post(event);
	}

	/**
	 * 发送程序结束的消息广播
	 * 
	 * @param src 发送源ID
	 */
	protected void finishNoticeFrom(HandlerId srcId) {
		application.getEventBus().post(new HandlerEvent()
				.setSrc(srcId)
				.setDst(HandlerId.TASK_APP)
				.setAct(ACT_SERV_SHUTDOWN));
	}
}
