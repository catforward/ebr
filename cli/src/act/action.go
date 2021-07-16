package act

import (
	"fmt"
)

type IAction interface {
	DoAction(target string)
}

func GetAction(actName string) IAction {
	switch actName {
	case "show":
		return new(ShowAction)
	case "run":
		return new(RunAction)
	default:
		fmt.Println(fmt.Sprintf("unknown action name [%s]", actName))
		return nil
	}
}