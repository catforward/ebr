# EBR (External Batch Runner)

开发运行环境

- OS：Debian 9
- JDK：OpenJDK 11

依赖

- 无

制作这个小工具主要为了达成以下目标

- 自动化执行若干可并行且包含复杂依赖关系的外部程序
- 用来学习新API，适应新语法，跟进JDK的版本的更新

外部程序包括

- 脚本程序 (shell, bat, python...)
- 无界面的可执行程序

外部程序的执行关系可参考下图

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)


