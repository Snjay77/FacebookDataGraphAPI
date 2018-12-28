package com.socialapi.json.linkedin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public class LPostData {
	private String timestamp;

	private UpdateContent updateContent;

	private String numLikes;

	private String updateKey;

	private String fulllink;

	private String cidCode;

	public String getCidCode() {
		return cidCode;
	}

	public void setCidCode(String cidCode) {
		this.cidCode = cidCode;
	}

	public String getFulllink() {
		return fulllink;
	}

	public void setFulllink(String fulllink) {
		this.fulllink = fulllink;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public UpdateContent getUpdateContent() {
		return updateContent;
	}

	public void setUpdateContent(UpdateContent updateContent) {
		this.updateContent = updateContent;
	}

	public String getNumLikes() {
		return numLikes;
	}

	public void setNumLikes(String numLikes) {
		this.numLikes = numLikes;
	}

	public String getUpdateKey() {
		return updateKey;
	}

	public void setUpdateKey(String updateKey) {
		this.updateKey = updateKey;
	}

	public String getComment() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getComment();
	}

	public String getDescription() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare().getContent() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getContent().getDescription();
	}

	public String getShortenedUrl() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare().getContent() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getContent().getShortenedUrl();
	}

	public String getSubmittedUrl() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare().getContent() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getContent().getSubmittedUrl();
	}

	public String getSubmittedImageUrl() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare().getContent() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getContent().getSubmittedImageUrl();
	}

	public String getTitle() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare().getContent() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getContent().getTitle();
	}

	public String getThumbnailUrl() {
		if (updateContent == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare() == null) {
			return "";
		}

		if (updateContent.getCompanyStatusUpdate().getShare().getContent() == null) {
			return "";
		}

		return updateContent.getCompanyStatusUpdate().getShare().getContent().getThumbnailUrl();
	}

	public boolean isPictureColumn(int column) {
		return column == 12;
	}

	public String[] toCSVArray() {

		return new String[] { updateKey, getComment(), timestamp, getTitle(), "post", getDescription(),
				getShortenedUrl(), getSubmittedUrl(), getSubmittedImageUrl(), numLikes, getFulllink(), getCidCode(),
				"image" };

		// 2003-34-34 12:34:34
		// LocalDateTime ts3 =
		// LocalDateTime.ofInstant(Instant.ofEpochMilli(34234234324l),
		// TimeZone.getDefault().toZoneId());

	}

}
