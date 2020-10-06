package com.fm.mirror.core;

import java.util.LinkedList;
import java.util.List;

public class Instance {
	public String name; // 接口的简称
	public String interfaceName; // 接口的全称
	public String className; // 接口的实现类
	public String method; // 接口实现类的获取策略
	public List<ConstructArg> constructArgs = new LinkedList<>(); // 接口的实现类的构造参数
	public List<Property> properties = new LinkedList<>(); // 接口的实现类的属性
}
