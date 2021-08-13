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

type FlowInfo struct {
	Url              string      `json:"url"`
	State            string      `json:"state"`
	LastModifiedTime string      `json:"lastModifiedTime"`
	FileSize         json.Number `json:"size"`
}

type FlowInfoRespData struct {
	Code string `json:"code"`
	Msg  string `json:"msg"`
	Data struct {
		Flows []FlowInfo `json:"flows"`
	} `json:"data"`
}

type TaskDetail struct {
	Url     string   `json:"url"`
	Type    string   `json:"type"`
	State   string   `json:"state"`
	Script  string   `json:"script"`
	Depends []string `json:"depends"`
}

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

type ShowAction struct {
	api_url   string
	api_param map[string]string
	api_res   []byte
}

func (act *ShowAction) DoAction(target string) {
	// 初始化数据
	act.initData(target)
	// 数据请求
	act.doRequest()
	// 格式化结果
	if target == symbols.ALL {
		act.formatFlowListResult()
	} else {
		act.formatFlowDetailResult()
	}
}

func (act *ShowAction) initData(target string) {
	act.api_param = make(map[string]string)
	if target == symbols.ALL {
		act.api_url = utl.BASE_URL + "/info/flows"
	} else {
		act.api_url = utl.BASE_URL + "/info/flow"
		act.api_param["flow"] = target
	}
}

func (act *ShowAction) doRequest() {
	bytesData, err := json.Marshal(act.api_param)
	if err != nil {
		fmt.Println(err.Error())
		return
	}

	request, err := http.NewRequest("POST", act.api_url, bytes.NewReader(bytesData))
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
	act.api_res = respBytes
	// fmt.Println("response Body:", string(act.api_res))
}

func (act *ShowAction) formatFlowListResult() {
	var res FlowInfoRespData
	if err := json.Unmarshal(act.api_res, &res); err != nil {
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
	if err := json.Unmarshal(act.api_res, &res); err != nil {
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
