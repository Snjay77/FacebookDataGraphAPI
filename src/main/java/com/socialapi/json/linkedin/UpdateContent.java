package com.socialapi.json.linkedin;

public class UpdateContent {
	private CompanyStatusUpdate companyStatusUpdate;

	public CompanyStatusUpdate getCompanyStatusUpdate() {
		return companyStatusUpdate;
	}

	public void setCompanyStatusUpdate(CompanyStatusUpdate companyStatusUpdate) {
		this.companyStatusUpdate = companyStatusUpdate;
	}

	@Override
	public String toString() {
		return "ClassPojo [companyStatusUpdate = " + companyStatusUpdate + "]";
	}
}