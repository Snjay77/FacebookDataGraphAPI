package com.socialapi.json.facebook;

import java.util.List;

public class FacebookImpressionDailyDifference {

	private List<ImpressionMetricDiff> metricsDiff;
	private FPostData data;

	public List<ImpressionMetricDiff> getMetricsDiff() {
		return metricsDiff;
	}

	public void setMetricsDiff(List<ImpressionMetricDiff> metricsDiff) {
		this.metricsDiff = metricsDiff;
	}

	public FPostData getData() {
		return data;
	}

	public void setData(FPostData data) {
		this.data = data;
	}

	
}
