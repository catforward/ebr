package actions

import (
	"ebr/symbols"
)

type IAction interface {
	DoAction(target string)
}

func GetAction(cmdName string) IAction {
	switch cmdName {
	case symbols.SHOW:
		return new(ShowAction)
	case symbols.RUN:
		return new(RunAction)
	default:
		return nil
	}
}
