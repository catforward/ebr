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

package pers.ebr.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;


/**
 * The Launcher of EBR-Server
 * 
 * @author l.gong
 */
public class MainVerticle extends AbstractVerticle {

    private final static Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    /**
     * Initialize and start the {@link MainVerticle}
     * @param startFuture An instance of {@link Future} which allows us to report on the startup status.
     */
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("Starting EBR Server achieved by Vert.x(default event loop pool size is {})", VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE);
    }

}
