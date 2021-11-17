package actions

import (
	"ebr/symbols"
)

// Server端的请求数据
type ActionParam struct {
	Id    string            `json:"api"`   // 请求ID
	Param map[string]string `json:"param"` // 请求参数
}

// cli的动作接口
type IAction interface {
	// 动作执行
	DoAction(requiredFlow string)
}

// 已实现的动作
func GetAction(cmdName string) IAction {
	switch cmdName {
	case symbols.SHOW:
		return new(ShowAction)
	case symbols.START:
		return new(StartAction)
	case symbols.ABORT:
		return new(AbortAction)
	default:
		return nil
	}
}
