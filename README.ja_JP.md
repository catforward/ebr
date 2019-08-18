# EBR (External Batch Runner)

開発環境

- OS：Debian 9
- JDK：OpenJDK 11

使用したライブラリ

- 無し

下記（主な）目標を持ってこのツールを開発した

- 並列実行可能、かつ複雑なタスクの依存関係を持つ外部プログラムを実行する
- JDKのバージョンアップに伴う新しいAPIや文法を学ぶ

外部コマンドとは

- スクリプト (shell, bat, python...)
- GUIが無いアプリ

コマンド間の関係は下記図の様

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)

