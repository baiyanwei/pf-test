package com.secpro.platform.test.testcase;

import junit.framework.TestCase;

import org.junit.Test;

import com.secpro.platform.core.services.PlatformTestLevel;

public class TestCaseSample extends TestCase {
	@PlatformTestLevel(testLevel = "level-1")
	@Test
	public void testLevel1() {
		System.out.println("#TestCase Method Sample TestCase in level-1");
	}

	@PlatformTestLevel(testLevel = "level-2")
	@Test
	public void testLevel2() {
		System.out.println("#TestCase Method Sample TestCase in level-2");
	}

	@PlatformTestLevel(testLevel = "level-3")
	@Test
	public void testLevel3() {
		System.out.println("#TestCase Method Sample TestCase in level-3");
	}

	@PlatformTestLevel(testLevel = "level-4")
	@Test
	public void testLevel4() {
		System.out.println("#TestCase Method Sample TestCase in level-4");
	}

	@Test
	public void testOther() {
		System.out.println("#TestCase Method Sample TestCase in other");
	}

}
