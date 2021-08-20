package utils

import (
	"bytes"
	"encoding/gob"
)

// 将接口数据转化成字节数组对象
func GetBytes(key interface{}) ([]byte, error) {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	err := enc.Encode(key)
	if err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}
