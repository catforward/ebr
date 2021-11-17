package actions

import (
	"bytes"
	"ebr/symbols"
	utl "ebr/utils"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/muesli/gotable"
)

// Flow/Task的信息显示动作
type ShowAction struct {
	api_request  ActionParam
	api_response []byte
}

// Flow的显示数据
type FlowInfo struct {
	Url              string      `json:"url"`
	State            string      `json:"state"`
	LastModifiedTime string      `json:"lastModifiedTime"`
	FileSize         json.Number `json:"size"`
}

// Flow一览显示动作的响应数据
type FlowInfoRespData struct {
	Code string `json:"code"`
	Msg  string `json:"msg"`
	Data struct {
		Flows []FlowInfo `json:"flows"`
	} `json:"data"`
}

// Task的显示数据
type TaskDetail struct {
	Url     string   `json:"url"`
	Type    string   `json:"type"`
	State   string   `json:"state"`
	Script  string   `json:"script"`
	Depends []string `json:"depends"`
}

// Flow的详细信息显示动作的响应数据
type FlowDetailRespData struct {
	Code string `json:"code"`
	Msg  string `json:"msg"`
	Data struct {
		Flow struct {
			Url     string       `json:"url"`
			Content []TaskDetail `json:"content"`
		} `json:"flow"`
	} `json:"data"`
}

// 动作执行
func (act *ShowAction) DoAction(requiredFlow string) {
	// 初始化数据
	act.initReqData(requiredFlow)
	// 数据请求
	act.doRequest()
	// 格式化结果
	if requiredFlow == symbols.ALL {
		act.formatFlowListResult()
	} else {
		act.formatFlowDetailResult()
	}
}

func (act *ShowAction) initReqData(requiredFlow string) {
	innerParam := make(map[string]string)
	act.api_request.Id = "api.info.flow_list"
	if requiredFlow != symbols.ALL {
		act.api_request.Id = "api.info.flow_detail"
		innerParam["flow"] = requiredFlow
	}
	act.api_request.Param = innerParam

}

func (act *ShowAction) doRequest() {
	bytesData, err := json.Marshal(act.api_request)
	if err != nil {
		fmt.Println(err.Error())
		return
	}

	request, err := http.NewRequest("POST", utl.BASE_URL, bytes.NewReader(bytesData))
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	request.Header.Set("Content-Type", "application/json;charset=UTF-8")

	client := &http.Client{}
	resp, err := client.Do(request)
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	defer resp.Body.Close()

	respBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	act.api_response = respBytes
	// fmt.Println("response Body:", string(act.api_response))
}

func (act *ShowAction) formatFlowListResult() {
	var res FlowInfoRespData
	if err := json.Unmarshal(act.api_response, &res); err != nil {
		fmt.Println(err.Error())
		return
	}

	tab := gotable.NewTable([]string{"URL", "State", "LastModifiedTime", "Size(bytes)"},
		[]int64{-40, -20, -20, 15}, "No data.")
	for _, flow := range res.Data.Flows {
		tab.AppendRow([]interface{}{flow.Url, flow.State, flow.LastModifiedTime, flow.FileSize.String()})
	}
	tab.Print()
}

func (act *ShowAction) formatFlowDetailResult() {
	var res FlowDetailRespData
	if err := json.Unmarshal(act.api_response, &res); err != nil {
		fmt.Println(err.Error())
		return
	}

	tab := gotable.NewTable([]string{"URL", "Type", "State", "Depends", "Script"},
		[]int64{-30, -10, -10, -40, -40}, "No data.")
	for _, task := range res.Data.Flow.Content {
		depends := ""
		for _, depUrl := range task.Depends {
			if depends == "" {
				depends = depUrl
			} else {
				depends = depends + ", " + depUrl
			}
		}
		if depends == "" {
			depends = "--"
		}
		script := "--"
		if task.Script != "" {
			script = task.Script
		}
		tab.AppendRow([]interface{}{task.Url, task.Type, task.State, depends, script})
	}

	tab.Print()
}
