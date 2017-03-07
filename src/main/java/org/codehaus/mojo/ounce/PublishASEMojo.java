package org.codehaus.mojo.ounce;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.ounce.core.OunceCore;
import org.codehaus.mojo.ounce.core.OunceCoreException;
import org.codehaus.plexus.util.StringUtils;


/**
 * This mojo provides the ability to publish an AppScan Source assessment to AppScan
 * Enterprise
 * 
 * @author <a href="mailto:sherardh@us.ibm.com">Sherard Howe</a>
 * @goal publishASE
 * @aggregator
 * @execute lifecycle="scan" phase="package"
 */

public class PublishASEMojo extends AbstractOunceMojo {
	
	/**
	 * This is the assessment file that will be published to AppScan Enterprise
	 * 
	 * @parameter expression="${ounce.assessmentOutput}" default-value="${basedir}/${project.artifactId}.ozasmt"
	 */
	
	String assessmentOutput;
	
	/**
	 * Optional. Assign a caller to the report generation operation. The caller can be
	 * the name of an actual user, but this is not required. The caller name is written
	 * to the ounceauto log file
	 * 
	 * @parameter expression="${ounce.caller}" 
	 */
	
	String caller;
	
	/**
	 * Optional. Enterprise Console folder to publish to. If this argument is not used, the
	 * assessment will be published to your default Enterprise Console folder.
	 * 
	 * @parameter expression="${ounce.folderID}"
	 */
	
	String folderID;
	
	/**
	 * The location of the Ounce client installation directory if the Ounce client is not on the path
	 * 
	 * @parameter expression="${ounce.installDir}"
	 */
	
	String installDir;
	
	/**
     * Forces the goal to wait until the scan finishes, thus blocking the Maven build. This is useful if the scan is
     * being performed from the report mojo as part of integration with the site target and the site is getting
     * deployed.
     * 
     * @parameter expression="${ounce.wait}" default-value="false"
     */
	
	boolean waitForScan;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		if(StringUtils.isEmpty(assessmentOutput))
		{
			throw new MojoExecutionException("\'assessmentFile\' must be defined.");
		}
		
		try
		{
			OunceCore core = getCore();
			core.publishASE(assessmentOutput, caller, folderID, installDir, waitForScan, getLog());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
