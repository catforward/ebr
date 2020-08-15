"use strict";

/*******************************************************************************************************
 *  TOPIC
 * *****************************************************************************************************/

 /**
 * 获取服务器信息
 * 请求：{req: "req.GetServerInfo", param: {空参数}}
 * 正常响应：{req: "req.GetServerInfo", result: {config:{key-value数据}, env:{key-value数据}}}
 */
const REQ_GET_SERVER_INFO = "req.GetServerInfo";

/**
 * 服务器端验证taskflow的定义合法性
 * 请求：{req: "req.ValidateFlow", param: {taskflow的json定义体}}
 * 正常响应：{req: "req.ValidateFlow", result: {空数据 }}
 * 异常响应：{req: "req.ValidateFlow", error: {空数据 }}
 */
const REQ_VALIDATE_FLOW = "req.ValidateFlow";

/**
 * 保存taskflow定义
 * 请求：{req: "req.SaveFlow", param: {taskflow的json定义体}}
 * 正常响应：{req: "req.SaveFlow", result: {空数据 }}
 * 异常响应：{req: "req.SaveFlow", error: {空数据 }}
 */
const REQ_SAVE_FLOW = "req.SaveFlow";

/**
 * 获取所有taskflow的定义以及状态
 * 请求：{req: "req.AllFlow", param: {空参数}}
 * 正常响应：{req: "req.AllFlow", result: {["taskflow_id": value, "instance_id": value, "tasks": [task detail array]]}}
 * 异常响应：{req: "req.AllFlow", error: {空数据 }}
 */

const REQ_ALL_FLOW = "req.AllFlow";

/**
 * 获取taskflow运行状态概要
 * 请求：{req: "req.ExecStatistics", param: {空参数}}
 * 正常响应：{req: "req.ExecStatistics", result: {"complete":int,"failed":int,"active":int}}
 * 异常响应：{req: "req.ExecStatistics", error: {"info":string}}
 */
const REQ_EXEC_STATISTICS = "req.ExecStatistics";

/**
 * 启动指定ID的taskflow
 * 请求：{req: "req.LaunchFlow", param: {"taskflow_id":string}}
 * 正常响应：{req: "req.LaunchFlow", result: {"info":string}}
 * 异常响应：{req: "req.LaunchFlow", error: {"info":string}}
 */
const REQ_LAUNCH_FLOW = "req.LaunchFlow";

/**
 * 删除指定ID的taskflow
 * 请求：{req: "req.DeleteFlow", param: {"taskflow_id":string}}
 * 正常响应：{req: "req.DeleteFlow", result: {"info":string}}
 * 异常响应：{req: "req.DeleteFlow", error: {"info":string}}
 */
const REQ_DEL_FLOW = "req.DeleteFlow";


/*******************************************************************************************************
 *  CONST
 * *****************************************************************************************************/

const TASK_STATE_UNKNOWN  = -1;
const TASK_STATE_INACTIVE = 1;
const TASK_STATE_ACTIVE   = 2;
const TASK_STATE_COMPLETE = 3;
const TASK_STATE_FAILED   = 4;

const SSK_PROC_CMD    = "proc_cmd";
const SSK_FLOW_ID = "taskflow_id";

/*******************************************************************************************************
 *  根对象
 * *****************************************************************************************************/
var ebr = {};
ebr.reqHandlerMap = new Map();
ebr.resHandlerMap = new Map();

/*******************************************************************************************************
 *  公共函数
 * *****************************************************************************************************/
ebr.com = {
    BindQuery : function(topic, reqFunc, resFunc) {
        if (typeof topic !== "string" || topic.trim() === ""
            || typeof reqFunc !== "function" || typeof resFunc !== "function") {
            alert("Error: [BindQuery] Invalid Params...");
            return;
        }
        ebr.reqHandlerMap.set(topic, reqFunc);
        ebr.resHandlerMap.set(topic, resFunc);
    },

    EmitQuery : function(topic) {
        if (typeof topic !== "string" || topic.trim() === "") {
            alert("Error: [EmitQuery] Invalid Params...");
            return;
        }
        if (!ebr.reqHandlerMap.has(topic) || !ebr.resHandlerMap.has(topic)) {
            alert("Error: [EmitQuery] no handler for [" + topic + "]");
            return;
        }
        let reqData = ebr.reqHandlerMap.get(topic)();
        if (reqData === undefined || reqData === null) {
            alert("Error: [EmitQuery] request data can not be null or undefined...");
            return;
        }
        ebr.com.postMsg({ req: topic, param: reqData}, ebr.resHandlerMap.get(topic));
    },

    postMsg : function(jsonData, resHandler) {
        if (jsonData === null || typeof jsonData !== "object" || typeof resHandler !== "function") {
            alert("Error: [postMsg] Invalid Params...");
            return;
        }
        $.ajax({
          type: "POST",
          url: "/proc",
          contentType: 'application/json',
          data: JSON.stringify(jsonData),
          success: function(data, textStatus, jqXHR) {
            //console.log("response data: "+ JSON.stringify(data));
            resHandler(data);
          }
        });
    },

};

/*******************************************************************************************************
 *  侧边栏
 *******************************************************************************************************/
ebr.sidebar = {};
ebr.sidebar.view = {
    Init : () => {
        $("#serverInfoPanelBtn").click(() => {
            ebr.sidebar.view.ShiftPanel("serverInfoPanel");
            ebr.com.EmitQuery(REQ_GET_SERVER_INFO);
        });
        $("#flowStatusInfoPanelBtn").click(() => {
            ebr.sidebar.view.ShiftPanel("flowStatusInfoPanel");
            ebr.com.EmitQuery(REQ_EXEC_STATISTICS);
            ebr.com.EmitQuery(REQ_ALL_FLOW);
        });
        $("#flowDefineViewerPanelBtn").click(() => {
            ebr.sidebar.view.ShiftPanel("flowDefineViewerPanel");
        });
    },

    ShiftPanel : function(panelId) {
        if (panelId) {
            // replace the main panel
            $(".ebr-panel").each((index, element) => {
                $(element).addClass("ebr-invisible");
            });
            $("#" + panelId).removeClass("ebr-invisible");
        }
    },
};
ebr.sidebar.ctl = {
    Init : () => {
        ebr.sidebar.view.Init();
    }
};

/********************************************************************************************************
 * 状态浏览页面
 ********************************************************************************************************/
ebr.state_viewer = {};
ebr.state_viewer.view = {
    Init : () => {
        document.querySelector("#getAllFlowBtn").addEventListener("click", () => {
            ebr.com.EmitQuery(REQ_EXEC_STATISTICS);
            ebr.com.EmitQuery(REQ_ALL_FLOW);
        }, false);
    },

    Process : (lnkObj) => {
        let flow_id = $(lnkObj).attr(SSK_FLOW_ID);
        let title = $(lnkObj).attr("title");
        sessionStorage.setItem(SSK_FLOW_ID, flow_id);
        if ("run" === title) {
            ebr.com.EmitQuery(REQ_LAUNCH_FLOW);
        } else if ("log" === title) {
            // TODO
        } else if ("download" === title) {
            // TODO
        } else if ("delete" === title) {
            ebr.com.EmitQuery(REQ_DEL_FLOW);
        }
    },

    AddFlowDetailView : (jsonResultData) => {
        // console.log(jsonResultData);
        $("#accordionFlowList").empty();
        for (let i = 0; i < jsonResultData.length; i++) {
            ebr.state_viewer.view.updateFlowStateList(jsonResultData[i]);
        }
    },

    updateFlowStateList : (detailData) => {
        if (!detailData.taskflow_id) return;

        let tmpCard = $("#taskStatusCard").clone();
        tmpCard.attr("id", "taskStatusCard-" + detailData.taskflow_id);

        let tmpCardHeader = $("#statusHeadingOne");
        tmpCardHeader.attr("id", "statusHeadingOne" + detailData.taskflow_id);

        let tmpCardTitle = tmpCard.find("#taskStatusCardTitle");
        tmpCardTitle.html(detailData.taskflow_id);
        tmpCardTitle.attr("data-target", "#collapseOne-" + detailData.taskflow_id);
        tmpCardTitle.attr("aria-controls", "collapseOne-" + detailData.taskflow_id);

        let tmpCardBody = tmpCard.find("#collapseOne");
        tmpCardBody.attr("id", "collapseOne-" + detailData.taskflow_id);
        tmpCardBody.attr("data-parent", "#" + tmpCard.attr("id"));
        tmpCardBody.attr("aria-labelledby", tmpCardHeader.attr("id"));

        tmpCard.find(".ebr-card-flex-bar-btn-link").each((idx, elem) => {
            // <a>
            $(elem).attr(SSK_FLOW_ID, detailData.taskflow_id);
        });

        for (let i = 0; i < detailData.tasks.length; i++) {
            let taskDetail = detailData.tasks[i];
            let trHtml = $("<tr></tr>");
            trHtml.append("<td>" + taskDetail.group + "</td>");
            trHtml.append("<td>" + taskDetail.id + "</td>");
            trHtml.append("<td>" + taskDetail.desc + "</td>");
            trHtml.append("<td>" + taskDetail.cmd + "</td>");
            switch(taskDetail.state) {
                case TASK_STATE_INACTIVE: {
                    trHtml.append("<td><span class='badge badge-light'>Inactive</span></td>"); break;
                }
                case TASK_STATE_ACTIVE: {
                    trHtml.append("<td><span class='badge badge-success'>Active</span></td>"); break;
                }
                case TASK_STATE_COMPLETE: {
                    trHtml.append("<td><span class='badge badge-info'>Complete</span></td>"); break;
                }
                case TASK_STATE_FAILED: {
                    trHtml.append("<td><span class='badge badge-danger'>Failed</span></td>"); break;
                }
                default : {
                    trHtml.append("<td><span class='badge badge-secondary'>Unknown</span></td>"); break;
                }
            } 
            trHtml.append("<td class='ebr-invisible'>" + taskDetail.url + "</td>");
            trHtml.append("<td class='ebr-invisible'>" + taskDetail.depends + "</td>");
            trHtml.appendTo(tmpCardBody.find("#taskDetailRow"));
        }

        tmpCard.appendTo($("#accordionFlowList"));
    },

    UpdateStatusNum : (jsonResultData) => {
        let activeNum = 0;
        let completeNum = 0;
        let failedNum = 0
        for (let schd in jsonResultData) {
            let schdData = jsonResultData[schd];
            for (let key in schdData) {
                if ("active" === key) {
                    activeNum += schdData[key];
                } else if ("complete" === key) {
                    completeNum += schdData[key];
                } else if ("failed" === key) {
                    failedNum += schdData[key];
                }
            }
        }

        $("#labelActiveNum").html(activeNum);
        $("#labelCompleteNum").html(completeNum);
        $("#labelFailedNum").html(failedNum);
    }

};
ebr.state_viewer.ctl = {
    Init : () => {
        ebr.state_viewer.view.Init();
        ebr.com.BindQuery(REQ_ALL_FLOW, ebr.state_viewer.ctl.GetAllFlowRequest, ebr.state_viewer.ctl.GetAllFlowResponse);
        ebr.com.BindQuery(REQ_EXEC_STATISTICS, ebr.state_viewer.ctl.GetExecStatisticsRequest, ebr.state_viewer.ctl.GetExecStatisticsResponse);
        ebr.com.BindQuery(REQ_LAUNCH_FLOW, ebr.state_viewer.ctl.LaunchFlowRequest, ebr.state_viewer.ctl.LaunchFlowResponse);
        ebr.com.BindQuery(REQ_DEL_FLOW, ebr.state_viewer.ctl.DelFlowRequest, ebr.state_viewer.ctl.DelFlowResponse);
    },

    GetAllFlowRequest : function() {
        return {};
    },

    GetAllFlowResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.state_viewer.view.AddFlowDetailView(jsonData.result);
        }
    },

    GetExecStatisticsRequest : function() {
        return {};
    },

    GetExecStatisticsResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.state_viewer.view.UpdateStatusNum(jsonData.result);
        }
    },

    LaunchFlowRequest : function() {
        let taskflow_id = sessionStorage.getItem(SSK_FLOW_ID);
        sessionStorage.removeItem(SSK_FLOW_ID);
        return {  "taskflow_id" : taskflow_id };
    },

    LaunchFlowResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            alert("taskflow started");
        } else if (typeof jsonData.result === "object" && jsonData.error !== null) {
            alert("start a taskflow failed. info:[" + jsonData.error.info + "]");
        }
    },

    DelFlowRequest : function() {
        let taskflow_id = sessionStorage.getItem(SSK_FLOW_ID);
        sessionStorage.removeItem(SSK_FLOW_ID);
        return {  "taskflow_id" : taskflow_id };
    },

    DelFlowResponse : function(jsonData) {
        console.log(jsonData)
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.com.EmitQuery(REQ_ALL_FLOW);
            alert(jsonData.result.info);
        } else if (typeof jsonData.result === "object" && jsonData.error !== null) {
            alert("delete a taskflow failed. info:[" + jsonData.error.info + "]");
        }
    },
};

/********************************************************************************************************
 * 定义浏览页面
 ********************************************************************************************************/
ebr.define_viewer = {};
ebr.define_viewer.view = {
    Init: () => {
        // file selector
        $("#fileSelector").on("change", () => {
            let updFiles = document.getElementById("fileSelector").files;
            ebr.define_viewer.ctl.saveJsonFileContent(updFiles[0]);
        });
        // Setup the dnd listeners.
        let dropZone = document.getElementById("fileContent");
        dropZone.addEventListener("dragover", (e) => {
            e.stopPropagation();
            e.preventDefault();
            e.dataTransfer.dropEffect = "copy"; // Explicitly show this is a copy.
        }, false);
        dropZone.addEventListener("drop", (e) => {
            e.stopPropagation();
            e.preventDefault();
            let updFiles = e.dataTransfer.files;
            ebr.define_viewer.ctl.saveJsonFileContent(updFiles[0]);
        }, false);
        let defineViewerPanel = document.getElementById("flowDefineViewerPanel");
        defineViewerPanel.addEventListener("fileStorageEvent", (e) => {
            ebr.define_viewer.view.updateFlowDefineView(e.detail.fileName);
        });
        $("#fileSelectBtn").on("click", (e) => {
            document.getElementById('fileSelector').click();
        });
        $("#fileClearBtn").on("click", (e) => {
            ebr.define_viewer.view.ClearJsonContent();
        });
        $("#fileValidateBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_VALIDATE_FLOW);
        });
        $("#fileSaveBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_SAVE_FLOW);
        });
    },

    GetFlowDefineFileName : function() {
        return $("#fileNameLabel").html();
    },

    updateFlowDefineView : function(jsonFileName) {
        $("#fileContent").empty();
        $("#taskCardContent").empty()
        let strContent = sessionStorage.getItem(jsonFileName);
        if (typeof strContent === "string" && strContent.trim() !== "") {
            let itemMap = new Map();
            try {
                let jsonContent = JSON.parse(strContent);
                for (let taskKey in jsonContent) {
                    //console.log(taskKey + ":" + JSON.stringify(jsonContent[taskKey]));
                    let taskContent = jsonContent[taskKey];
                    itemMap.set(taskKey, taskContent);
                }
                for (let [taskKey, taskContent] of itemMap) {
                    let groupId = null;
                    if (itemMap.has(taskContent.group)) {
                        groupId = taskContent.group;
                    }
                    ebr.define_viewer.view.createTaskCardItem(taskKey, taskContent, groupId, itemMap);
                    ebr.define_viewer.view.createTaskSrcItem(taskKey, taskContent, groupId, itemMap);
                };
            } catch(err) {
                alert(err.name + " : " + err.message);
            } finally {
                itemMap.clear();
            }
        } else {
            alert("Error: [updateFlowDefineView] The Content of Json File is empty...");
        }
    },

    createTaskCardItem : function(taskId, taskContent, groupId, itemMap) {
        let myTmpCard = $("#taskInfoCard-" + taskId);
        if (myTmpCard.length !== 0) return;
        let tmpCard = $("#taskInfoCard").clone();
        tmpCard.attr("id", "taskInfoCard-" + taskId);
        tmpCard.addClass("ebr-inner-card-body");
        let tmpCardHeader = tmpCard.find("#taskInfoCardHeader");
        tmpCardHeader.attr("id", "taskInfoCardHeader-" + taskId);
        // body
        if (taskContent !== undefined && taskContent !== null) {
            for (let innerElemKey in taskContent) {
                let trHtml = $("<tr></tr>");
                trHtml.append("<td>" + innerElemKey + "</td>");
                trHtml.append("<td class='border-bottom'>" + taskContent[innerElemKey] + "</td>");
                trHtml.appendTo(tmpCard.find("tbody"));
            }
        }
        // header & place
        if (groupId && groupId !== taskId) {
            let headHtml = $("<span class='badge badge-warning'>Unit</span><span style='margin-left: 20px;'><b>" + taskId + "</b></span>");
            headHtml.appendTo(tmpCard.find(".card-header"));
            // parentTask
            let parentTask = $("#taskInfoCard-" + groupId);
            if (parentTask.length === 0) {
                let parentContent = itemMap.get(groupId);
                let ppTaskId = parentContent.group ? parentContent.group : "";
                // create parent card
                ebr.view.createTaskCardItem(groupId, parentContent, ppTaskId, itemMap);
                // get parent card again
                parentTask = $("#taskInfoCard-" + groupId);
            }
            tmpCard.appendTo(parentTask);
            let parentHead = parentTask.find("#taskInfoCardHeader-" + groupId);
            parentHead.find(".badge").remove();
            $("<span class='badge badge-info'>Group</span>").prependTo(parentHead);
        } else if (!groupId || groupId === taskId) {
            let headHtml = $("<span class='badge badge-info'>Group</span><span style='margin-left: 20px;'><b>" + taskId + "</b></span>");
            headHtml.appendTo(tmpCard.find(".card-header"));
            tmpCard.appendTo($("#taskCardContent"));
        }
    },

    createTaskSrcItem : function(taskId, taskContent, parentTaskId) {
        let jsonFileContent = $("#fileContent");
        jsonFileContent.html(jsonFileContent.html() + "\r" + taskId + " : " +JSON.stringify(taskContent));
    },

    ClearJsonContent : function() {
        let fileName = ebr.define_viewer.view.GetFlowDefineFileName();
        sessionStorage.removeItem(fileName);
        $("#fileNameLabel").empty();
        $("#lastModifiedDate").empty();
        $("#fileContent").empty();
        $("#taskCardContent").empty()
        $("#fileSelector").val("");
    },

};
ebr.define_viewer.ctl = {
    Init : () => {
        ebr.com.BindQuery(REQ_VALIDATE_FLOW, ebr.define_viewer.ctl.ValidateFlowRequest, ebr.define_viewer.ctl.ValidateFlowResponse);
        ebr.com.BindQuery(REQ_SAVE_FLOW, ebr.define_viewer.ctl.SaveFlowRequest, ebr.define_viewer.ctl.SaveFlowResponse);
        ebr.define_viewer.view.Init();
    },

    ValidateFlowRequest : function() {
        let fileName = ebr.define_viewer.view.GetFlowDefineFileName();
        let strContent = sessionStorage.getItem(fileName);
        if (typeof strContent !== "string" || strContent.trim() === "") {
            return null;
        }
        return JSON.parse(strContent);
    },

    ValidateFlowResponse : function(jsonData) {
        let resultStr = "unknown result...";
        if (typeof jsonData.result === "object") {
            resultStr = "success...";
        } else if (typeof jsonData.error === "object") {
            resultStr = "failed...";
        }
        alert("validate result : " + resultStr);
    },

    SaveFlowRequest : function() {
        let fileName = ebr.define_viewer.view.GetFlowDefineFileName();
        let strContent = sessionStorage.getItem(fileName);
        if (typeof strContent !== "string" || strContent.trim() === "") {
            alert("Error: [SaveFlowRequest] The Content of JSON file is empty...");
        }
        return JSON.parse(strContent);
    },

    SaveFlowResponse : function(jsonData) {
        let resultStr = "unknown result...";
        if (typeof jsonData.result === "object") {
            resultStr = "success...";
        } else if (typeof jsonData.error === "object") {
            resultStr = "failed...";
        }
        alert("save result : " + resultStr);
    },

    saveJsonFileContent : function(jsonFile) {
        if (jsonFile === undefined || jsonFile === null || jsonFile.type !== "application/json") {
            $("#fileNameLabel").empty();
            $("#lastModifiedDate").empty();
            $("#fileContent").empty();
            $("#taskCardContent").empty()
        } else {
            $("#fileNameLabel").html(jsonFile.name);
            if (jsonFile.lastModifiedDate) { // firefox
                $("#lastModifiedDate").html(jsonFile.lastModifiedDate.toLocaleDateString());
            }
            let reader = new FileReader();
            reader.readAsText(jsonFile);
            reader.onload = (e) => {
                let contentStr = e.target.result;
                sessionStorage.removeItem(jsonFile.name);
                sessionStorage.setItem(jsonFile.name, contentStr);
                let defineViewerPanel = document.getElementById("flowDefineViewerPanel");
                defineViewerPanel.dispatchEvent(new CustomEvent('fileStorageEvent', { bubbles: true, detail: { fileName: jsonFile.name } }));
            };
        }
    },
};

/********************************************************************************************************
 * 服务器信息浏览页面
 ********************************************************************************************************/
ebr.server_info = {};
ebr.server_info.view = {
    Init : () => {},

    AddServerInfoTableView : function(jsonResultData) {
        let infoPanel = $("#serverInfoPanel");
        // environment variables
        if (typeof jsonResultData.env === "object" && jsonResultData.env !== null) {
            $("#envTableBody").empty();
            let rowNum = 1;
            for (var item in jsonResultData.env) {
                let trHtml = $("<tr></tr>");
                trHtml.append("<td>" + rowNum + "</td>");
                trHtml.append("<td>" + item + "</td>");
                trHtml.append("<td>" + jsonResultData.env[item] + "</td>");
                trHtml.appendTo(infoPanel.find("#envTableBody"));
                rowNum++;
                //console.log(item+":"+jsonResultData.env[item]);
            }
        }
        // server config
        if (typeof jsonResultData.config === "object" && jsonResultData.config !== null) {
            $("#serverConfigTableBody").empty();
            let rowNum = 1;
            for (var item in jsonResultData.config) {
                let trHtml = $("<tr></tr>");
                trHtml.append("<td>" + rowNum + "</td>");
                trHtml.append("<td>" + item + "</td>");
                trHtml.append("<td>" + jsonResultData.config[item] + "</td>");
                trHtml.appendTo(infoPanel.find("#serverConfigTableBody"));
                rowNum++;
                //console.log(item+":"+jsonResultData.env[item]);
            }
        }
    },

};
ebr.server_info.ctl = {
    Init : () => {
        ebr.com.BindQuery(REQ_GET_SERVER_INFO, ebr.server_info.ctl.GetServerInfoRequest, ebr.server_info.ctl.GetServerInfoResponse);
    },

    GetServerInfoRequest : function() {
        return {};
    },

    GetServerInfoResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.server_info.view.AddServerInfoTableView(jsonData.result);
        }
    },
};

/********************************************************************************************************
 * 脚本被装载时执行
 ********************************************************************************************************/
(function() {
    ebr.sidebar.ctl.Init();
    ebr.state_viewer.ctl.Init();
    ebr.define_viewer.ctl.Init();
    ebr.server_info.ctl.Init();
})();