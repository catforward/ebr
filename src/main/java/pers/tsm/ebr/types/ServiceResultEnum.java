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

import pers.tsm.ebr.service.IResult;

/**
 *
 *
 * @author l.gong
 */
public enum ServiceResultEnum implements IResult {
	NORMAL("0", "success"),
    ERROR("-1", "unknown error"),
    HTTP_200("200", "服务器成功返回请求的数据"),
    HTTP_201("201", "新建或修改数据成功"),
    HTTP_202("202", "一个请求已经进入后台排队（异步任务）"),
    HTTP_204("204", "删除数据成功"),
    HTTP_400("400", "发出的请求有错误，服务器没有进行新建或修改数据的操作"),
    HTTP_401("401", "用户没有权限（令牌、用户名、密码错误）"),
    HTTP_403("403", "用户得到授权，但是访问是被禁止的"),
    HTTP_404("404", "发出的请求针对的是不存在的记录，服务器没有进行操作"),
    HTTP_406("406", "请求的格式不可得"),
    HTTP_410("410", "请求的资源被永久删除，且不会再得到的"),
    HTTP_422("422", "当创建一个对象时，发生一个验证错误"),
    HTTP_500("500", "服务器发生错误，请检查服务器"),
    HTTP_502("502", "网关错误"),
    HTTP_503("503", "服务不可用，服务器暂时过载或维护"),
    HTTP_504("504", "网关超时"),
    RC_11001("11001", "invalid parameter"),
    ;
    /** 返回码 */
    private final String code;
    /** 响应信息 */
    private final String message;

    ServiceResultEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * <p>获取处理结果码</p>
     * @return 处理结果码
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * <p>获取处理结果描述</p>
     * @return 处理结果描述
     */
    @Override
    public String getMessage() {
        return message;
    }
}
