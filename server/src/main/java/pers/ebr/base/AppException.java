/*
  Copyright 2021 liang gong

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package pers.ebr.base;

import pers.ebr.types.ResultEnum;

/**
 * <pre>App's runtime exception</pre>
 *
 * @author l.gong
 */
public final class AppException extends RuntimeException {
    private static final long serialVersionUID = -2319024343224680740L;
    private final transient IResult reason;

    public AppException(String msg) {
        super(msg);
        reason = ResultEnum.ERROR;
    }

    public AppException(String msg, Throwable cause) {
        super(msg, cause);
        reason = ResultEnum.ERROR;
    }

    public AppException(IResult result) {
        super(result.getMessage());
        reason = result;
    }

    public AppException(IResult result, Throwable cause) {
        super(cause);
        reason = result;
    }

    public IResult getReason() {
        return reason;
    }

}
