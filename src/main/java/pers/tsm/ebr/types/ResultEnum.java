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
package pers.tsm.ebr.types;

import pers.tsm.ebr.base.IResult;

/**
 *
 *
 * @author l.gong
 */
public enum ResultEnum implements IResult {
    SUCCESS("0", "success"),
    ERROR("1", "internal error"),
    ERR_400("400", "bad request"),
    ERR_401("401", "unauthorized"),
    ERR_404("404", "not found"),
    ERR_500("500", "internal server error"),
    ERR_10101("10101", "make flow failed"),
    ERR_10102("10102", "only one root task in a flow"),
    ERR_10103("10103", "DAG flow validate failed"),
    ERR_10104("10104", "task's define error"),
    ERR_11001("11001", "invalid parameter"),
    ERR_11002("11002", "invalid request"),
    ERR_11003("11003", "specified flow is not exist"),
    ERR_11004("11004", "specified task is not exist"),
    ERR_11005("11005", "specified flow is already running or skipped"),
    ERR_11006("11006", "specified task is already running or skipped"),
    ERR_11007("11007", "unsupported action"),
    ;

    private final String code;
    private final String message;

    ResultEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
