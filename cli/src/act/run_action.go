package act

import (
	"log"
)

type RunAction struct {

}

func (act *RunAction) DoAction(target string) {
	log.Println("RunAction...")
}