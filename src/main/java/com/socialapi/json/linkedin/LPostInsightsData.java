package com.socialapi.json.linkedin;

public class LPostInsightsData {

	/*
	 * "_total": 1, "values": [{ "clickCount": 40, "commentCount": 0, "engagement":
	 * 0.010792060127192138, "impressionCount": 5189, "likeCount": 14, "shareCount":
	 * 2, "time": 1533081600000 }] }
	 */
	private long clickCount;

	private long commentCount;

	private String engagement;

	private long impressionCount;

	private long likeCount;

	private long shareCount;

	private String time;

	private LPostData data = new LPostData();

	public LPostData getData() {
		return data;
	}

	public void setData(LPostData data) {
		this.data = data;
	}

	public long getClickCount() {
		return clickCount;
	}

	public void setClickCount(long clickCount) {
		this.clickCount = clickCount;
	}

	public long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}

	public String getEngagement() {
		return engagement;
	}

	public void setEngagement(String engagement) {
		this.engagement = engagement;
	}

	public long getImpressionCount() {
		return impressionCount;
	}

	public void setImpressionCount(long impressionCount) {
		this.impressionCount = impressionCount;
	}

	public long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(long likeCount) {
		this.likeCount = likeCount;
	}

	public long getShareCount() {
		return shareCount;
	}

	public void setShareCount(long shareCount) {
		this.shareCount = shareCount;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
