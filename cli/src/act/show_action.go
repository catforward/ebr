package act

import (
	"log"
)

type ShowAction struct {

}

func (act *ShowAction) DoAction(target string) {
	log.Println("ShowAction...")
}