package actions

import (
	"bytes"
	utl "ebr/utils"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
)

// Flow/Task执行动作
type RunAction struct {
	api_url   string
	api_param map[string]string
	api_res   []byte
}

// Flow/Task执行动作的响应数据结构
type RunActionRespData struct {
	Code string `json:"code"`
	Msg  string `json:"msg"`
}

// 动作执行
func (act *RunAction) DoAction(target string) {
	log.Println("RunAction...")
	// 初始化数据
	act.initData(target)
	// 数据请求
	act.doRequest()
	// 格式化结果
	act.formatResult()
}

func (act *RunAction) initData(flow string) {
	if flow == "" {
		log.Fatalf("the target of run action must be set.")
		return
	}
	act.api_url = utl.BASE_URL + "/schd/action"
	act.api_param = make(map[string]string)
	act.api_param["action"] = "start"
	act.api_param["flow"] = flow
}

func (act *RunAction) doRequest() {
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

func (act *RunAction) formatResult() {
	var res RunActionRespData
	if err := json.Unmarshal(act.api_res, &res); err != nil {
		fmt.Println(err.Error())
		return
	}

	if res.Code != "0" {
		log.Fatalf("Request Failed... [code:'%s', msg:'%s']", res.Code, res.Msg)
	} else {
		fmt.Println("Request Succeeded...")
	}
}
