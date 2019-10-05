/*
  MIT License

  Copyright (c) 2019 catforward

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.

 */
package ebr.cli;

import ebr.core.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Task实现类
 * 保存任务定义的基本属性
 * @author catforward
 */
public final class TaskImpl implements Task {
    private static final int INIT_CAP = 8;

    private final String id;
    private final TaskImpl parent;
    final List<Task> children = new ArrayList<>(INIT_CAP);
    final List<String> preTask = new ArrayList<>(INIT_CAP);
    String desc;
    String command;

    TaskImpl(String id, TaskImpl parent) {
        this.id = id;
        this.parent = parent;
    }

    /**
     * <pre>
     * task's id
     * </pre>
     *
     * @return id
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * <pre>
     * task's description
     * </pre>
     *
     * @return desc
     */
    @Override
    public String desc() {
        return desc;
    }

    /**
     * <pre>
     * the command of task
     * </pre>
     *
     * @return command
     */
    @Override
    public String command() {
        return command;
    }

    /**
     * <pre>
     * the pre condition tasks of this task
     * </pre>
     *
     * @return define str of pre tasks
     */
    @Override
    public List<String> preTasks() {
        return preTask;
    }

    /**
     * <pre>
     * the sub tasks of this task
     * </pre>
     *
     * @return task's list of children
     */
    @Override
    public List<Task> children() {
        return children;
    }

    /**
     * <pre>
     * the parent of this task
     * </pre>
     *
     * @return parent task
     */
    @Override
    public Task parentTask() {
        return parent;
    }
}
