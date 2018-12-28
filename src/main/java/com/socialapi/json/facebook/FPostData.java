package com.socialapi.json.facebook;

import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;

public class FPostData {
	private String id;
	private String message;
	private String created_time;
	private String updated_time;
	private String name;
	private String type;
	private String link;
	private String permalink_url;
	private String full_picture;
	private Shares shares;
	private Likes likes;
	// TODO: add comments
	private String cidCode;
	private String fullLink;

	public String getFullLink() {
		return fullLink;
	}

	public void setFullLink(String fullLink) {
		this.fullLink = fullLink;
	}

	public void setCidCode(String cidCode) {
		this.cidCode = cidCode;
	}

	public String getCidCode() {
		return cidCode;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}

	public String getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPermalink_url() {
		return permalink_url;
	}

	public void setPermalink_url(String permalink_url) {
		this.permalink_url = permalink_url;
	}

	public String getFull_picture() {
		return full_picture;
	}

	public void setFull_picture(String full_picture) {
		this.full_picture = full_picture;
	}

	public Shares getShares() {
		return shares;
	}

	public void setShares(Shares shares) {
		this.shares = shares;
	}

	public Likes getLikes() {
		return likes;
	}

	public void setLikes(Likes likes) {
		this.likes = likes;
	}

	public String getShareCount() {
		return shares == null ? "" : shares.getCount();
	}

	public String getLikeCount() {
		if (likes == null) {
			return "";
		}

		if (likes.getSummary() == null) {
			return "";
		}
		return likes.getSummary().getTotal_count();
	}

	public boolean isPictureColumn(int column) {
		return column == 13;
	}

	public String[] toCSVArray() {
		return new String[] { id, message, created_time, updated_time, name, type, link, permalink_url, full_picture,
				getShareCount(), getLikeCount(), getFullLink(), getCidCode(), "image" };
	}

	public long diffShareCount(FPostData another) {
		return NumberUtils.toLong(this.getShareCount(), 0l) - NumberUtils.toLong(another.getShareCount(), 0l);

	}
	public long diffLikeCount(FPostData another) {
		return NumberUtils.toLong(this.getLikeCount(), 0l) - NumberUtils.toLong(another.getLikeCount(), 0l);

	}

}

class Shares {
	private String count;

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
}

class Likes {
	private Summary summary;

	public Summary getSummary() {
		return summary;
	}

	public void setSummary(Summary summary) {
		this.summary = summary;
	}
}

class Summary {
	private String total_count;

	public String getTotal_count() {
		return total_count;
	}

	public void setTotal_count(String total_count) {
		this.total_count = total_count;
	}
}
