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
package tsm.ebr.task.storage;

import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Type;
import tsm.ebr.base.Task.Unit;

import static tsm.ebr.base.Message.Symbols.MSG_DATA_TASK_ROOT_UNIT;

/**
 * <pre>
 * 验证创建的任务定义对象
 * </pre>
 * @author catforward
 */
public class MetaValidateHandler implements IHandler {

    /**
     * <pre>
     * handle this event
     * </pre>
     * @param context context of this event
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        Unit rootUnit = (Unit) context.getParam(MSG_DATA_TASK_ROOT_UNIT);
        return validate(context, rootUnit);
    }

    /**
     * <pre>
     * 验证生成的任务单元的基本结构是否正确
     * </pre>
     * @param context
     * @param unit
     * @return boolean
     */
    private boolean validate(HandlerContext context, Unit unit) {
        if (unit == null) {
            context.setErrorMessage("the define of unit is not exist!");
            return false;
        }

        // uid
        if (unit.uid == null || unit.uid.isBlank()) {
            context.setErrorMessage("the define of uid is not exist!");
            return false;
        }
        // url
        if (unit.url == null || unit.url.isBlank()) {
            context.setErrorMessage(String.format("[%s]: url is not exist", unit.uid));
            return false;
        }
        if (unit.parent != null) {
            String target = String.format("%s/%s", unit.parent.url, unit.uid);
            if (!unit.url.equals(target)) {
                context.setErrorMessage(String.format("[%s]: url(%s) is not correct", unit.uid, unit.url));
                return false;
            }
        }
        // type
        if (Type.TASK != unit.type && Type.MODULE != unit.type && Type.ROOT != unit.type) {
            context.setErrorMessage(String.format("unknown type (%s)", unit.url));
            return false;
        }
        if (unit.parent != null) {
            if (Type.TASK != unit.type && Type.MODULE == unit.parent.type) {
                context.setErrorMessage(String.format("[%s]: can not contain a module in any non-task unit", unit.uid));
                return false;
            }
        }
        // command
        if (Type.TASK == unit.type) {
            if (unit.command == null || unit.command.isBlank()) {
                context.setErrorMessage(String.format("[%s]: the define onf command is not exist!", unit.uid));
                return false;
            }
        }
        // children
        if (!unit.children.isEmpty()) {
            for (Unit child : unit.children) {
                if (!validate(context, child)) {
                    return false;
                }
            }
        }
        return true;
    }
}
