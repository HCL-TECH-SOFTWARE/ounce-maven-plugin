package org.codehaus.mojo.ounce;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * Generate the scan results as part of the site.
 * 
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 * 
 */
@Mojo (name="report", defaultPhase=LifecyclePhase.SITE)
@Execute( lifecycle="scan", phase=LifecyclePhase.INSTALL )
public class ReportMojo
    extends ScanMojo
    implements MavenReport
{
    /**
     * Directory where reports will go.
     * 
     * 
     */
	@Parameter (property="ounce.reportOutputDirectory", defaultValue="${project.reporting.outputDirectory}/ounce", required=true, readonly=true)
    private File reportOutputDirectory;

    /**
     * Specify the name of an existing assessment for which to generate a report. If not specified, Ounce/Maven scans
     * the application and generates the report from that assessment.
     * 
     * 
     */
	@Parameter (property="ounce.existingAssessmentFile")
    String existingAssessmentFile;

    /**
     * The current Project.
     * 
     * 
     */
	@Parameter (property="project", readonly=true)
    protected MavenProject project;

    /**
     * For internal use only.
     * 
     * 
     * @required
     * @readonly
     */
	@Component
    private Renderer siteRenderer;

    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.description" );
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.name" );
    }

    public String getOutputName()
    {
        return "Ounce-Analysis/index";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#isExternalReport()
     */
    public boolean isExternalReport()
    {
        return true;
    }

    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "ounce-report", locale, this.getClass().getClassLoader() );
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        this.getLog().warn( "Generating..." );

        if ( existingAssessmentFile != null )
        {
            if ( options == null )
            {
                options = new HashMap();
            }
            options.put( "existingAssessmentFile", existingAssessmentFile );
        }

        super.execute();
    }

    public boolean canGenerateReport()
    {
        return true;
    }

    public void generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        this.getLog().warn( "Generating..." );
        this.waitForScan = true;
        if ( this.reportType == null )
        {
            this.reportType = "Findings";
        }
        if ( this.reportOutputType == null )
        {
            this.reportOutputType = "html";
        }
        this.reportOutputPath = reportOutputDirectory + File.separator + getOutputName() + ".html";

        try
        {
            super.execute();
        }
        catch ( MojoExecutionException e )
        {
            throw new MavenReportException( "Execption generating report:", e );
        }
        catch ( MojoFailureException e )
        {
            throw new MavenReportException( "Execption generating report:", e );
        }
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getCategoryName()
     */
    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getReportOutputDirectory()
     */
    public File getReportOutputDirectory()
    {
        return this.reportOutputDirectory;
    }

    /**
     * 
     */
    public void setReportOutputDirectory( File outputDirectory )
    {
        reportOutputDirectory = outputDirectory;
    }
}
