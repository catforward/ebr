var ebr = {

    procBind : function(pathStr, reqFunc, resFunc) {
    },

    sendTestMsg: function() {
        // test func
        $.ajax({
          type: "POST",
          url: "/proc/GetEnvVars",
          contentType: 'application/json',
          data: JSON.stringify({ name: "John", time: "2pm" }),
          success: function(data, textStatus, jqXHR) {
            console.log("recv data:"+ data);
          }
        });
    },
};

ebr.req = {};

ebr.res = {};

// run when page loaded
(function() {
    // TODO
    //ebr.sendTestMsg();
})();