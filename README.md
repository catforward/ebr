# EBR (External Batch Runner)

![build](https://img.shields.io/badge/build-passing-green)
[![license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://github.com/catforward/ebr/blob/master/LICENSE)

[中文](./README.zh_CN.md) | [日本語](./README.ja_JP.md)

## Intro

**EBR** is a simple tool used to manage several external process with clear dependencies.
> This is a pure personal project. Update only when adding or modifying features.

## Quick Start
### Flow's Define (folder [/sample_path/ebr/data/FLOW-4.json])

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
> The define file will be read and translated to a graph(DAG) data struct when the flow is ready to run
<br>
![image](docs/sample_task_flow.jpg)

- Server Start
```bash
root@sample-server: /sample_path/ebr/bin/server-startup.sh
```

- CLI
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

root@sample-server: /sample_path/ebr/bin/ebr run -f /FLOW-4
```

- Task's execution order
```bash
T1 --> T4,T2-1,T2-2 --> T2-3 --> T3
```


## Usecase
![image](docs/sample_usecase.jpg)


