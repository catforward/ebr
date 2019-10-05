![](https://img.shields.io/badge/build-passing-green) ![](https://img.shields.io/badge/language-java-blue.svg) ![](https://img.shields.io/badge/license-MIT-000000.svg) 

# EBR (External Batch Runner)

EBR(External Batch Runner)は明確な依存関係を持つ外部プログラムを並列で実行するツールである。

外部プログラムとは

- スクリプト (shell, bat, python...)
- コマンド(GUIが無いプログラム)

コマンド間の関係は下記図の様

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)


開発環境

- OS: Debian 9
- JDK: OpenJDK 11

使用したライブラリ

- OpenJDK 11

外部コマンド間の関係定義

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

コマンドの使い方

```
/${your_path}/ebr/bin/ebr.sh -f ${your_define_file}.xml
```
