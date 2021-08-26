package actions

import (
	"ebr/symbols"
)

// cli的动作接口
type IAction interface {
	// 动作执行
	DoAction(target string)
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
