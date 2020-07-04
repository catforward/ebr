"use strict";

/** 获取服务器信息 */
const REQ_GET_SERVER_INFO = "req.info.GetServerInfo";
/** 服务器端验证task flow的定义合法性 */
const REQ_VALIDATE_FLOW = "req.flow.ValidateFlow";
/** 保存task flow定义 */
const REQ_SAVE_FLOW = "req.flow.SaveFlow";
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
        ebr.com.postMsg({ path: topic, param: reqData}, ebr.resHandlerMap.get(topic));
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

ebr.view = {

    /******************** common *********************/
    Init : function() {
        // sidebar
        ebr.view.initSideBar();
        // server info panel
        ebr.view.initServerInfoPanel();
        // task flow status info panel
        ebr.view.initStatusInfoPanel();
        // define viewer panel
        ebr.view.initDefineViewerPanel();
    },

    initSideBar : function() {
        $("#serverInfoPanelBtn").click(() => {
            ebr.com.EmitQuery(REQ_GET_SERVER_INFO);
        });
        $("#flowStatusInfoPanelBtn").click(() => {
            ebr.view.ShiftPanel("flowStatusInfoPanel");
        });
        $("#flowDefineViewerPanelBtn").click(() => {
            ebr.view.ShiftPanel("flowDefineViewerPanel");
        });
    },

    initServerInfoPanel : function() {
        // file selector
        $("#fileSelector").on("change", () => {
            let updFiles = document.getElementById("fileSelector").files;
            ebr.view.saveJsonFileContent(updFiles[0]);
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
            ebr.view.saveJsonFileContent(updFiles[0]);
        }, false);
        let defineViewerPanel = document.getElementById("flowDefineViewerPanel");
        defineViewerPanel.addEventListener("fileStorageEvent", (e) => {
            ebr.view.updateFlowDefineView(e.detail.fileName);
        });
    },

    initStatusInfoPanel : function() {
        $("#getAllFlowBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_GET_ALL_FLOW);
        });
    },

    initDefineViewerPanel : function() {
        $("#fileSelectBtn").on("click", (e) => {
            document.getElementById('fileSelector').click();
        });
        $("#fileClearBtn").on("click", (e) => {
            ebr.view.ClearJsonContent();
        });
        $("#fileValidateBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_VALIDATE_FLOW);
        });
        $("#fileSaveBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_SAVE_FLOW);
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

    /********************  ServerInfoPanel *********************/
    AddServerInfoTableView : function(jsonResultData) {
        ebr.view.ShiftPanel("serverInfoPanel");
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

    /******************** TaskFlow Status Info Panel ********************/
    AddFlowInfoListView : function(jsonResultData) {
        $("#flowList").empty();
        $("#taskStatusCardContent").empty();
        let flowList = $("#flowList");
        flowList.empty();
        for (let item in jsonResultData) {
            let flowId = jsonResultData[item];
            let liHtml = $("<li class=\"list-group-item list-group-item-action\" " +
                            "onclick=\"ebr.view.FlowIdSelect('" + flowId + "')\">" + flowId + "</li>");
            liHtml.appendTo(flowList);
        }
    },
    FlowIdSelect : function(flowId) {
        sessionStorage.setItem("current_flow_id", flowId);
        ebr.com.EmitQuery(REQ_GET_FLOW_STATUS);
    },
    ShowLogOf : function(taskHeaderItem) {
        if (taskHeaderItem && taskHeaderItem.name) {
            sessionStorage.setItem("current_cmd_target", taskHeaderItem.name);
            ebr.com.EmitQuery(REQ_SHOW_LOG);
        }
    },
    RunFlow : function(taskHeaderItem) {
        if (taskHeaderItem && taskHeaderItem.name) {
            sessionStorage.setItem("current_cmd_target", taskHeaderItem.name);
            ebr.com.EmitQuery(REQ_RUN_FLOW);
        }
    },
    AddTaskStatusInfoToView : function(jsonResultData) {
        $("#taskStatusCardContent").empty();
        let currentFlowId = sessionStorage.getItem("current_flow_id");
        let defineJsonContent = ebr.view.getFlowDefine(currentFlowId, jsonResultData);
        if (defineJsonContent) {
            ebr.view.updateTaskStatusInfoView(currentFlowId, defineJsonContent);
        }
        sessionStorage.removeItem("current_flow_id");
    },
    getFlowDefine : function(currentFlowId, jsonResultData) {
        for (let flowId in jsonResultData) {
            if (currentFlowId === flowId) {
                return jsonResultData[flowId];
            }
        }
        return "";
    },
    updateTaskStatusInfoView : function(currentFlowId, defineJsonContent) {
        $("#taskStatusCardContent").empty()
        let itemMap = new Map();
        try {
            // create a item map pool
            for (let taskKey in defineJsonContent) {
                let taskContent = defineJsonContent[taskKey];
                itemMap.set(taskKey, taskContent);
            }
            for (let [taskKey, taskContent] of itemMap) {
                let groupId = null;
                if (itemMap.has(taskContent.group)) {
                    groupId = taskContent.group;
                }
                ebr.view.createTaskStatusCardItem(taskKey, taskContent, groupId, itemMap);
            };
        } catch(err) {
            alert(err.name + " : " + err.message);
        } finally {
            itemMap.clear();
        }
    },
    createTaskStatusCardItem : function(taskId, taskContent, groupId, itemMap) {
        let existCard = $("#taskStatusCard-" + taskId);
        if (existCard.length !== 0) return;
        let tmpCard = $("#taskStatusCard").clone();
        tmpCard.attr("id", "taskStatusCard-" + taskId);
        tmpCard.addClass("ebr-inner-card-body");
        let tmpCardHeader = tmpCard.find("#taskStatusCardHeader");
        tmpCardHeader.attr("id", "taskStatusCardHeader-" + taskId);
        // update collapse info
        tmpCardHeader.attr("data-target", "#collapseOne-" + taskId);
        tmpCardHeader.attr("aria-controls", "collapseOne-" + taskId);
        let tmpCollapseDiv = tmpCard.find("#collapseOne");
        tmpCollapseDiv.attr("id", "collapseOne-" + taskId);
        // body
        if (taskContent) {
            for (let innerElemKey in taskContent) {
                let trHtml = $("<tr></tr>");
                trHtml.append("<td>" + innerElemKey + "</td>");
                trHtml.append("<td class='border-bottom'>" + taskContent[innerElemKey] + "</td>");
                trHtml.appendTo(tmpCard.find("tbody"));
            }
        }
        // header & place
        if (groupId && groupId !== taskId) {
            // delete card btn ul
            tmpCard.find(".ebr-card-flex-bar-btn-ul").remove();
            let headHtml = $("<span class='badge badge-warning'>Unit</span><span style='margin-left: 20px;'><b>" + taskId + "</b></span>");
            headHtml.appendTo(tmpCard.find("#taskStatusCardHeader-" + taskId));
            // parentTask
            let parentTask = $("#taskStatusCard-" + groupId);
            if (parentTask.length === 0) {
                let parentContent = itemMap.get(groupId);
                let ppTaskId = parentContent.group ? parentContent.group : "";
                // create parent card
                ebr.view.createTaskStatusCardItem(groupId, parentContent, ppTaskId, itemMap);
                // get parent card again
                parentTask = $("#taskStatusCard-" + groupId);
            }
            tmpCard.appendTo(parentTask);
            let parentHead = parentTask.find("#taskStatusCardHeader-" + groupId);
            parentHead.find(".badge").remove();
            $("<span class='badge badge-info'>Group</span>").prependTo(parentHead);
        } else if (!groupId || groupId === taskId) {
            // update card btn ul
            tmpCard.find(".ebr-card-flex-bar-btn-link").each((index, element) => {
                $(element).attr("name", taskId);
            });
            let headHtml = $("<span class='badge badge-info'>Group</span><span style='margin-left: 20px;'><b>" + taskId + "</b></span>");
            headHtml.appendTo(tmpCard.find("#taskStatusCardHeader-" + taskId));
            tmpCard.appendTo($("#taskStatusCardContent"));
        }
    },

    /********************  Define Viewer Panel *********************/
    GetFlowDefineFileName : function() {
        return $("#fileNameLabel").html();
    },

    ClearJsonContent : function() {
        let fileName = ebr.view.GetFlowDefineFileName();
        sessionStorage.removeItem(fileName);
        $("#fileNameLabel").empty();
        $("#lastModifiedDate").empty();
        $("#fileContent").empty();
        $("#taskCardContent").empty()
        $("#fileSelector").val("");
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
                    ebr.view.createTaskCardItem(taskKey, taskContent, groupId, itemMap);
                    ebr.view.createTaskSrcItem(taskKey, taskContent, groupId, itemMap);
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

};

ebr.ctl = {
    /********************  ServerInfoPanel *********************/
    GetServerInfoRequest : function() {
        return {};
    },
    GetServerInfoResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.view.AddServerInfoTableView(jsonData.result);
        }
    },

    /********************  Flow Status Info Panel *********************/
    GetAllFlowRequest : function() {
        return {};
    },
    GetAllFlowResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.view.AddFlowInfoListView(jsonData.result);
        }
    },
    GetFlowStatusRequest : function() {
        let flowId = sessionStorage.getItem("current_flow_id");
        if (typeof flowId === "string" && flowId.trim() !== "") {
            return { id : flowId };
        } else {
            alert("Error: [GetTaskFlowStatusRequest] The Content of [current_flow_id] is empty...");
        }
    },
    GetFlowStatusResponse : function(jsonData) {
        if (typeof jsonData.result === "object") {
            if (jsonData.result) {
                ebr.view.AddTaskStatusInfoToView(jsonData.result);
            } else if (jsonData.error) {
                alert("load result : failed...");
                if (jsonData.error.info) {
                    alert("error message : " + jsonData.error.info);
                }
            }
        }
    },
    SendCmdRequest : function() {
        let target = sessionStorage.getItem("current_cmd_target");
        if (typeof target === "string" && target.trim() !== "") {
            return { id : target };
        } else {
            alert("Error: [SendCmdRequest] The Content of [current_cmd_target] is empty...");
        }
    },
    SendCmdResponse : function(jsonData) {
        if (typeof jsonData.result === "object") {
            if (jsonData.result) {
                // TODO
            } else if (jsonData.error) {
                alert("load result : failed...");
                if (jsonData.error.info) {
                    alert("error message : " + jsonData.error.info);
                }
            }
        }
    },

    /********************  Define Viewer Panel *********************/
    ValidateFlowRequest : function() {
        let fileName = ebr.view.GetFlowDefineFileName();
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
        let fileName = ebr.view.GetFlowDefineFileName();
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
};

// run js source was loaded
(function() {
    /********************  ServerInfoPanel *********************/
    ebr.com.BindQuery(REQ_GET_SERVER_INFO, ebr.ctl.GetServerInfoRequest, ebr.ctl.GetServerInfoResponse);

    /********************  TaskFlow Status Info Panel *********************/
    ebr.com.BindQuery(REQ_GET_ALL_FLOW, ebr.ctl.GetAllFlowRequest, ebr.ctl.GetAllFlowResponse);
    ebr.com.BindQuery(REQ_GET_FLOW_STATUS, ebr.ctl.GetFlowStatusRequest, ebr.ctl.GetFlowStatusResponse);
    ebr.com.BindQuery(REQ_SHOW_FLOW_LOG, ebr.ctl.SendCmdRequest, ebr.ctl.SendCmdResponse);
    ebr.com.BindQuery(REQ_RUN_FLOW, ebr.ctl.SendCmdRequest, ebr.ctl.SendCmdResponse);

    /********************  Define Viewer Panel *********************/
    ebr.com.BindQuery(REQ_VALIDATE_FLOW, ebr.ctl.ValidateFlowRequest, ebr.ctl.ValidateFlowResponse);
    ebr.com.BindQuery(REQ_SAVE_FLOW, ebr.ctl.SaveFlowRequest, ebr.ctl.SaveFlowResponse);

})();