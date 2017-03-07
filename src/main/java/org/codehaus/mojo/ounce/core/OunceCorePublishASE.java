package org.codehaus.mojo.ounce.core;

public class OunceCorePublishASE {
	
	String assessmentFile;
	String caller;
	String folderID;
	String installDir;
	boolean wait;
	
	public OunceCorePublishASE(String assessmentFile, String caller,
			String folderID, String installDir, boolean wait) {
		super();
		this.assessmentFile = assessmentFile;
		this.caller = caller;
		this.folderID = folderID;
		this.installDir = installDir;
		this.wait = wait;
	}
	
	public String getAssessmentFile() {
		return assessmentFile;
	}
	public String getCaller() {
		return caller;
	}
	public String getFolderID() {
		return folderID;
	}
	public String getInstallDir() {
		return installDir;
	}

	public boolean isWait() {
		return wait;
	}
}
