package utils

import (
	"ebr/consts"
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

const (
	CONFIG_FILE string = "config.json"
)

// 只需要http定义的部分
// 具体参见 conf/config.json
type HttpConfig struct {
	Address string `json:"address"`
	Port    int    `json:"port"`
}

type Config struct {
	Http HttpConfig `json:"http"`
}

var EbrConfig *Config

func ConfigInit() {
	log.Printf("GetBinPath: '%s'", GetBinPath())
	log.Printf("GetConfPath: '%s'", GetConfPath())
	configFile := filepath.Join(GetConfPath(), CONFIG_FILE)
	EbrConfig = new(Config)
	EbrConfig.load(configFile)
	log.Printf("ServerAddress: '%s'", EbrConfig.Http.Address)
	log.Printf("ServerPort: %d", EbrConfig.Http.Port)
}

func (cfg *Config) load(configFile string) {
	log.Printf("config load.'%s'.", configFile)
	//ReadFile函数会读取文件的全部内容，并将结果以[]byte类型返回
	jsonData, err := ioutil.ReadFile(configFile)
	if err != nil {
		log.Fatalf("load config error. '%s'", err)
		os.Exit(consts.FILE_ERROR)
	}
	// 解析config.json
	err = json.Unmarshal(jsonData, &EbrConfig)
	if err != nil {
		log.Fatalf("unmarshal config file error. '%s'", err)
		os.Exit(consts.JSON_ERROR)
	}
	// localhost
	if EbrConfig.Http.Address == "0.0.0.0" || EbrConfig.Http.Address == "localhost" {
		EbrConfig.Http.Address = "127.0.0.1"
	}
}
