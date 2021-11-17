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
package pers.ebr;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pers.ebr.types.ResultEnum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static pers.ebr.base.AppSymbols.*;
import static pers.ebr.base.ServiceSymbols.*;

/**
 * <pre>
 * </pre>
 *
 * @author l.gong
 */
public class ServiceApiTest {
    private final static String DEFAULT_URL = "http://localhost:8088/ebr";
    private static AppMain app;

    @BeforeAll
    public static void initApp() throws InterruptedException {
        app = new AppMain();
        app.launch();
        // improve this
        // waiting for the http server...
        Thread.sleep(5000);
    }

    @AfterAll
    public static void releaseApp() throws Exception {
        app.onShutdown();
    }

    /**
     * 测试正常得请求及响应
     * 只验证到准备数据得件数这一层
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_list_api_test_01() throws IOException, InterruptedException {
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_INFO_FLOW_LIST);

//        JsonObject expectData = new ServiceResultMsg(ResultEnum.SUCCESS).setData(new JsonObject().put(FLOWS, new JsonArray()
//                .add(new JsonObject().put(URL, "/FLOW-1").put(STATE, STORED.getName()).put(LAST_MODIFIED_TIME, "2021-07-16 19:45:54").put(SIZE, 349))
//                .add(new JsonObject().put(URL, "/FLOW-2").put(STATE, STORED.getName()).put(LAST_MODIFIED_TIME, "2021-07-16 19:45:54").put(SIZE, 420))
//                .add(new JsonObject().put(URL, "/FLOW-3").put(STATE, STORED.getName()).put(LAST_MODIFIED_TIME, "2021-07-16 19:45:54").put(SIZE, 524))
//                .add(new JsonObject().put(URL, "/FLOW-4").put(STATE, STORED.getName()).put(LAST_MODIFIED_TIME, "2021-09-04 22:57:06").put(SIZE, 957))
//                .add(new JsonObject().put(URL, "/sub/FLOW-5").put(STATE, STORED.getName()).put(LAST_MODIFIED_TIME, "2021-07-16 19:45:54").put(SIZE, 477))
//        )).toJsonObject();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.SUCCESS.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.SUCCESS.getMessage(), responseData.getString(MSG));
        JsonObject data = responseData.getJsonObject(DATA, EMPTY_JSON_OBJ);
        // 验证data目录下所有flow定义文件都被读取到
        JsonArray flows = data.getJsonArray(FLOWS, EMPTY_JSON_ARR);
        Assertions.assertEquals(5, flows.size());

    }

    /**
     * 测试无请求体正常得请求及响应
     * 只验证到准备数据得件数这一层
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_list_api_test_02() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        Assertions.assertEquals(ResultEnum.ERR_404.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.ERR_404.getMessage(), responseData.getString(MSG));
    }

    /**
     * 测试正常得请求及响应
     * 只验证到数据得件数这一层
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_detail_api_test_01() throws IOException, InterruptedException {
        String flowId = "/sub/FLOW-5";
        JsonObject flowIdObj = new JsonObject();
        flowIdObj.put(FLOW, flowId);
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_INFO_FLOW_DETAIL);
        requestData.put(PARAM, flowIdObj);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.SUCCESS.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.SUCCESS.getMessage(), responseData.getString(MSG));
        JsonObject data = responseData.getJsonObject(DATA, EMPTY_JSON_OBJ);
        // 验证data目录下所有flow定义文件都被读取到
        JsonObject flowDetail = data.getJsonObject(FLOW, EMPTY_JSON_OBJ);
        // flow-5不是cron对象所以只有2个元素
        Assertions.assertEquals(2, flowDetail.size());
        Assertions.assertEquals(flowId, flowDetail.getString(URL, BLANK_STR));
        // flow-5 内部定义task数量参见实际定义文件
        JsonArray tasks = flowDetail.getJsonArray(CONTENT, EMPTY_JSON_ARR);
        Assertions.assertEquals(4, tasks.size());
    }

    /**
     * 测试正常得请求及响应
     * 带有cron标签的flow
     * 只验证到数据得件数这一层
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_detail_api_test_02() throws IOException, InterruptedException {
        String flowId = "/FLOW-4";
        JsonObject flowIdObj = new JsonObject();
        flowIdObj.put(FLOW, flowId);
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_INFO_FLOW_DETAIL);
        requestData.put(PARAM, flowIdObj);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.SUCCESS.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.SUCCESS.getMessage(), responseData.getString(MSG));
        JsonObject data = responseData.getJsonObject(DATA, EMPTY_JSON_OBJ);
        // 验证data目录下所有flow定义文件都被读取到
        JsonObject flowDetail = data.getJsonObject(FLOW, EMPTY_JSON_OBJ);
        // flow-4是cron对象所以只有3个元素
        Assertions.assertEquals(3, flowDetail.size());
        Assertions.assertEquals(flowId, flowDetail.getString(URL, BLANK_STR));
        // flow-5 内部定义task数量参见实际定义文件
        JsonArray tasks = flowDetail.getJsonArray(CONTENT, EMPTY_JSON_ARR);
        Assertions.assertEquals(7, tasks.size());
    }

    /**
     * 测试未设定flow_id的请求及响应
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_detail_api_test_03() throws IOException, InterruptedException {
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_INFO_FLOW_DETAIL);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.ERR_11001.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.ERR_11001.getMessage(), responseData.getString(MSG));
    }

    /**
     * 测试正常得请求及响应
     * Flow开始命令
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_schd_action_api_test_01() throws IOException, InterruptedException {
        JsonObject flowIdObj = new JsonObject();
        flowIdObj.put(FLOW, "/FLOW-4");
        flowIdObj.put(ACTION, START);
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_SCHD_ACTION);
        requestData.put(PARAM, flowIdObj);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.SUCCESS.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.SUCCESS.getMessage(), responseData.getString(MSG));
    }

    /**
     * 测试正常得请求及响应
     * Flow停止命令(开始命令执行成功后)
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_schd_action_api_test_02() throws IOException, InterruptedException {
        JsonObject flowIdObj = new JsonObject();
        flowIdObj.put(FLOW, "/FLOW-4");
        flowIdObj.put(ACTION, ABORT);
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_SCHD_ACTION);
        requestData.put(PARAM, flowIdObj);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.SUCCESS.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.SUCCESS.getMessage(), responseData.getString(MSG));
    }

    /**
     * 测试缺少flow_id的请求及响应
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_schd_action_api_test_03() throws IOException, InterruptedException {
        JsonObject flowIdObj = new JsonObject();
        flowIdObj.put(ACTION, START);
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_SCHD_ACTION);
        requestData.put(PARAM, flowIdObj);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.ERR_11001.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.ERR_11001.getMessage(), responseData.getString(MSG));
    }

    /**
     * 测试缺少action_id的请求及响应
     * @throws IOException HttpClient 送信失败
     * @throws InterruptedException HttpClient 送信失败
     */
    @Test
    public void flow_schd_action_api_test_04() throws IOException, InterruptedException {
        JsonObject flowIdObj = new JsonObject();
        flowIdObj.put(FLOW, "/FLOW-4");
        JsonObject requestData = new JsonObject();
        requestData.put(API, API_SCHD_ACTION);
        requestData.put(PARAM, flowIdObj);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.encode()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        JsonObject responseData = new JsonObject(response.body());
        // 验证处理成功
        Assertions.assertEquals(ResultEnum.ERR_11001.getCode(), responseData.getString(CODE));
        Assertions.assertEquals(ResultEnum.ERR_11001.getMessage(), responseData.getString(MSG));
    }
}
