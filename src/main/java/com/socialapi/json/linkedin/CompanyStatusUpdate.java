package com.socialapi.json.linkedin;

public class CompanyStatusUpdate {
	private Share share;

	public Share getShare() {
		return share;
	}

	public void setShare(Share share) {
		this.share = share;
	}

	@Override
	public String toString() {
		return "ClassPojo [share = " + share + "]";
	}
}
