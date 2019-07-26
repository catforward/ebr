# EBR (External Batch Runner)

開発環境

- OS：Debian 9
- JDK：OpenJDK 11

使用したライブラリ

- Jackson：JSON解析
- Guava：Eventbus機能とGraphicsデータ構造

下記（主な）目標を持ってこのツールを開発した

- 前後関係がある複数個の外部コマンドを実行実行する為
- JDKのバージョンアップに伴う新しいAPIや文法を学ぶ

外部コマンドとは

- スクリプト (shell, bat, python...)
- GUIが無いアプリ

コマンド間の関係は下記図の様

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)

- predecessors：一つのコマンドはゼロ～複数個の前任者（コマンド）を持つのが可能，すべての前任者（コマンド）は正常終了のときのみ、対象コマンドを実行する
- successors：一つのコマンドはゼロ～複数個の後続者（コマンド）を持つのが可能，対象コマンドは正常終了のとき、全ての後続者（コマンド）へ通知する

