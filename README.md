# EBR (External Batch Runner)

Development Environment

- OS：Debian 9
- JDK：OpenJDK 11

Dependency

- Jackson：Analyzing JSON data 
- Guava：Using the EventBus module

Motivation

- Execute a set of external parallel programs with complex task dependencies
- A practice project to write with new api and new grammar

External Program

- A script program (ex. shell script, windows bat, python script ...)
- Executable programs which had no GUI mode

Procedural flaw

example like this

![image](https://github.com/catforward/ebr/raw/master/images/sample_task_flow.jpg)

- A target program can be executed only when its predecessors had completely done
- All successors, which belong target program will be notified when target program is done
