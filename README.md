![](https://img.shields.io/badge/build-passing-green) ![](https://img.shields.io/badge/language-java-blue.svg) ![](https://img.shields.io/badge/license-MIT-000000.svg) 

# EBR (External Batch Runner)

EBR(External Batch Runner) , a small tool used to execute several external commands with clear front-end dependencies.

External Program

- A script program (ex. shell script, windows bat, python script ...)
- Executable programs without GUI

Front-End Dependencies:

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)

Development Environment

- OS: Debian 9
- JDK: OpenJDK 11

Dependency

- OpenJDK 11

Definition of Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<task id="TASK_FLOW" desc="root group">
    <task id="T1" desc="run command-1"
          command="/your/command-1"/>
    <task id="T2" desc="command group" pre_tasks="T1">
        <task id="T2-1" desc="run command-2"
              command="/your/command-2"/>
        <task id="T2-1" desc="run command-3"
                      command="/your/command-3"/>
        <task id="T2-3" desc="run command-4" pre_tasks="T2-1,T2-2"
              command="/your/command-4"/>
    </task>
    <task id="T3" desc="run command-5" pre_tasks="T1,T2"
          command="/your/command-5"/>
    <task id="T4" desc="run command-6" pre_tasks="T1"
          command="/your/command-6"/>
</task>
```

Usage

```
/${your_path}/ebr/bin/ebr.sh -f ${your_define_file}.xml
```


