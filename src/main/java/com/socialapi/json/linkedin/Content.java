package com.socialapi.json.linkedin;

public class Content {
	private String title;

	private String shortenedUrl;

	private String submittedUrl;

	private String description;

	private String thumbnailUrl;

	private String submittedImageUrl;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortenedUrl() {
		return shortenedUrl;
	}

	public void setShortenedUrl(String shortenedUrl) {
		this.shortenedUrl = shortenedUrl;
	}

	public String getSubmittedUrl() {
		return submittedUrl;
	}

	public void setSubmittedUrl(String submittedUrl) {
		this.submittedUrl = submittedUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getSubmittedImageUrl() {
		return submittedImageUrl;
	}

	public void setSubmittedImageUrl(String submittedImageUrl) {
		this.submittedImageUrl = submittedImageUrl;
	}

	@Override
	public String toString() {
		return "ClassPojo [title = " + title + ", shortenedUrl = " + shortenedUrl + ", submittedUrl = " + submittedUrl
				+ ", description = " + description + ", thumbnailUrl = " + thumbnailUrl + ", submittedImageUrl = "
				+ submittedImageUrl + "]";
	}
}