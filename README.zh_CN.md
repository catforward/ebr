# EBR (External Batch Runner)

![build](https://img.shields.io/badge/build-passing-green)

README

- [English](./README.md)
- [日本語](./README.ja_JP.md)

EBR(External Batch Runner) 是个人使用的小工具，用来管理并执行若干个有清晰的前后依赖关系的外部命令。

外部程序包括

- 脚本程序 (shell, bat, python...)
- 无界面的可执行程序

例如

- 当有一个如下的依赖定义时

```xml
<?xml version="1.0" encoding="UTF-8"?>
<task id="TaskFlow-1" desc="root group">
    <task id="task-1" desc="run command-1" command="/your/path/command-1.sh"/>
    <task id="task-2" desc="task group-1" depends="task-1">
        <task id="task-2-1" desc="run command-2" command="/your/path/command-2.sh"/>
        <task id="task-2-1" desc="run command-3" command="/your/path/command-3.sh"/>
        <task id="task-2-3" desc="run command-4" depends="task-2-1,task-2-2" command="/your/path/command-4.sh"/>
    </task>
    <task id="task-3" desc="run command-5" depends="task-1,task-2" command="/your/path/command-5.sh"/>
    <task id="task-4" desc="run command-6" depends="task-1" command="/your/path/command-6.sh"/>
</task>
```

- 执行以下命令

```sh
java -jar /${your_path}/ebr-cli.jar -f /${your_path}/your_define.xml
```

- EBR将会解析给定的依赖定义，并转换成如下的有向无环的图结构（DAG），然后按照顺序执行它们

![image](./images/sample_task_flow.jpg)

开发运行环境

- OS: Debian 9
- JDK: OpenJDK 11

依赖

- OpenJDK 11
