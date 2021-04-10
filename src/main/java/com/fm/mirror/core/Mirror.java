package com.fm.mirror.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public final class Mirror {

	private final static Map<String, Object> container = new HashMap<>();// 容器(放置需要的Object)
	private final static Map<String, Instance> config = ConfigReader.parseConfig();
	static {
		for (Map.Entry<String, Instance> entry : config.entrySet()) {
			Instance instance = entry.getValue();
			Object existObj = container.get(entry.getKey());
			if (existObj == null && instance.method.equals("single")) { // createInstance方法会在container中放入实例对象，所以要检查
				container.put(entry.getKey(), createInstance(instance));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(String name, Class<T> clazz) {
		if (clazz != null) {
			return getInstance(name);
		}
		throw new NullPointerException();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(String name) {
		T object = (T) container.get(name);
		if (object == null) {
			object = (T) createInstance(config.get(name));
		}
		return object;
	}

	private static Object createInstance(Instance instance) {
		try {
			Class<?> clazz = Class.forName(instance.className);
			// 构造对象 >
			Class<?>[] paramTypes = new Class[instance.constructArgs.size()];
			Object[] paramsValues = new Object[instance.constructArgs.size()];
			int index = 0;
			for (ConstructArg constructArg : instance.constructArgs) {
				paramTypes[index] = transformToClass(constructArg.type);
				Object value;
				if (constructArg.value != null) { // 值类型
					value = transformToPrimitive(paramTypes[index], constructArg.value);
				} else if (constructArg.ref != null) { // 对象类型
					Object existObj = container.get(constructArg.ref);
					if (existObj == null) { // 容器中目前没有此实例
						existObj = createInstance(config.get(constructArg.ref));
						if (config.get(constructArg.ref).method.equals("single")) {
							container.put(constructArg.ref, existObj);
						}
					}
					value = existObj;
				} else {
					throw new InstantiationException("IOC配置文档错误:" + instance.name + "的" + constructArg.name + "未有效配置");
				}
				paramsValues[index] = value;
				++index;
			}
			Constructor<?> constructorMethod = clazz.getConstructor(paramTypes);
			Object obj = constructorMethod.newInstance(paramsValues);
			// 构造对象 <

			// 注入属性 >
			for (Property property : instance.properties) {
				Field field = obj.getClass().getDeclaredField(property.name);
				Object value;
				if (property.value != null) { // 值类型
					value = transformToPrimitive(field, property.value);
				} else if (property.ref != null) { // 对象类型
					Object existObj = container.get(property.ref);
					if (existObj == null) { // 容器中目前没有此实例
						existObj = createInstance(config.get(property.ref));
						if (config.get(property.ref).method.equals("single")) {
							container.put(property.ref, existObj);
						}
					}
					value = existObj;
				} else {
					throw new InstantiationException("IOC配置文档错误:" + instance.name + "的" + property.name + "未有效配置");
				}
				field.setAccessible(true);
				field.set(obj, value);
			}
			// 注入属性 <
			return obj;
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
				NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?> transformToClass(String type) throws ClassNotFoundException {
		switch (type) {
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "char":
				return char.class;
			case "boolean":
				return boolean.class;
			case "byte":
				return byte.class;
			case "short":
				return short.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			default:
				return Class.forName(type);
		}
	}

	private static Object transformToPrimitive(Field field, String value) throws IllegalAccessException, InstantiationException {
		return transformToPrimitive(field.getType(), value);
	}

	private static Object transformToPrimitive(Class<?> clazz, String value) throws IllegalAccessException, InstantiationException {
		if (clazz == String.class) {
			return value;
		} else if (clazz == int.class || clazz == Integer.class) { // Integer.TYPE == int.class
			return Integer.parseInt(value);
		} else if (clazz == long.class || clazz == Long.class) {
			return Long.parseLong(value);
		} else if (clazz == char.class || clazz == Character.class) {
			if (value.length() > 1) {
				throw new InstantiationException("\"" + value + "\"中不止一个char");
			}
			return value.charAt(0);
		} else if (clazz == boolean.class || clazz == Boolean.class) {
			return Boolean.parseBoolean(value);
		} else if (clazz == byte.class || clazz == Byte.class) {
			return Byte.parseByte(value);
		} else if (clazz == short.class || clazz == Short.class) {
			return Short.parseShort(value);
		} else if (clazz == float.class || clazz == Float.class) {
			return Float.parseFloat(value);
		} else if (clazz == double.class || clazz == Double.class) {
			return Double.parseDouble(value);
		} else if (clazz == BigDecimal.class) {
			return new BigDecimal(value);
		} else if (clazz == BigInteger.class) {
			return new BigInteger(value);
		} else {
			throw new InstantiationException("无法实例化此类型: " + clazz.toString());
		}
	}
}
