/**
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
package pers.tsm.ebr.data;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;

/**
 *
 *
 * @author l.gong
 */
public class VerticleProp {
    private final Supplier<Verticle> verticle;
    private DeploymentOptions options;

    public VerticleProp(Supplier<Verticle> verticle, DeploymentOptions options) {
        requireNonNull(verticle);
        requireNonNull(options);
        this.verticle = verticle;
        this.options = options;
    }

    public Supplier<Verticle> getVerticle() {
        return verticle;
    }

    public DeploymentOptions getOptions() {
        return options;
    }

    public void setOptions(DeploymentOptions options) {
        this.options = options;
    }

}
