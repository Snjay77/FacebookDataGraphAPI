package com.socialapi.json.facebook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebokPostInsight {
	List<FacebookPostImpression> data;

	String pageId;

	public List<FacebookPostImpression> getData() {
		return data;
	}

	public void setData(List<FacebookPostImpression> data) {
		this.data = data;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

}
