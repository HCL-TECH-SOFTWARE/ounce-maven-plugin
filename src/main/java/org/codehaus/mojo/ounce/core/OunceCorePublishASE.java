package org.codehaus.mojo.ounce.core;

public class OunceCorePublishASE {
	
	private final String assessmentFile;
	private final String caller;
	private final String folderID;
	private final String installDir;
	private final String aseApplication;
	boolean wait;
	
	public OunceCorePublishASE(String aseApplication, String assessmentFile, String caller,
			String folderID, String installDir, boolean wait) {
		super();
		this.aseApplication = aseApplication;
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

	public String getAseApplication() {
		return aseApplication;
	}

}
