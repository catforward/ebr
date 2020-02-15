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
package pers.ebr.cli;

import org.junit.Assert;
import org.junit.Test;
import pers.ebr.cli.util.GetOpts;

/**
 * 命令行参数测试
 * @author l.gong
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