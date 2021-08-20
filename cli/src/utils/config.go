package utils

import (
	"ebr/symbols"
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

const (
	CONFIG_FILE string = "config.json"
)

// EBR的配置文件
// 具体参见 conf/config.json
type Config struct {
	Http HttpConfig `json:"http"`
}

// 只需要http定义的部分
type HttpConfig struct {
	Address string `json:"address"`
	Port    int    `json:"port"`
}

var EbrConfig *Config

// 初始化
func ConfigInit() {
	configFile := filepath.Join(GetConfPath(), CONFIG_FILE)
	EbrConfig = new(Config)
	EbrConfig.load(configFile)
	// log.Printf("ServerAddress: '%s'", EbrConfig.Http.Address)
	// log.Printf("ServerPort: %d", EbrConfig.Http.Port)
}

func (cfg *Config) load(configFile string) {
	// log.Printf("config load.'%s'.", configFile)
	//ReadFile函数会读取文件的全部内容，并将结果以[]byte类型返回
	jsonData, err := ioutil.ReadFile(configFile)
	if err != nil {
		log.Fatalf("load config error. '%s'", err)
		os.Exit(symbols.FILE_ERROR)
	}
	// 解析config.json
	err = json.Unmarshal(jsonData, &EbrConfig)
	if err != nil {
		log.Fatalf("unmarshal config file error. '%s'", err)
		os.Exit(symbols.JSON_ERROR)
	}
	// localhost
	if EbrConfig.Http.Address == "0.0.0.0" || EbrConfig.Http.Address == "127.0.0.1" {
		EbrConfig.Http.Address = "localhost"
	}
}
