/*
  Copyright 2021 liang gong

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.ebr.data;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ebr.base.AppException;
import pers.ebr.types.ResultEnum;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static pers.ebr.base.StringUtils.isNullOrBlank;

/**
 * <pre>Cron Object Store</pre>
 *
 * @author l.gong
 */
public class CronFlowRepo {
    private static final Logger logger = LoggerFactory.getLogger(CronFlowRepo.class);
    /** key: cron_str, value: cron instance */
    private final Map<String, Cron> cronExprPool;
    /** key: flow's url, value: flow instance */
    private final Map<String, Flow> cronSchdFlowPool;

    private static class InstanceHolder {
        private static final CronFlowRepo INSTANCE = new CronFlowRepo();
    }

    private CronFlowRepo() {
        cronExprPool = new ConcurrentHashMap<>();
        cronSchdFlowPool = new ConcurrentHashMap<>();
    }

    public static void release() {
        InstanceHolder.INSTANCE.cronExprPool.clear();
        InstanceHolder.INSTANCE.cronSchdFlowPool.clear();
    }

    public static Map<String, Flow> getCronSchdFlowPoolRef() {
        return InstanceHolder.INSTANCE.cronSchdFlowPool;
    }

    static boolean isOnSchedule(Flow flow) {
        requireNonNull(flow);
        return InstanceHolder.INSTANCE.cronSchdFlowPool.containsKey(flow.getUrl());
    }

    static void addFlow(Flow flow) {
        requireNonNull(flow);
        Task root = flow.getRootTask();
        if (isNullOrBlank(root.getCronStr())) {
            logger.debug("Cron Expr is empty. Abort... Flow:[{}]", flow.getUrl());
            return;
        }
        if (InstanceHolder.INSTANCE.cronSchdFlowPool.containsKey(flow.getUrl())) {
            logger.debug("Flow Obj is already on schedule. Abort... Flow:[{}]", flow.getUrl());
            return;
        }

        Cron cron = InstanceHolder.INSTANCE.cronExprPool.get(root.getCronStr());
        if (isNull(cron)) {
            // only unix type
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
            try {
                cron = new CronParser(cronDefinition).parse(root.getCronStr());
                InstanceHolder.INSTANCE.cronExprPool.put(root.getCronStr(), cron);
            } catch (IllegalArgumentException ex) {
                logger.error("flow[{}]'s parameter[cron] create Cron instance failed.", root.meta.id, ex);
                throw new AppException(ResultEnum.ERR_10104);
            }
        }

        flow.setCron(cron);
        InstanceHolder.INSTANCE.cronSchdFlowPool.put(root.getUrl(), flow);
        // desc
        CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);
        logger.debug("Add new flow instance into cron scheduler pool. flow:{}, cron:{}", root.getUrl(),descriptor.describe(cron));
    }

    static Flow getFlow(String url) {
        requireNonNull(url);
        return InstanceHolder.INSTANCE.cronSchdFlowPool.get(url);
    }

    static void removeFlow(Flow flow) {
        requireNonNull(flow);
        Task root = flow.getRootTask();
        if (InstanceHolder.INSTANCE.cronSchdFlowPool.containsKey(root.getUrl())) {
            InstanceHolder.INSTANCE.cronSchdFlowPool.remove(root.getUrl());
            logger.debug("Remove from cron scheduler pool. flow:{}", root.getUrl());
        }
    }

}
