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
package ebr.cli;

import org.junit.Assert;
import org.junit.Test;
import ebr.cli.GetOpts;

/**
 * 命令行参数测试
 * @author catforward
 */
public class GetOptsTest {

	@Test
	public void testGetOpts_1() {
		StringBuilder actual = new StringBuilder();
		String expected = "ttest.sod24hhvl";
		String[] args = {"-t", "test.so", "-d", "24h", "-hvl"};
		GetOpts opt = new GetOpts(args, "d:t:hvl");
		int c = -1;
		while ((c = opt.getNextOption()) != -1) {
			char cc = (char)c;
			switch (cc) {
			case 'd':
			case 't':{
				actual.append(String.valueOf(cc)).append(opt.getOptionArg());
				break;
			}
			case 'h':
			case 'v':
			case 'l':{
				actual.append(String.valueOf(cc));
				break;
			}
			default: break;
			}
		}
		System.out.println("Result1:"+actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}
	
	@Test
	public void testGetOpts_2() {
		StringBuilder actual = new StringBuilder();
		String expected = "";
		String[] args = {"-", "test.so", "-d", "24h", "-h"};
		GetOpts opt = new GetOpts(args, "d:t:h");
		int c = -1;
		while ((c = opt.getNextOption()) != -1) {
			char cc = (char)c;
			switch (cc) {
			case 'd': 
			case 't':{
				actual.append(String.valueOf(cc)).append(opt.getOptionArg());
				break;
			}
			case 'h':{
				actual.append(String.valueOf(cc));
				break;
			}
			default: break;
			}
		}
		System.out.println("Result2:"+actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}
	
	@Test
	public void testGetOpts_3() {
		StringBuilder actual = new StringBuilder();
		String expected = "";
		String[] args = {"--t", "test.so", "-d", "24h", "-h"};
		GetOpts opt = new GetOpts(args, "d:t:h");
		int c = -1;
		while ((c = opt.getNextOption()) != -1) {
			char cc = (char)c;
			switch (cc) {
			case 'd': 
			case 't':{
				actual.append(String.valueOf(cc)).append(opt.getOptionArg());
				break;
			}
			case 'h':{
				actual.append(String.valueOf(cc));
				break;
			}
			default: break;
			}
		}
		System.out.println("Result3:"+actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}
	
	@Test
	public void testGetOpts_4() {
		StringBuilder actual = new StringBuilder();
		String expected = "ttest.soshould have an argument : -d";
		String[] args = { "-t", "test.so", "-d", "-h" };
		GetOpts opt = new GetOpts(args, "d:t:h");
		int c = -1;
		try {
			while ((c = opt.getNextOption()) != -1) {
				char cc = (char)c;
				switch (cc) {
				case 'd':
				case 't': {
					actual.append(String.valueOf(cc)).append(opt.getOptionArg());
					break;
				}
				case 'h': {
					actual.append(String.valueOf(cc));
					break;
				}
				default:
					break;
				}
			}
		} catch (IllegalArgumentException ex) {
			actual.append(ex.getMessage());
		}
		System.out.println("Result4:" + actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}

	@Test
	public void testGetOpts_5() {
		StringBuilder actual = new StringBuilder();
		String expected = "ttest.sounknown option : -v";
		String[] args = { "-t", "test.so", "-v", "-h" };
		GetOpts opt = new GetOpts(args, "d:t:h");
		int c = -1;
		try {
			while ((c = opt.getNextOption()) != -1) {
				char cc = (char)c;
				switch (cc) {
				case 'd':
				case 't': {
					actual.append(String.valueOf(cc)).append(opt.getOptionArg());
					break;
				}
				case 'h': {
					actual.append(String.valueOf(cc));
					break;
				}
				default:
					break;
				}
			}
		} catch (IllegalArgumentException ex) {
			actual.append(ex.getMessage());
		}
		System.out.println("Result5:" + actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}
	
	@Test
	public void testGetOpts_6() {
		StringBuilder actual = new StringBuilder();
		String expected = "ttest.soh";
		String[] args = { "-t", "test.so", "-h", "22" };
		GetOpts opt = new GetOpts(args, "d:t:h");
		int c = -1;
		try {
			while ((c = opt.getNextOption()) != -1) {
				char cc = (char)c;
				switch (cc) {
				case 'd':
				case 't': {
					actual.append(String.valueOf(cc)).append(opt.getOptionArg());
					break;
				}
				case 'h': {
					actual.append(String.valueOf(cc));
					break;
				}
				default:
					break;
				}
			}
		} catch (IllegalArgumentException ex) {
			actual.append(ex.getMessage());
		}
		System.out.println("Result6:" + actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}
	
	@Test
	public void testGetOpts_7() {
		StringBuilder actual = new StringBuilder();
		String expected = "";
		String[] args = {"t", "test.so", "-d", "24h", "-hvl"};
		GetOpts opt = new GetOpts(args, "d:t:hvl");
		int c = -1;
		while ((c = opt.getNextOption()) != -1) {
			char cc = (char)c;
			switch (cc) {
			case 'd':
			case 't':{
				actual.append(String.valueOf(cc)).append(opt.getOptionArg());
				break;
			}
			case 'h':
			case 'v':
			case 'l':{
				actual.append(String.valueOf(cc));
				break;
			}
			default: break;
			}
		}
		System.out.println("Result7:"+actual.toString());
		Assert.assertEquals(expected, actual.toString());
	}
}
