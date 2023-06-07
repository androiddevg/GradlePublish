package com.gp.jitpack.example.response;

import com.google.gson.annotations.SerializedName;

public class UpdateData{

	@SerializedName("reminders")
	private String reminders = "";

	@SerializedName("releaseDate")
	private String releaseDate = "";

	@SerializedName("updateBy")
	private String updateBy = "";

	@SerializedName("platformMinTarget")
	private String platformMinTarget = "";

	@SerializedName("updateURL")
	private String updateURL = "";

	@SerializedName("type")
	private String type = "";

	@SerializedName("previousMandatoryVersionCode")
	private String previousMandatoryVersionCode = "";

	@SerializedName("platform")
	private String platform = "";

	@SerializedName("versionCode")
	private String versionCode = "";

	public String getReminders(){
		return reminders;
	}

	public String getReleaseDate(){
		return releaseDate;
	}

	public String getUpdateBy(){
		return updateBy;
	}
	public void setUpdateBy(String updateBy){
		this.updateBy = updateBy;
	}
	public String getPlatformMinTarget(){
		return platformMinTarget;
	}

	public String getUpdateURL(){
		return updateURL;
	}

	public String getType(){
		return type;
	}

	public void setType(String updateType){
		this.type = updateType;
	}
	public String getPreviousMandatoryVersionCode(){
		return previousMandatoryVersionCode;
	}

	public void setPreviousMandatoryVersionCode(String previousMandatoryVersionCode){
		this.previousMandatoryVersionCode = previousMandatoryVersionCode;
	}

	public String getPlatform(){
		return platform;
	}

	public String getVersionCode(){
		return versionCode;
	}

	public void setVersionCode(String versionCode){
		this.versionCode = versionCode;
	}
}