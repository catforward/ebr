package utils

import (
	"strconv"
)

var (
	BASE_URL string
)

// 初始化API清酒接口的基本URL
func HttpInit() {
	config := EbrConfig
	BASE_URL = "http://" + config.Http.Address + ":" + strconv.Itoa(config.Http.Port) + "/ebr/api"
	// log.Printf("Http base url: '%s'.", BASE_URL)

}
