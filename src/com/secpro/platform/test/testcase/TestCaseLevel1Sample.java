package com.secpro.platform.test.testcase;

import junit.framework.TestCase;

import org.junit.Test;

import com.secpro.platform.core.services.PlatformTestLevel;

@PlatformTestLevel(testLevel = "level-1")
public class TestCaseLevel1Sample extends TestCase {
	@Test
	public void test() {
		System.out.println("#TestCaseLevel1Sample TestCase in level-1");
	}
}
