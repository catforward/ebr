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

import ebr.core.base.ExternalBatchRunner;

/**
 * the builder of EBR Service
 * @author catforward
 */
public class ServiceBuilder {

    private boolean serviceMode = false;
    private int maxWorkerNum = 8;
    private int minWorkerNum = 2;
    private boolean devMode = false;

    public static ServiceBuilder createExternalBatchRunnerBuilder() {
        return new ServiceBuilder();
    }

    public boolean getDevMode() {
        return this.devMode;
    }

    public int getMinWorkerNum() {
        return this.minWorkerNum;
    }

    public int getMaxWorkerNum() {
        return this.maxWorkerNum;
    }

    public boolean isServiceMode() {
        return this.serviceMode;
    }

    public void setServiceMode(boolean serviceMode) {
        this.serviceMode = serviceMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public void setMaxWorkerNum(int maxWorkerNum) {
        this.maxWorkerNum = maxWorkerNum;
    }

    public void setMinWorkerNum(int minWorkerNum) {
        this.minWorkerNum = minWorkerNum;
    }

    public ExternalBatchRunnerService buildExternalBatchRunnerService() {
        return ExternalBatchRunner.getInstance().init(this);
    }
}
