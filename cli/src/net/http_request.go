package net

import (
	utl "ebr/utils"
	"log"
	"net/http"
	"strconv"
)

var (
	BASE_URL string
	Client   = &http.Client{}
)

func HttpInit() {
	config := utl.EbrConfig
	BASE_URL = "http://" + config.Http.Address + ":" + strconv.Itoa(config.Http.Port) + "/ebr/api"
	log.Printf("Http base url: '%s'.", BASE_URL)

}

func Request(apiPath string) {
	// TODO
	log.Println("Http Request...")
}
