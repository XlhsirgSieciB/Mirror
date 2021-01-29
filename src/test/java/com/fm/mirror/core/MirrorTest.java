package com.fm.mirror.core;

import org.junit.Test;

import java.util.Map;

public class MirrorTest {
	@Test
	public void getInstanceTest() {
		Map<String, Object> map1 = Mirror.getInstance("Map");
		assert map1 != null;
		Map<String, Object> map2 = Mirror.getInstance("Map");
		assert map2 != null;
		assert map1 != map2;
	}
}
