"use strict";

const REQ_INFO_GET_SERVER_INFO = "req.info.GetServerInfo";
const REQ_TASK_VALIDATE_TASK_FLOW = "req.task.ValidateTaskFlow";
const REQ_TASK_SAVE_TASK_FLOW = "req.task.SaveTaskFlow";

var ebr = {};

ebr.reqHandlerMap = new Map();
ebr.resHandlerMap = new Map();

ebr.com = {
    BindQuery : function(pathStr, reqFunc, resFunc) {
        if (typeof pathStr !== "string" || pathStr.trim() === ""
            || typeof reqFunc !== "function" || typeof resFunc !== "function") {
            alert("Error: [BindQuery] Invalid Params...");
            return;
        }
        ebr.reqHandlerMap.set(pathStr, reqFunc);
        ebr.resHandlerMap.set(pathStr, resFunc);
    },

    EmitQuery : function(pathStr) {
        if (typeof pathStr !== "string" || pathStr.trim() === "") {
            alert("Error: [EmitQuery] Invalid Params...");
            return;
        }
        if (!ebr.reqHandlerMap.has(pathStr) || !ebr.resHandlerMap.has(pathStr)) {
            alert("Error: [EmitQuery] no handler for [" + pathStr + "]");
            return;
        }
        let reqData = ebr.reqHandlerMap.get(pathStr)();
        if (reqData === undefined || reqData === null) {
            alert("Error: [EmitQuery] request data can not be null or undefined...");
            return;
        }
        ebr.com.postMsg({ path: pathStr, param: reqData}, ebr.resHandlerMap.get(pathStr));
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
        // main panel
        ebr.view.initMainPanel();
        // server info panel
        ebr.view.initServerInfoPanel();
        // task flow status info panel
        ebr.view.initStatusInfoPanel();
        // define viewer panel
        ebr.view.initDefineViewerPanel();
    },

    initMainPanel : function() {
        // sidebar
        $("#serverInfoPanelBtn").on("click", () => {
            ebr.com.EmitQuery(REQ_INFO_GET_SERVER_INFO);
        });
        $("#taskFlowStatusInfoPanelBtn").on("click", () => {
            ebr.view.ShiftPanel("taskFlowStatusInfoPanel");
        });
        $("#taskFlowDefineViewerPanelBtn").on("click", () => {
            ebr.view.ShiftPanel("taskFlowDefineViewerPanel");
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
        let defineViewerPanel = document.getElementById("taskFlowDefineViewerPanel");
        defineViewerPanel.addEventListener("fileStorageEvent", (e) => {
            ebr.view.updateTaskFlowDefineView(e.detail.fileName);
        });
    },

    initStatusInfoPanel : function() {

    },

    initDefineViewerPanel : function() {
        $("#fileSelectBtn").on("click", (e) => {
            document.getElementById('fileSelector').click();
        });
        $("#fileClearBtn").on("click", (e) => {
            ebr.view.ClearJsonContent();
        });
        $("#fileValidateBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_TASK_VALIDATE_TASK_FLOW);
        });
        $("#fileSaveBtn").on("click", (e) => {
            ebr.com.EmitQuery(REQ_TASK_SAVE_TASK_FLOW);
        });
    },

    ShiftPanel : function(panelId) {
        if (panelId !== null) {
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

    /********************  Define Viewer Panel *********************/
    GetTaskFlowDefineFileName : function() {
        return $("#fileNameLabel").html();
    },

    ClearJsonContent : function() {
        let fileName = ebr.view.GetTaskFlowDefineFileName();
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
                let defineViewerPanel = document.getElementById("taskFlowDefineViewerPanel");
                defineViewerPanel.dispatchEvent(new CustomEvent('fileStorageEvent', { bubbles: true, detail: { fileName: jsonFile.name } }));
            };
        }
    },

    updateTaskFlowDefineView : function(jsonFileName) {
        let strContent = sessionStorage.getItem(jsonFileName);
        if (typeof strContent === "string" && strContent.trim() !== "") {
            let itemMap = new Map();
            try {
                let jsonContent = JSON.parse(strContent);
                for (let taskKey in jsonContent) {
                    //console.log(taskKey + ":" + JSON.stringify(jsonContent[taskKey]));
                    let taskContent = jsonContent[taskKey];
                    let parentTaskId = null;
                    if (itemMap.has(taskContent.group)) {
                        parentTaskId = taskContent.group;
                    }
                    ebr.view.createTaskCardItem(taskKey, taskContent, parentTaskId);
                    ebr.view.createTaskSrcItem(taskKey, taskContent, parentTaskId);
                    itemMap.set(taskKey, taskContent);
                }
            } catch(err) {
                alert(err.name + " : " + err.message);
            } finally {
                itemMap.clear();
            }
        } else {
            alert("Error: [updateTaskFlowDefineView] The Content of Json File is empty...");
        }
    },

    createTaskCardItem : function(taskId, taskContent, parentTaskId) {
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
        if (parentTaskId !== undefined && parentTaskId !== null) {
            let headHtml = $("<span class='badge badge-warning'>Unit</span><span style='margin-left: 20px;'><b>" + taskId + "</b></span>");
            headHtml.appendTo(tmpCard.find(".card-header"));
            // parentTask
            let parentTask = $("#taskInfoCard-" + parentTaskId);
            tmpCard.appendTo(parentTask);
            let parentHead = parentTask.find("#taskInfoCardHeader-" + parentTaskId);
            parentHead.find(".badge").remove();
            $("<span class='badge badge-info'>Group</span>").prependTo(parentHead);
        } else {
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
    /********************  Define Viewer Panel *********************/
    ValidateTaskFlowRequest : function() {
        let fileName = ebr.view.GetTaskFlowDefineFileName();
        let strContent = sessionStorage.getItem(fileName);
        if (typeof strContent !== "string" || strContent.trim() === "") {
            return null;
        }
        return JSON.parse(strContent);
    },
    ValidateTaskFlowResponse : function(jsonData) {
        let resultStr = "unknown result...";
        if (typeof jsonData.result === "object") {
            resultStr = "success...";
        } else if (typeof jsonData.error === "object") {
            resultStr = "failed...";
        }
        alert("validate result : " + resultStr);
    },
    SaveTaskFlowRequest : function() {
        let fileName = ebr.view.GetTaskFlowDefineFileName();
        let strContent = sessionStorage.getItem(fileName);
        if (typeof strContent !== "string" || strContent.trim() === "") {
            return null;
        }
        return JSON.parse(strContent);
    },
    SaveTaskFlowResponse : function(jsonData) {
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
    ebr.com.BindQuery(REQ_INFO_GET_SERVER_INFO, ebr.ctl.GetServerInfoRequest, ebr.ctl.GetServerInfoResponse);
    ebr.com.BindQuery(REQ_TASK_VALIDATE_TASK_FLOW, ebr.ctl.ValidateTaskFlowRequest, ebr.ctl.ValidateTaskFlowResponse);
    ebr.com.BindQuery(REQ_TASK_SAVE_TASK_FLOW, ebr.ctl.SaveTaskFlowRequest, ebr.ctl.SaveTaskFlowResponse);
})();