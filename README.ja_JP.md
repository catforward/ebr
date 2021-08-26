# EBR (External Batch Runner)

![build](https://img.shields.io/badge/build-passing-green)
[![license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://github.com/catforward/ebr/blob/master/LICENSE)


[English](./README.md) | [中文](./README.zh_CN.md)

## Intro

**EBR**は明確な依存の関係を持つ外部プログラムを並列で実行するツールである。
> 個人プロジェクト、機能の新規や変更次第更新する

## Quick Start
### タスクの定義
例え、下記のような実行対象となるコマンド間の関係定義がある

```json
{
  "flow": {
    "desc": "sample flow-4 (nested hybrid)"
  },
  "T1": {
    "group": "flow", "desc": "test task unit T1", "script": "echo.sh T1"
  },
  "T2": {
    "group": "flow", "desc": "test task unit T2",
    "depends": [ "T1" ]
  },
  "T2-1": {
    "group": "T2", "desc": "test task unit T2-1", "script": "echo.sh T2-1"
  },
  "T2-2": {
    "group": "T2", "desc": "test task unit T2-2", "script": "echo.sh T2-2"
  },
  "T2-3": {
    "group": "T2", "desc": "test task unit T2-3", "script": "echo.sh T2-3",
    "depends": [ "T2-1", "T2-2" ]
  },
  "T3": {
    "group": "flow", "desc": "test task unit T3", "script": "echo.sh T3",
    "depends": [ "T1", "T2" ]
  },
  "T4": {
    "group": "flow", "desc": "test task unit T4", "script": "echo.sh T4",
    "depends": [ "T1" ]
  }
}
```
> サーバ中、下記の様なグラフィック（DAG）構造へ変換される
> ![image](docs/sample_task_flow.jpg)

- サーバ起動
```bash
root@sample-server: /sample_path/ebr/bin/server-startup.sh
```

- CLIツール
```bash
root@sample-server: /sample_path/ebr/bin/ebr show
URL                                       State                 LastModifiedTime          Size(bytes)
-----------------------------------------------------------------------------------------------------
/FLOW-4                                   stored                2021-07-16 19:45:54               881


root@sample-server: /sample_path/ebr/bin/ebr show -f /FLOW-4
URL               Type    State     Depends                             Script
-----------------------------------------------------------------------------------------------------------
/FLOW-4/T4        task    stored    /FLOW-4/T1                          /sample_path/ebr/bin/echo.sh T4
/FLOW-4/T1        task    stored    --                                  /sample_path/ebr/bin/echo.sh T1
/FLOW-4/T2        group   stored    /FLOW-4/T1                          --
/FLOW-4/T2/T2-3   task    stored    /FLOW-4/T2/T2-1, /FLOW-4/T2/T2-2    /sample_path/ebr/bin/echo.sh T2-3
/FLOW-4/T2/T2-1   task    stored    --                                  /sample_path/ebr/bin/echo.sh T2-1
/FLOW-4/T2/T2-2   task    stored    --                                  /sample_path/ebr/bin/echo.sh T2-2
/FLOW-4/T3        task    stored    /FLOW-4/T1, /FLOW-4/T2              /sample_path/ebr/bin/echo.sh T3

root@sample-server: /sample_path/ebr/bin/ebr start -f /FLOW-4
```

- タスクの実行順番
```bash
T1 --> T4,T2-1,T2-2 --> T2-3 --> T3
```


## 使用場面
![image](docs/sample_usecase.jpg)


