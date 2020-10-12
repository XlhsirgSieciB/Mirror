package com.fm.mirror.util;

import java.lang.reflect.Field;
import java.util.Map;

public class ClassPropertyUtil {
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(Object obj, String name) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(obj);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setProperty(Object obj, String name, Object value) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setProperties(Object obj, Map<String, ? extends Object> properties) {
		for (Map.Entry<String, ? extends Object> entry : properties.entrySet()) {
			setProperty(obj, entry.getKey(), entry.getValue());
		}
	}
}
