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

import tsm.ebr.base.Service;

import static tsm.ebr.base.Event.Symbols.EVT_ACT_LOAD_DEF_FILE;

public class StorageService extends Service {

    public StorageService() {
        super();
    }

    @Override
    public ServiceId id() {
        return ServiceId.STORAGE;
    }

    @Override
    protected void onInit() {
        // 从定义文件读取任务定义
        registerActionHandler(EVT_ACT_LOAD_DEF_FILE, DefineFileLoadHandler.class, MetaValidateHandler.class);
    }

    @Override
    protected void onFinish() {
        unregister(EVT_ACT_LOAD_DEF_FILE);
    }
}
