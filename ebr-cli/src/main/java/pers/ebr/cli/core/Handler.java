/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.cli.core;

import pers.ebr.cli.util.AppLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * 业务处理器
 * - 处理器的作用是当某一个内部事件触发时，由注册此事件的处理器来进行具体处理
 * - 同一事件可以有多个处理器按照注册时的生命顺序进行链式处理
 * </pre>
 *
 * @author l.gong
 */
public interface Handler {
    /**
     * <pre>
     * 进行实际处理
     * 当返回false时表示处理链将被终止
     * </pre>
     *
     * @param context 上下文
     * @return true: succeeded false: failed
     */
    boolean doHandle(HandlerContext context);

    /**
     * <pre>
     * 处理器的描述
     * 用于缓存处理其class对象
     * </pre>
     */
     class HandlerDesc {
        final Class<? extends Handler> handlerClass;

        HandlerDesc(Class<? extends Handler> clazz) {
            handlerClass = clazz;
        }

        Handler newHandlerInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return handlerClass.getDeclaredConstructor().newInstance();
        }

    }

    /**
     * <pre>
     * 处理上下文
     * 用来保存每一个事件的参数及发送源，发送目的信息
     * </pre>
     */
    class HandlerContext {
        /**
         * 事件所携带的参数
         */
        final HashMap<String, Object> param = new HashMap<>();
        /**
         * 处理器执行后的处理结果
         */
        final HashMap<String, Object> result = new HashMap<>();
        String errorMsg = null;
        String nextAction = null;
        String currentAction = null;
        String noticeAction = null;

        /**
         * <pre>
         * 重置所有数据至初始状态
         * </pre>
         *
         * @param newParam 新的事件所携带的参数
         */
        void reset(String action, Map<String, Object> newParam) {
            currentAction = action;
            errorMsg = null;
            nextAction = null;
            noticeAction = null;
            result.clear();
            param.clear();
            param.putAll(newParam);
        }

        /**
         * <pre>
         * 当有处理链中有多个处理器时
         * 每个处理器执行前
         * 合并事件携带的参数
         * 达到上一个处理器的结果能够作为下一个处理器的参数
         * </pre>
         */
        void mergeParam() {
            param.putAll(result);
        }

        public Object getParam(String name) {
            return param.get(name);
        }

        public String getCurrentAction() {
            return currentAction;
        }

        public void setNoticeAction(String newAction) {
            noticeAction = newAction;
        }

        public void setNextAction(String newAction) {
            if (nextAction != null) {
                AppLogger.debug(String.format("nextAction将被替换(%s)<-(%s)", nextAction, newAction));
            }
            nextAction = newAction;
        }

        public void addHandlerResult(String newKey, Object newObj) {
            if (result.containsKey(newKey)) {
                AppLogger.debug(String.format("handlerResult(%s)将被替换", newKey));
            }
            result.put(newKey, newObj);
        }

        public HandlerContext setErrorMessage(String msg) {
            if (errorMsg != null) {
                AppLogger.debug(String.format("errorMsg将被替换(%s)<-(%s)", errorMsg, msg));
            }
            errorMsg = msg;
            return this;
        }
    }

    /**
     * <pre>
     * 处理链
     * </pre>
     */
    class HandlerChain {
        /**
         *
         */
        final Map<Class<? extends Handler>, HandlerDesc> handlerPool;
        final String action;

        HandlerChain(String newAct) {
            action = newAct;
            handlerPool = new LinkedHashMap<>();
        }

        void addHandler(Class<? extends Handler> clazz) {
            handlerPool.put(clazz, new HandlerDesc(clazz));
        }
    }
}
