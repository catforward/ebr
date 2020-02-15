/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.ebr.cli.util;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * <pre>
 * 命令行参数解析
 * </pre>
 * @author l.gong
 */
public class GetOpts {
	/** 定义的参数格式 */
	private final String optStr;
	/** 参数集合迭代器 */
	private final ListIterator<OptArgPair> optArgPairListIterator;
	/** 解析中参数及参数值 */
	private OptArgPair currentPair;

	public GetOpts(String[] args, String optString) {
		optStr = optString;
		/* 解析出来的参数及参数值集合 */
		ArrayList<OptArgPair> opts = new ArrayList<>();

		for (String token : args) {
			if ("-".equals(token) || token.startsWith("--")) {
				break;
			}

			int tokenLen = token.length();
			char firstChar = token.charAt(0);
			// -t 这种单字符参数
			if (tokenLen == 2 && '-' == firstChar) {
				opts.add(new OptArgPair(token.charAt(1)));
			}
			// -tse 这种复数个字符参数，分割后加入参数列表
			else if (tokenLen > 2 && '-' == firstChar) {
				for (int j = 1; j < tokenLen; ++j) {
					opts.add(new OptArgPair(token.charAt(j)));
				}
			}
			// 参数值
			else if ('-' != firstChar) {
				// 没有任何参数的情况下，直接给参数值
				if (opts.isEmpty()) {
					break;
				} else {
					// 找出遇到这个参数值之前最近的一个参数
					OptArgPair prevOpt = opts.get(opts.size() - 1);
					if (shouldHaveArg(prevOpt.optChar)) {
						prevOpt.optArgVal = token;
					} else {
						break;
					}
				}
			}
		}

		optArgPairListIterator = opts.listIterator();
	}

	public int getNextOption() {
		if (optArgPairListIterator.hasNext()) {
			currentPair = optArgPairListIterator.next();
			if (!hasOpt(currentPair.optChar)) {
				throw new IllegalArgumentException(
						String.format("unknown option : -%s", String.valueOf(currentPair.optChar)));
			} else if (shouldHaveArg(currentPair.optChar) && currentPair.optArgVal == null) {
				throw new IllegalArgumentException(
						String.format("should have an argument : -%s", String.valueOf(currentPair.optChar)));
			}
			return currentPair.optChar;
		}
		return -1;
	}

	public String getOptionArg() {
		return currentPair.optArgVal;
	}

	private boolean hasOpt(char c) {
		return (optStr.indexOf(c) != -1);
	}

	private boolean shouldHaveArg(char c) {
		boolean match = hasOpt(c);
		if (match) {
			int idx = optStr.indexOf(c) + 1;
			return idx < optStr.length() && ':' == optStr.charAt(idx);
		}
		return false;
	}
}

class OptArgPair {
	final char optChar;
	String optArgVal;

	OptArgPair(char opt) {
		optChar = opt;
		optArgVal = null;
	}
}
