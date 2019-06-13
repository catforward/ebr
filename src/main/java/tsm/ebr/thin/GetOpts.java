package tsm.ebr.thin;

import java.util.ArrayList;
import java.util.ListIterator;

public class GetOpts {

	private final String optStr;
	private final ArrayList<OptArgPair> opts;
	private final ListIterator<OptArgPair> optIter;
	private OptArgPair currentPair;

	public GetOpts(String[] args, String optString) {
		optStr = optString;
		opts = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			String token = args[i];
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

		optIter = opts.listIterator();
	}

	public int getNextOption() throws IllegalArgumentException {
		if (optIter.hasNext()) {
			currentPair = optIter.next();
			if (!hasOpt(currentPair.optChar)) {
				throw new IllegalArgumentException(
						String.format("unknown option : -%s", String.valueOf(currentPair.optChar)));
			} else if (shouldHaveArg(currentPair.optChar) && currentPair.optArgVal == null) {
				throw new IllegalArgumentException(
						String.format("should have an argument : -%s", String.valueOf(currentPair.optChar)));
			}
			return (int) currentPair.optChar;
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
			if (idx < optStr.length() && ':' == optStr.charAt(idx)) {
				return true;
			}
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