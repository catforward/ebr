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
type AbortAction struct {
	api_request  ActionParam
	api_response []byte
}

// Flow/Task执行动作的响应数据结构
type AbortActionRespData struct {
	Code string `json:"code"`
	Msg  string `json:"msg"`
}

// 动作执行
func (act *AbortAction) DoAction(requiredFlow string) {
	log.Println("AbortAction...")
	// 初始化数据
	act.initData(requiredFlow)
	// 数据请求
	act.doRequest()
	// 格式化结果
	act.formatResult()
}

func (act *AbortAction) initData(flow string) {
	if flow == "" {
		log.Fatalf("the target of abort action must be set.")
		return
	}

	innerParam := make(map[string]string)
	innerParam["action"] = "abort"
	innerParam["flow"] = flow

	act.api_request.Id = "api.schd.action"
	act.api_request.Param = innerParam
}

func (act *AbortAction) doRequest() {
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

func (act *AbortAction) formatResult() {
	var res AbortActionRespData
	if err := json.Unmarshal(act.api_response, &res); err != nil {
		fmt.Println(err.Error())
		return
	}

	if res.Code != "0" {
		log.Fatalf("Request Failed... [code:'%s', msg:'%s']", res.Code, res.Msg)
	} else {
		fmt.Println("Request Succeeded...")
	}
}
