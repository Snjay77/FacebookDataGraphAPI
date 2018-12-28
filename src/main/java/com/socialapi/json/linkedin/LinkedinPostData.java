package com.socialapi.json.linkedin;

import java.util.ArrayList;
import java.util.List;

public class LinkedinPostData {
	String[] header = new String[] { "ID", "Post-Message", "Created date", "Head line", "type", "Description", "link",
			"Submitted URL", " full picture url", "like count", "Full link", "CID code", " image" };

	int _count;
	int _start;
	int _total;
	
	//80 - pagination
	//count=100, start = 0, total=80 -- first page
	
	//count=100, start = 100, total=480 -- s page
	//count=100, start = 200, total=480 -- t page
	//count=100, start = 300, total=480 -- 4 page
	//count=100, start = 400, total=480 -- 5 page
	//count=100, start = 500, total=480 -- 6 page

	public boolean isPictureUrlIndex(int colNum) {
		return colNum == 8;
	}

	private List<LPostData> values;

	public String[] getHeader() {
		return header;
	}

	public void setHeader(String[] header) {
		this.header = header;
	}

	public int get_count() {
		return _count;
	}

	public void set_count(int _count) {
		this._count = _count;
	}

	public int get_start() {
		return _start;
	}

	public void set_start(int _start) {
		this._start = _start;
	}

	public int get_total() {
		return _total;
	}

	public void set_total(int _total) {
		this._total = _total;
	}

	public List<LPostData> getValues() {
		if(values==null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<LPostData> values) {
		this.values = values;
	}

}
