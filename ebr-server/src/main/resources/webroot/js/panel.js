"use strict";

var ebr = {};

ebr.reqHandlerMap = new Map();
ebr.resHandlerMap = new Map();

ebr.com = {
    BindProc : function(pathStr, reqFunc, resFunc) {
        if (typeof pathStr !== "string" || pathStr.trim() === ""
            || typeof reqFunc !== "function" || typeof resFunc !== "function") {
            console.error("Invalid Params...");
            return;
        }
        ebr.reqHandlerMap.set(pathStr, reqFunc);
        ebr.resHandlerMap.set(pathStr, resFunc);
    },

    EmitProc : function(pathStr) {
        if (typeof pathStr !== "string" || pathStr.trim() === "") {
            console.error("Invalid Params...");
            return;
        }
        if (!ebr.reqHandlerMap.has(pathStr) || !ebr.resHandlerMap.has(pathStr)) {
            console.error("no handler for [" + pathStr + "]");
            return;
        }
        let reqData = ebr.reqHandlerMap.get(pathStr)();
        ebr.com.postMsg({ path: pathStr, param: reqData}, ebr.resHandlerMap.get(pathStr));
    },

    postMsg : function(jsonData, resHandler) {
        if (jsonData === null || typeof jsonData !== "object" || typeof resHandler !== "function") {
            console.error("Invalid Params...");
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
    /* ServerInfoPanel */
    AddServerInfoTableView : function(jsonResultData) {
        // environment variables
        if (typeof jsonResultData.env === "object" && jsonResultData.env !== null) {
            let rowNum = 1;
            for (var item in jsonResultData.env) {
                let trHtml = $("<tr></tr>");
                trHtml.append("<td>" + rowNum + "</td>");
                trHtml.append("<td>" + item + "</td>");
                trHtml.append("<td>" + jsonResultData.env[item] + "</td>");
                trHtml.appendTo("#envTableBody");
                rowNum++;
                //console.log(item+":"+jsonResultData.env[item]);
            }
        }
    },
};

ebr.ctl = {
    GetServerInfoRequest : function() {
        return {};
    },
    GetServerInfoResponse : function(jsonData) {
        if (typeof jsonData.result === "object" && jsonData.result !== null) {
            ebr.view.AddServerInfoTableView(jsonData.result);
        }
    },
};

// run js source was loaded
(function() {
    ebr.com.BindProc("info.GetServerInfo", ebr.ctl.GetServerInfoRequest, ebr.ctl.GetServerInfoResponse);
})();