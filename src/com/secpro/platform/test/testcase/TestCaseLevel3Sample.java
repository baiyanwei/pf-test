package com.secpro.platform.test.testcase;


import junit.framework.TestCase;

import org.junit.Test;

import com.secpro.platform.core.services.PlatformTestLevel;

@PlatformTestLevel(testLevel="level-3")
public class TestCaseLevel3Sample extends TestCase {

	@Test
	public void test() {
		System.out.println("#TestCaseLevel3Sample TestCase in level-3");
	}

}
