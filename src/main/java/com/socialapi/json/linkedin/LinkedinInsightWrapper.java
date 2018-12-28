package com.socialapi.json.linkedin;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LinkedinInsightWrapper {
	String[] header = new String[] { "ID", "Post-Message", "Created date", "Head line", "type", "Shortened URL",
			" full picture url", "like count", "clickcount", "commentcount", "engagement", "impressioncount",
			"sharecount", "Full link", "CID code" };
	List<LinkedinPostInsightsData> insights = new ArrayList<>();

	@JsonIgnore
	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}

	public void add(LinkedinPostInsightsData data) {
		insights.add(data);
	}

	public List<LinkedinPostInsightsData> getInsights() {
		return insights;
	}

	public void setInsights(List<LinkedinPostInsightsData> insights) {
		this.insights = insights;
	}

}
