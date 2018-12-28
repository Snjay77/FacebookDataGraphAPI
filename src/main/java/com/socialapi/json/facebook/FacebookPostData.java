package com.socialapi.json.facebook;

import java.util.List;

public class FacebookPostData {
	String[] header = new String[] { "ID", "post", "created date", "updated date", "head line", "type", "link",
			"perm link", " full picture url", "share count", "like count", "full link", "cid code", " image" };

	public boolean isPictureUrlIndex(int colNum) {
		return colNum == 8;
	}

	private List<FPostData> data;

	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}

	public List<FPostData> getData() {
		return data;
	}

	public void setData(List<FPostData> data) {
		this.data = data;
	}

}
