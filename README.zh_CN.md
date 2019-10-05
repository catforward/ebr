![](https://img.shields.io/badge/build-passing-green) ![](https://img.shields.io/badge/language-java-blue.svg) ![](https://img.shields.io/badge/license-MIT-000000.svg) 

# EBR (External Batch Runner)

EBR(External Batch Runner) 如其名，一个用来执行若干个有清晰的前后依赖关系的外部命令的小工具。

外部程序包括

- 脚本程序 (shell, bat, python...)
- 无界面的可执行程序等等

外部程序的执行关系可参考下图

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)


开发运行环境

- OS: Debian 9
- JDK: OpenJDK 11

依赖

- OpenJDK 11

外部命令的依赖定义

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

用法

```
/${your_path}/ebr/bin/ebr.sh -f ${your_define_file}.xml
```


