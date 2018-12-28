package com.socialapi.json.linkedin;

public class Share {
	private String id;

	private Content content;

	private String comment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "ClassPojo [id = " + id + ", content = " + content + ", comment = " + comment + "]";
	}
}