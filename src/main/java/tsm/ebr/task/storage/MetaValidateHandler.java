/**
 * MIT License
 *
 * Copyright (c) 2019 catforward
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package tsm.ebr.task.storage;

import tsm.ebr.base.Handler.HandlerContext;
import tsm.ebr.base.Handler.IHandler;
import tsm.ebr.base.Task.Meta;
import tsm.ebr.base.Task.Type;

import java.util.Map;
import java.util.logging.Logger;

import static tsm.ebr.base.Event.Symbols.EVT_DATA_META_MAP;
import static tsm.ebr.base.Task.Symbols.*;

public class MetaValidateHandler implements IHandler {
    private final static Logger logger = Logger.getLogger(MetaValidateHandler.class.getCanonicalName());
    /**
     * handle this event
     * @param context context of this event
     * @return true: succeeded false: failed
     */
    @Override
    public boolean doHandle(HandlerContext context) {
        Map<String, Meta> urlMetaMap = (Map<String, Meta>) context.getParam(EVT_DATA_META_MAP);
        Meta rootMeta = urlMetaMap.get(KEY_ROOT_UNIT);
        return validate(context, rootMeta, urlMetaMap);
    }

    private boolean validate(HandlerContext context, Meta meta, Map<String, Meta> urlMetaMap) {
        if (meta == null) {
            context.setErrorMessage("the define of unit is not exist!");
            return false;
        }

        // uid
        String uid = (String) meta.symbols.get(KEY_UID);
        if (uid == null || uid.isBlank()) {
            context.setErrorMessage("the define of uid is not exist!");
            return false;
        }
        // url
        String url = (String) meta.symbols.get(KEY_UNIT_URL);
        if (url == null || url.isBlank()) {
            context.setErrorMessage(String.format("[%s]: url is not exist", uid));
            return false;
        }
        if (meta.parent != null) {
            String target = String.format("%s/%s", meta.parent.symbols.get(KEY_UNIT_URL), uid);
            if (!url.equals(target)) {
                context.setErrorMessage(String.format("[%s]: url(%s) is not correct", uid, url));
                return false;
            }
        }
        // type
        String typeStr = (String) meta.symbols.get(KEY_UNIT_TYPE);
        if (!Type.TASK.name().equals(typeStr)
                && !Type.MODULE.name().equals(typeStr)
                && !Type.ROOT.name().equals(typeStr)) {
            context.setErrorMessage(String.format("unknown type (%s)", url));
            return false;
        }
        if (meta.parent != null) {
            String pTypeStr = (String) meta.parent.symbols.get(KEY_UNIT_TYPE);
            if (!Type.TASK.name().equals(typeStr)
                && Type.MODULE.name().equals(pTypeStr)) {
                context.setErrorMessage(String.format("[%s]: can not contain any type of module in any non-task unit", uid));
                return false;
            }
        }
        // command
        String command = (String) meta.symbols.get(KEY_COMMAND);
        if (Type.TASK.name().equals(typeStr) && (command == null || command.isBlank())) {
            context.setErrorMessage(String.format("[%s]: the define onf command is not exist!", uid));
            return false;
        }
        // predecessors
        if (!meta.predecessorUrl.isEmpty()) {
            for (String pUrl : meta.predecessorUrl) {
                if (!urlMetaMap.containsKey(pUrl)
                    || urlMetaMap.get(pUrl) == null) {
                    context.setErrorMessage(String.format("[%s]: the predecessor(%s) is not exist", uid, pUrl));
                    return false;
                }
            }
        }
        // children
        if (!meta.children.isEmpty()) {
            for (Meta child : meta.children) {
                if (!validate(context, child, urlMetaMap)) {
                    return false;
                }
            }
        }
        return true;
    }
}
