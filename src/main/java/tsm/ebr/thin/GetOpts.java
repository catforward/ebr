/*
  MIT License
  <p>
  Copyright (c) 2019 catforward
  <p>
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  <p>
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
  <p>
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 */
package tsm.ebr.thin;

import tsm.ebr.base.Const;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * <pre>
 * 命令行参数解析
 * </pre>
 * @author catforward
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

	public int getNextOption() throws IllegalArgumentException {
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
			return idx < optStr.length() && Const.COLON == optStr.charAt(idx);
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