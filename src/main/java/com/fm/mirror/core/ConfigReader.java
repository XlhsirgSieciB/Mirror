package com.fm.mirror.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {
	private static final String DEFAULTFILENAME = "/Mirror.xml";

	/**
	 * 读取默认配置文件，生成实例简称与实例实现类的映射关系
	 * @return 实例简称与实例实现类的映射关系
	 */
	public static Map<String, Instance> parseConfig() {
		return _parseConfig(DEFAULTFILENAME);
	}

	private static Map<String, Instance> _parseConfig(String filename) {
		Map<String, Instance> config = new HashMap<>();
		// 使用dom4j解析XML配置文件 >
		// 读取文件到内存 >
		SAXReader reader = new SAXReader();
		InputStream conf = ConfigReader.class.getResourceAsStream(filename);
		Document xml;
		try {
			xml = reader.read(conf);
		} catch (DocumentException e) {
			throw new RuntimeException("no such file:" + filename, e);
		}
		// 读取文件到内存 <
		// 解析文件 >
		String xpath = "//instance";
		List nodes = xml.selectNodes(xpath);
		for (Object o : nodes) {
			Element node = (Element) o;
			Instance instance = new Instance();
			instance.name = node.attributeValue("name");
			instance.interfaceName= node.attributeValue("interface");
			instance.className = node.attributeValue("class");
			instance.method = node.attributeValue("method");
			if (instance.method == null) {
				instance.method = "single";
			}
			for (Object obj : node.elements("construct-arg")) {
				Element arg = (Element) obj;
				ConstructArg construct = new ConstructArg();
				construct.name = arg.attributeValue("name");
				construct.type = arg.attributeValue("type");
				construct.value = arg.attributeValue("value");
				construct.ref = arg.attributeValue("ref");
				instance.constructArgs.add(construct);
			}
			for (Object obj : node.elements("property")) {
				Element child = (Element) obj;
				Property property = new Property();
				property.name = child.attributeValue("name");
				property.value = child.attributeValue("value");
				property.ref = child.attributeValue("ref");
				instance.properties.add(property);
			}
			config.put(instance.name, instance);
		}
		// 解析文件 <
		// 使用dom4j解析XML配置文件 <
		return config;
	}
}
