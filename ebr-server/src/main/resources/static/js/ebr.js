"use strict";

/**
 * 获取服务器信息
 * 请求：{req: "req.GetServerInfo", param: {空参数}}
 * 正常响应：{req: "req.GetServerInfo", result: {config:{key-value数据}, env:{key-value数据}}}
 */
const REQ_GET_SERVER_INFO = "req.GetServerInfo";

/**
 * 服务器端验证workflow的定义合法性
 * 请求：{req: "req.ValidateWorkflow", param: {workflow的json定义体}}
 * 正常响应：{req: "req.ValidateWorkflow", result: {空数据 }}
 * 异常响应：{req: "req.ValidateWorkflow", error: {空数据 }}
 */
const REQ_VALIDATE_WORKFLOW = "req.ValidateWorkflow";

/**
 * 保存workflow定义
 * 请求：{req: "req.SaveWorkFlow", param: {workflow的json定义体}}
 * 正常响应：{req: "req.SaveWorkFlow", result: {空数据 }}
 * 异常响应：{req: "req.SaveWorkFlow", error: {空数据 }}
 */
const REQ_SAVE_WORKFLOW = "req.SaveWorkflow";
/** 获取所有task flow的定义 */
const REQ_GET_ALL_FLOW = "req.flow.GetAllFlow";
/** 获取指定id的task flow定义及运行状态 */
const REQ_GET_FLOW_STATUS = "req.flow.GetFlowStatus";
/** 启动指定ID的task */
const REQ_RUN_FLOW = "req.flow.RunFlow";
/** 获取指定ID的task的日志信息 */
const REQ_SHOW_FLOW_LOG = "req.flow.ShowLog";

var ebr = {};

ebr.reqHandlerMap = new Map();
ebr.resHandlerMap = new Map();

/** 公共函数 */
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

/** 侧边栏 */
ebr.sidebar = {};
ebr.sidebar.view = {
    Init : () => {
        $("#serverInfoPanelBtn").click(() => {
            ebr.com.EmitQuery(REQ_GET_SERVER_INFO);
        });
        $("#flowStatusInfoPanelBtn").click(() => {
            ebr.sidebar.view.ShiftPanel("flowStatusInfoPanel");
            ebr.com.EmitQuery(REQ_GET_ALL_FLOW);
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
            // sidebar button
            $(".feather").each((index, element) => {
                $(element).removeClass("ebr-icon-32-current");
            });
            $("#" + panelId + "Btn").find(".ebr-icon-32").addClass("ebr-icon-32-current");
        }
    },
};
ebr.sidebar.ctl = {
    Init : () => {
        ebr.sidebar.view.Init();
    }
};

/** 状态浏览页面 */
ebr.state_viewer = {};
ebr.state_viewer.view = {};
ebr.state_viewer.ctl = {};

/** workflow定义浏览页面 */
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
            ebr.com.EmitQuery(REQ_VALIDATE_WORKFLOW);
        });
        $("#fileSaveBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_SAVE_WORKFLOW);
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
        ebr.com.BindQuery(REQ_VALIDATE_WORKFLOW, ebr.define_viewer.ctl.ValidateFlowRequest, ebr.define_viewer.ctl.ValidateFlowResponse);
        ebr.com.BindQuery(REQ_SAVE_WORKFLOW, ebr.define_viewer.ctl.SaveFlowRequest, ebr.define_viewer.ctl.SaveFlowResponse);
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

/** 服务器信息浏览页面 */
ebr.server_info = {};
ebr.server_info.view = {
    Init : () => {},

    AddServerInfoTableView : function(jsonResultData) {
        ebr.sidebar.view.ShiftPanel("serverInfoPanel");
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

// run js source was loaded
(function() {
    ebr.sidebar.ctl.Init();
    ebr.define_viewer.ctl.Init();
    ebr.server_info.ctl.Init();
})();