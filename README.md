# EBR (External Batch Runner)

![build](https://img.shields.io/badge/build-passing-green)

README

- [中文](./README.zh_CN.md)
- [日本語](./README.ja_JP.md)

Note: This is a pure personal study project. It's means that it have no design documents and have no the best practices, just coding following my mind.

EBR(External Batch Runner) , a simple tool used to execute several external programs with clear dependencies.

External Program

- A script program (i.e. shell script, windows bat, python script ...)
- Executable binary programs without GUI

For example

we have a definition of program's dependencies like this below

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

when we kick this command

```sh
java -jar /${your_path}/ebr-cli.jar -f /${your_path}/your_define.xml
```

EBR will parse the definition to a directed acyclic graph (DAG), and then, execute them as we defined.

![image](ebr-docs/sample_task_flow.jpg)

Development Environment

- OS: Debian 9
- JDK: OpenJDK 11

Dependency

- OpenJDK 11
