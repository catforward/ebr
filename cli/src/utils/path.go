package utils

import (
	"ebr/symbols"
	"log"
	"os"
	"path"
	"path/filepath"
	"runtime"
	"strings"
)

// 获得bin所在路径
// 假设可执行文件部署在bin目录下
func GetBinPath() string {
	return getCurrentAbsPath()
}

// 获得conf所在路径
// 假设conf目录与bin目录平级
func GetConfPath() string {
	parentPath := filepath.Dir(getCurrentAbsPath())
	confPath := filepath.Join(parentPath, symbols.CONF)
	return confPath
}

// 最终方案-全兼容
func getCurrentAbsPath() string {
	dir := getCurrentAbsPathByExecutable()
	tmpDir, _ := filepath.EvalSymlinks(os.TempDir())
	if strings.Contains(dir, tmpDir) {
		return getCurrentAbsPathByCaller()
	}
	return dir
}

// 获取当前执行文件绝对路径
func getCurrentAbsPathByExecutable() string {
	exePath, err := os.Executable()
	if err != nil {
		log.Fatal(err)
	}
	res, _ := filepath.EvalSymlinks(filepath.Dir(exePath))
	return res
}

// 获取当前执行文件绝对路径（go run）
func getCurrentAbsPathByCaller() string {
	var absPath string
	_, filename, _, ok := runtime.Caller(0)
	if ok {
		absPath = path.Dir(filename)
	}
	return absPath
}
