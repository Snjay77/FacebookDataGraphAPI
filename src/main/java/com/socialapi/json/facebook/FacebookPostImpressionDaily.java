package com.socialapi.json.facebook;

public class FacebookPostImpressionDaily {

	/*
	 * we will use this class to compare and generate the final csv daily comparison
	 */
	private FPostData data;

	private FacebokPostInsight insight;

	public FPostData getData() {
		return data;
	}

	public void setData(FPostData data) {
		this.data = data;
	}

	public FacebokPostInsight getInsight() {
		return insight;
	}

	public void setInsight(FacebokPostInsight insight) {
		this.insight = insight;
	}

}
