package com.socialapi.json.facebook;

public class ImpressionMetricDiff {

	String name; // or impression metrics type

	long diff;

	public ImpressionMetricDiff() {}
	public ImpressionMetricDiff(String name, long diff) {
		super();
		this.name = name;
		this.diff = diff;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDiff() {
		return diff;
	}

	public void setDiff(long diff) {
		this.diff = diff;
	}

}