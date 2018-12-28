package com.socialapi.json.linkedin;

import java.util.List;

public class LinkedinPostInsightsData {

	private List<LPostInsightsData> values;

	public List<LPostInsightsData> getvalues() {
		return values;
	}

	public void setInsightsvalues(List<LPostInsightsData> values) {
		this.values = values;
	}

	public String[] toCSVArray() {

		return new String[] { values.get(0).getData().getUpdateKey(), values.get(0).getData().getComment(),
				values.get(0).getData().getTimestamp(), values.get(0).getData().getTitle(), "post",
				values.get(0).getData().getShortenedUrl(), values.get(0).getData().getSubmittedImageUrl(),
				values.get(0).getLikeCount() + "", values.get(0).getClickCount() + "",
				values.get(0).getCommentCount() + "", values.get(0).getEngagement(),
				values.get(0).getImpressionCount() + "", values.get(0).getShareCount() + "",
				values.get(0).getData().getFulllink(), values.get(0).getData().getCidCode() };

		/*
		 * return "LinkedPostInsightsData [clickcount=" + clickcount + ", commentcount="
		 * + commentcount + ", engagement=" + engagement + ", impressionCount=" +
		 * impressionCount + ", likeCount=" + likeCount + ", shareCount=" + shareCount +
		 * ", time=" + time + "]";
		 */
	}
}
