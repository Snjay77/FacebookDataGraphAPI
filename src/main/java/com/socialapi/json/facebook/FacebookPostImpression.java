package com.socialapi.json.facebook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class FacebookPostImpression {
	String name;
	List<Value> values;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

	public long count() {
		return values.get(0).value;
	}
}

class Value {
	long value;

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	
	 
}
