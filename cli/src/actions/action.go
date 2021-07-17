package actions

import (
	"ebr/consts"
	"fmt"
)

type IAction interface {
	DoAction(target string)
}

func GetAction(cmdName string) IAction {
	switch cmdName {
	case consts.SHOW:
		return new(ShowAction)
	case consts.RUN:
		return new(RunAction)
	default:
		fmt.Println(fmt.Sprintf("unknown command name [%s]", cmdName))
		return nil
	}
}
