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
package ebr.core;

import java.util.List;

/**
 * <pre>
 * meta info of task
 * </pre>
 * @author catforward
 */
public interface Task {

    /**
     * <pre>
     * task's id
     * </pre>
     * @return id
     */
    String id();

    /**
     * <pre>
     * task's description
     * </pre>
     * @return desc
     */
    String desc();

    /**
     * <pre>
     * the command of task
     * </pre>
     * @return command
     */
    String command();

    /**
     * <pre>
     * the pre condition tasks of this task
     * </pre>
     * @return define str of pre tasks
     */
    List<String> preTasks();

    /**
     * <pre>
     * the sub tasks of this task
     * </pre>
     * @return task's list of children
     */
    List<Task> children();

    /**
     * <pre>
     * the parent of this task
     * </pre>
     * @return parent task
     */
    Task parentTask();
}
