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

import java.util.Map;

/**
 * <pre>
 * the service event that notice the caller of this lib
 * what event had occurred
 * </pre>
 * @author catforward
 */
public interface ServiceEvent {
    enum Type {
        /* occurred at job's state changed */
        JOB_STATE_CHANGED,
        /* occurred at all jobs done */
        ALL_JOB_FINISHED,
        /* occurred at all internal service finished */
        SERVICE_SHUTDOWN,
    }
    enum Symbols {
        /* identity url of job that the state changed */
        JOB_URL("JOB_URL"),
        /* the new state of job */
        JOB_STATE("JOB_STATE");

        private final String val;
        Symbols(String newValue) {
            val = newValue;
        }
    }

    /**
     * the type of service event
     * @return Type
     */
    Type type();

    /**
     * the payload within service event
     * @return Map
     */
    Map<Symbols, Object> data();
}
