# EBR (External Batch Runner)

开发运行环境

- OS：Debian 9
- JDK：OpenJDK 11

依赖

- Jackson：JSON解析
- Guava：Eventbus和图数据结构

制作这个小工具主要为了达成以下目标

- 自动化执行若干个基于流程的外部程序
- 用来学习新API，适应新语法，跟进JDK的版本的更新

外部程序包括

- 脚本程序 (shell, bat, python...)
- 无界面的可执行程序

外部程序的执行关系可参考下图

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)

- 前驱：每个程序可以有零到若干个前驱程序，当前驱程序全部正常结束后方可执行
- 后继：每个程序可以有零到若干个后继程序，当程序结束后通知后继程序检查各自的启动条件

TODO:

- [] 异常处理完全没有考虑，必须补上
- [] 没有做单元测试，必须补上
- [] 整理日志输出内容
