package com.secpro.platform.test.testcase;


import junit.framework.TestCase;

import org.junit.Test;

import com.secpro.platform.core.services.PlatformTestLevel;

@PlatformTestLevel(testLevel="level-2")
public class TestCaseLevel2Sample extends TestCase {

	@Test
	public void test() {
		System.out.println("#TestCaseLevel2Sample TestCase in level-2");
	}

}
