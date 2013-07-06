package com.secpro.platform.test.services;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class CfgBean {
	@XmlElement(name = "name")
	private String name = "aa";
	@XmlElement(name = "stringData", type = List.class)
	private List<String> stringDataList = new ArrayList<String>();

	public String toString(){
		return CfgBean.class+"#"+this.hashCode();
	}
}
