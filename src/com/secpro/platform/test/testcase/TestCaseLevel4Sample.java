package com.secpro.platform.test.testcase;


import junit.framework.TestCase;

import org.junit.Test;

import com.secpro.platform.core.services.PlatformTestLevel;

@PlatformTestLevel(testLevel="level-4")
public class TestCaseLevel4Sample extends TestCase {

	@Test
	public void test() {
		System.out.println("#TestCaseLevel4Sample TestCase in level-4");
	}

}
