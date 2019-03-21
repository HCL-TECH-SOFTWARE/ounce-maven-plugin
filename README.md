# IBM AppScan Source Maven Plug-in

Easily integrate security testing into your Maven builds using the ounce-maven-plugin and AppScan Source.

# Prerequisites

- Java 1.7 or higher.
- Maven 3.0 or higher

# Usage

The ounce-maven-plugin supports 7 goals:

1. <b>ounce:application</b>
2. <b>ounce:project</b>
3. <b>ounce:help</b>
4. <b>ounce:report</b>
5. <b>ounce:scan</b>
6. <b>ounce:publishASE</b>
7. <b>ounce:project-only</b>

ounce:application<br>
  This mojo generates an Ounce application file. It will automatically include
  all child modules as projects. This list make be modified using the includes
  and excludes patterns. Projects that are external to this build may be
  included directly using the externalProjects list. External Applications may
  also be included. All of their modules will be inherted as part of this
  application file. Those projects may also be filtered upon import.

  Available parameters:

    appDir (Default: ${basedir})
      Specifies the directory where to create the paf file
      Expression: ${ounce.appDir}

    appName (Default: ${project.artifactId})
      Specifies the directory where to create the paf file
      Expression: ${ounce.appName}

    coreHint (Default: ouncexml)
      This hint provides a way to switch the core implementation. Consult Ounce
      support for details, most users should leave this set to the default. Use
      -Dounce.core=console to have have the output displayed instead of written
      to the file for debugging purposes.
      Expression: ${ounce.core}

    excludes
      An array of directories containing the pom file of any projects to
      exclude. Excludes can contain standard Ant-style wildcards.
      Excludes only apply to inherited modules, not external projects. The
      current project is not filtered.

    externalApplications
      Allows you to include projects from multiple applications. The external
      application properties are not inherited, and the external application
      must already exist.
      externalApplications is a list of directories containing top-level pom
      files.
      The format for externalApplications is:
      pathname,[includes|includes],[excludes|excludes]
      Where:
      
      - pathname, includes, and excludes are comma delimited; if you have
        excludes, but no includes, use two commas.
      - Multiple includes or excludes are separated by pipes (\x7c).
      - Excludes can contain standard Ant style wildcards.

    externalProjects
      List of external projects to include. These projects are included after
      any other projects have been included or excluded.
      The format is: name,path
      Where:
      
      - name is the artifact ID of the project to include.
      - path is the pathname to the project.

    includes
      An array of directories containing the pom file of any projects to
      include. If an include pattern is specified, projects not specifed by
      include patterns are excluded.
      Include only applies to inherited modules, not external projects. The
      current project is not filtered.
      The include pattern may contain the following wildcard characters:
      *: Zero or more characters
      **: Any folders
      ?: One and only one character

    pathVariableMap
      Map of Ounce variable names and paths.
      pathVariableMap variables are automatically registered with Ounce by the
      Ounce/Maven plugin if the Ounce Automation Server is installed.

    projectDir (Default: ${basedir})
      Specifies the directory where to create the ppf file
      Expression: ${ounce.projectDir}

    skipPoms (Default: true)
      If pom packaging projects should be skipped. Typically these will not have
      source code and should be excluded. This is true by default because
      typically the application or projects will be created at a pom level and
      the poms have no source to be analyzed Only set this if you have source in
      your 'pom' packaging projects that needs to be scanned.
      Expression: ${ounce.skipPoms}

ounce:help<br>
  Display help information on ounce-maven-plugin.
  Call
    mvn ounce:help -Ddetail=true -Dgoal=<goal-name>
  to display parameter details.

  Available parameters:

    detail (Default: false)
      If true, display all settable properties for each goal.
      Expression: ${detail}

    goal
      The name of the goal for which to show help. If unspecified, all goals
      will be displayed.
      Expression: ${goal}

    indentSize (Default: 2)
      The number of spaces per indentation level, should be positive.
      Expression: ${indentSize}

    lineLength (Default: 80)
      The maximum length of a display line, should be positive.
      Expression: ${lineLength}

ounce:project<br>
  This mojo generates an Ounce project file. It forks the build and executes the
  process-sources phase so that any plugins that may generate sources and attach
  new source folders to the project will execute and those source folders will
  be automatically included in the generated project. This mojo is intended to
  be executed from the command line. If you would rather have the project built
  automatically during your build, use the project-only goal instead.

  Available parameters:

    analyzeStrutsFramework (Default: false)
      Whether to analyze the framework for a Struts application
      Expression: ${ounce.analyzeStrutsFramework}

    appDir (Default: ${basedir})
      Specifies the directory where to create the paf file
      Expression: ${ounce.appDir}

    classpathScope (Default: compile)
      The scope of the classpath used to analyze this project.
      Valid choices are: compile, test, runtime, or system. If
      includeTestSources is true, then the classpathScope reverts to test.
      Otherwise, the default is compile.
      Expression: ${ounce.classpathScope}

    coreHint (Default: ouncexml)
      This hint provides a way to switch the core implementation. Consult Ounce
      support for details, most users should leave this set to the default. Use
      -Dounce.core=console to have have the output displayed instead of written
      to the file for debugging purposes.
      Expression: ${ounce.core}

    createVariables (Default: true)
      Whether the plugin should use the Ounce Automation Server to create any
      necessary variables (such as M2_REPO). Requires that the Ounce Automation
      Server be installed.
      Expression: ${ounce.createVariables}

    externalJars (Default: ${basedir})
      Specifies external jars to be added to the project classpath. These are
      typically jar files that are part of the application server installation.
      Comma separated list of files with absolute path.
      Expression: ${ounce.externalJars}

    importStrutsValidation (Default: false)
      Whether to import Struts validation routines
      Expression: ${ounce.importStrutsValidation}

    includeTestSources (Default: false)
      If TestSources should be included in the compilable sources. If set, adds
      project.getTestSourceRoot() to the path and defaults the classpathScope to
      test.
      Expression: ${ounce.includeTestSources}

    installDir
      The location of the Ounce client installation directory. Required if
      ounceauto is not on the path.
      Expression: ${ounce.installDir}

    javaCompilerOptions
      Options to pass to the javac compiler.
      Expression: ${ounce.javaCompilerOptions}

    jdkName
      JDK configuration known to Ounce Core.
      Expression: ${ounce.jdkName}

    jspCompilerName (Default: Tomcat 8)
      JSP compiler type name / application server.
      Expression: ${ounce.jspCompilerName}

    jspCompilerType (Default: 13)
      JSP compiler type name / application server.
      Expression: ${ounce.jspCompilerType}

    pathVariableMap
      Map of Ounce variable names and paths.
      pathVariableMap variables are automatically registered with Ounce by the
      Ounce/Maven plugin if the Ounce Automation Server is installed.

    precompileScan (Default: false)
      (no description available)
      Expression: ${ounce.preCompileScan}

    projectDir (Default: ${basedir})
      Specifies the directory where to create the ppf file
      Expression: ${ounce.projectDir}

    projectFile (Default: ${basedir}/${project.artifactId}.ppf)
      Specifies the location to create the ppf file
      Expression: ${ounce.projectFile}

    skipPoms (Default: true)
      If pom packaging projects should be skipped. Typically these will not have
      source code and should be excluded. This is true by default because
      typically the application or projects will be created at a pom level and
      the poms have no source to be analyzed Only set this if you have source in
      your 'pom' packaging projects that needs to be scanned.
      Expression: ${ounce.skipPoms}

    srcRoots
      Specifies the location of the source root
      Expression: ${ounce.srcRoots}

    webappDirectory (Default: ${basedir}/src/main/webapp)
      The location of the web context root, if needed.
      Expression: ${ounce.webappDir}

ounce:project-only<br>
  This mojo generates an Ounce project file. It does not fork the build like the
  'project' mojo and is instead intended to be bound in a pom for automatic
  execution. If you would rather have the project generated on demand via the
  command line, use the project goal instead.

  Available parameters:

    analyzeStrutsFramework (Default: false)
      Whether to analyze the framework for a Struts application
      Expression: ${ounce.analyzeStrutsFramework}

    appDir (Default: ${basedir})
      Specifies the directory where to create the paf file
      Expression: ${ounce.appDir}

    classpathScope (Default: compile)
      The scope of the classpath used to analyze this project.
      Valid choices are: compile, test, runtime, or system. If
      includeTestSources is true, then the classpathScope reverts to test.
      Otherwise, the default is compile.
      Expression: ${ounce.classpathScope}

    coreHint (Default: ouncexml)
      This hint provides a way to switch the core implementation. Consult Ounce
      support for details, most users should leave this set to the default. Use
      -Dounce.core=console to have have the output displayed instead of written
      to the file for debugging purposes.
      Expression: ${ounce.core}

    createVariables (Default: true)
      Whether the plugin should use the Ounce Automation Server to create any
      necessary variables (such as M2_REPO). Requires that the Ounce Automation
      Server be installed.
      Expression: ${ounce.createVariables}

    externalJars (Default: ${basedir})
      Specifies external jars to be added to the project classpath. These are
      typically jar files that are part of the application server installation.
      Comma separated list of files with absolute path.
      Expression: ${ounce.externalJars}

    importStrutsValidation (Default: false)
      Whether to import Struts validation routines
      Expression: ${ounce.importStrutsValidation}

    includeTestSources (Default: false)
      If TestSources should be included in the compilable sources. If set, adds
      project.getTestSourceRoot() to the path and defaults the classpathScope to
      test.
      Expression: ${ounce.includeTestSources}

    installDir
      The location of the Ounce client installation directory. Required if
      ounceauto is not on the path.
      Expression: ${ounce.installDir}

    javaCompilerOptions
      Options to pass to the javac compiler.
      Expression: ${ounce.javaCompilerOptions}

    jdkName
      JDK configuration known to Ounce Core.
      Expression: ${ounce.jdkName}

    jspCompilerName (Default: Tomcat 8)
      JSP compiler type name / application server.
      Expression: ${ounce.jspCompilerName}

    jspCompilerType (Default: 13)
      JSP compiler type name / application server.
      Expression: ${ounce.jspCompilerType}

    pathVariableMap
      Map of Ounce variable names and paths.
      pathVariableMap variables are automatically registered with Ounce by the
      Ounce/Maven plugin if the Ounce Automation Server is installed.

    precompileScan (Default: false)
      (no description available)
      Expression: ${ounce.preCompileScan}

    projectDir (Default: ${basedir})
      Specifies the directory where to create the ppf file
      Expression: ${ounce.projectDir}

    projectFile (Default: ${basedir}/${project.artifactId}.ppf)
      Specifies the location to create the ppf file
      Expression: ${ounce.projectFile}

    skipPoms (Default: true)
      If pom packaging projects should be skipped. Typically these will not have
      source code and should be excluded. This is true by default because
      typically the application or projects will be created at a pom level and
      the poms have no source to be analyzed Only set this if you have source in
      your 'pom' packaging projects that needs to be scanned.
      Expression: ${ounce.skipPoms}

    srcRoots
      Specifies the location of the source root
      Expression: ${ounce.srcRoots}

    webappDirectory (Default: ${basedir}/src/main/webapp)
      The location of the web context root, if needed.
      Expression: ${ounce.webappDir}

ounce:publishASE<br>
  This mojo provides the ability to publish an AppScan Source assessment to
  AppScan Enterprise

  Available parameters:

    aseApplication
      Optional. Name of the application that is going to be used to publish on
			the AppScan Enterprise.
      Expression: ${ounce.aseApplication}

    assessmentOutput (Default: ${basedir}/${project.artifactId}.ozasmt)
      This is the assessment file that will be published to AppScan Enterprise
      Expression: ${ounce.assessmentOutput}

    caller
      Optional. Assign a caller to the report generation operation. The caller
      can be the name of an actual user, but this is not required. The caller
      name is written to the ounceauto log file
      Expression: ${ounce.caller}

    coreHint (Default: ouncexml)
      This hint provides a way to switch the core implementation. Consult Ounce
      support for details, most users should leave this set to the default. Use
      -Dounce.core=console to have have the output displayed instead of written
      to the file for debugging purposes.
      Expression: ${ounce.core}

    folderID
      Optional. Enterprise Console folder to publish to. If this argument is not
      used, the assessment will be published to your default Enterprise Console
      folder.
      Expression: ${ounce.folderID}

    installDir
      The location of the Ounce client installation directory if the Ounce
      client is not on the path
      Expression: ${ounce.installDir}

    pathVariableMap
      Map of Ounce variable names and paths.
      pathVariableMap variables are automatically registered with Ounce by the
      Ounce/Maven plugin if the Ounce Automation Server is installed.

    skipPoms (Default: true)
      If pom packaging projects should be skipped. Typically these will not have
      source code and should be excluded. This is true by default because
      typically the application or projects will be created at a pom level and
      the poms have no source to be analyzed Only set this if you have source in
      your 'pom' packaging projects that needs to be scanned.
      Expression: ${ounce.skipPoms}

    waitForScan (Default: false)
      Forces the goal to wait until the scan finishes, thus blocking the Maven
      build. This is useful if the scan is being performed from the report mojo
      as part of integration with the site target and the site is getting
      deployed.
      Expression: ${ounce.wait}

ounce:report<br>
  Generate the scan results as part of the site.

  Available parameters:

    applicationFile (Default: ${ounce.appDir}/${project.artifactId}.paf)
      The location of the application file (.paf) to scan.
      DO NOT USE
      Expression: ${ounce.applicationFile}

    appserver_type
      If the application that you are opening includes JavaServer Pages (for
      example, a WAR or EAR file), use this setting to specify the application
      server to use for JSP compilation. Specify one of these, in double
      quotation marks: Tomcat 5, Tomcat 6, Tomcat 7, Tomcat 8, WebSphere 6.1,
      WebSphere 7.0, WebSphere 8.0, WebSphere 8.5, WebLogic 8, WebLogic 9,
      WebLogic 11g or WebLogic 12c Command line variable: -Dounce.appserver_type
      Example: -Dounce.appserver_type='WebSphere 8.5'
      Expression: ${ounce.appserver_type}

    assessmentName
      A name to help identify the assessment.
      Expression: ${ounce.assessmentName}

    assessmentOutput
      A filename to which to save the assessment.
      If filename is not specified, Ounce/Maven generates a name based on the
      application name and timestamp and saves it to the applications working
      directory.
      Command line variable: -Dounce.assessmentOutput
      Example: -Dounce.assessmentOutput='MyAssessment.ozasmt'
      Expression: ${ounce.assessmentOutput}

    caller
      A short string to help identify the corresponding entries in the ounceauto
      log file.
      Command line variable: -Dounce.caller
      Expression: ${ounce.caller}

    coreHint (Default: ouncexml)
      This hint provides a way to switch the core implementation. Consult Ounce
      support for details, most users should leave this set to the default. Use
      -Dounce.core=console to have have the output displayed instead of written
      to the file for debugging purposes.
      Expression: ${ounce.core}

    existingAssessmentFile
      Specify the name of an existing assessment for which to generate a report.
      If not specified, Ounce/Maven scans the application and generates the
      report from that assessment.
      Expression: ${ounce.existingAssessmentFile}

    includeSrcAfter
      Number of lines of source code to include in the report after each
      finding.
      Command line variable: -Dounce.includeSrcAfter
      Example: -Dounce.includeSrcAfter=5
      Expression: ${ounce.includeSrcAfter}

    includeSrcBefore
      Number of lines of source code to include in the report before each
      finding.
      Command line variable: -Dounce.includeSrcBefore
      Example: -Dounce.includeSrcBefore=5
      Expression: ${ounce.includeSrcBefore}

    includeTraceCoverage
      Include trace information in the report for scan coverage findings.
      Command line variable: -Dounce.includeTraceCoverage Example:
      -Dounce.includeTraceCoverage=true
      Expression: ${ounce.includeTraceCoverage}

    includeTraceDefinitive
      Include trace information in the report for definitive findings.
      Command line variable: -Dounce.includeTraceDefinitive
      Example: -Dounce.includeTraceDefinitive=true
      Expression: ${ounce.includeTraceDefinitive}

    includeTraceSuspect
      Include trace information in the report for suspect findings.
      Command line variable: -Dounce.includeTraceSuspect
      Example: -Dounce.includeTraceSuspect=true
      Expression: ${ounce.includeTraceSuspect}

    installDir
      The location of the Ounce client installation directory if the Ounce
      client is not on the path.
      Command line variable: -Dounce.installDir
      Example: Dounce.installDir='C:\Program Files (x86)\IBM\AppScanSource'
      Expression: ${ounce.installDir}

    pathVariableMap
      Map of Ounce variable names and paths.
      pathVariableMap variables are automatically registered with Ounce by the
      Ounce/Maven plugin if the Ounce Automation Server is installed.

    projectFile
      (no description available)
      Expression: ${ounce.projectFile}

    publish (Default: false)
      Automatically publish the assessment following the completion of the scan.
      Command line variable: -Dounce.publish
      Example: -Dounce.publish=true
      Expression: ${ounce.publish}

    reportOutputPath
      The path to which to write the report specified in reportType. Required
      with reportType.
      Command line variable: -Dounce.reportOutputPath
      Example: -Dounce.reportOutputPath='C:\MyReports'
      Expression: ${ounce.reportOutputPath}

    reportOutputType
      The output to generate for the report specified in reportType. Required
      with reportType. Output type may be html, zip, pdf-summary, pdf-detailed,
      pdf-comprehensive, or pdf-annotated.
      Command line variable: -Dounce.reportOutputType
      Example: -Dounce.reportOutputType='html'
      Expression: ${ounce.reportOutputType}

    reportType
      Generates an Ounce report of the specified type, including findings
      reports, SmartAudit Reports, and, if available, custom reports.
      Ounce/Maven generates a report for this assessment after the scan
      completes.
      The following report types are included: Findings, Findings By CWE,
      Findings By API, Findings By Classification, Findings By File, Findings By
      Type, Findings By Bundle, OWASP Top Ten, PCI Data Security Standard, Ounce
      Software Security Profile, or OWASP Top Ten 2007. If you specify
      reportType, then reportOutputType and reportOutputPath are required.
      Command line variable: -Dounce.reportType
      Example: -Dounce.reportType='Findings'
      Expression: ${ounce.reportType}

    scanConfig
      Allows a scan configuration to be specified for the scan
      Command line variable: -Dounce.scanconfig
      Example: -Dounce.scanconfig='Normal scan'
      Expression: ${ounce.scanconfig}

    skipPoms (Default: true)
      If pom packaging projects should be skipped. Typically these will not have
      source code and should be excluded. This is true by default because
      typically the application or projects will be created at a pom level and
      the poms have no source to be analyzed Only set this if you have source in
      your 'pom' packaging projects that needs to be scanned.
      Expression: ${ounce.skipPoms}

    waitForScan (Default: false)
      Forces the goal to wait until the scan finishes, thus blocking the Maven
      build. This is useful if the scan is being performed from the report mojo
      as part of integration with the site target and the site is getting
      deployed.
      Command line variable: -Dounce.wait
      Example: -Dounce.wait=true
      Expression: ${ounce.wait}

ounce:scan<br>
  This mojo allows an on demand scan of an application and the optional
  publishing of the results.

  Available parameters:

    applicationFile (Default: ${ounce.appDir}/${project.artifactId}.paf)
      The location of the application file (.paf) to scan.
      DO NOT USE
      Expression: ${ounce.applicationFile}

    appserver_type
      If the application that you are opening includes JavaServer Pages (for
      example, a WAR or EAR file), use this setting to specify the application
      server to use for JSP compilation. Specify one of these, in double
      quotation marks: Tomcat 5, Tomcat 6, Tomcat 7, Tomcat 8, WebSphere 6.1,
      WebSphere 7.0, WebSphere 8.0, WebSphere 8.5, WebLogic 8, WebLogic 9,
      WebLogic 11g or WebLogic 12c Command line variable: -Dounce.appserver_type
      Example: -Dounce.appserver_type='WebSphere 8.5'
      Expression: ${ounce.appserver_type}

    assessmentName
      A name to help identify the assessment.
      Expression: ${ounce.assessmentName}

    assessmentOutput
      A filename to which to save the assessment.
      If filename is not specified, Ounce/Maven generates a name based on the
      application name and timestamp and saves it to the applications working
      directory.
      Command line variable: -Dounce.assessmentOutput
      Example: -Dounce.assessmentOutput='MyAssessment.ozasmt'
      Expression: ${ounce.assessmentOutput}

    caller
      A short string to help identify the corresponding entries in the ounceauto
      log file.
      Command line variable: -Dounce.caller
      Expression: ${ounce.caller}

    coreHint (Default: ouncexml)
      This hint provides a way to switch the core implementation. Consult Ounce
      support for details, most users should leave this set to the default. Use
      -Dounce.core=console to have have the output displayed instead of written
      to the file for debugging purposes.
      Expression: ${ounce.core}

    includeSrcAfter
      Number of lines of source code to include in the report after each
      finding.
      Command line variable: -Dounce.includeSrcAfter
      Example: -Dounce.includeSrcAfter=5
      Expression: ${ounce.includeSrcAfter}

    includeSrcBefore
      Number of lines of source code to include in the report before each
      finding.
      Command line variable: -Dounce.includeSrcBefore
      Example: -Dounce.includeSrcBefore=5
      Expression: ${ounce.includeSrcBefore}

    includeTraceCoverage
      Include trace information in the report for scan coverage findings.
      Command line variable: -Dounce.includeTraceCoverage Example:
      -Dounce.includeTraceCoverage=true
      Expression: ${ounce.includeTraceCoverage}

    includeTraceDefinitive
      Include trace information in the report for definitive findings.
      Command line variable: -Dounce.includeTraceDefinitive
      Example: -Dounce.includeTraceDefinitive=true
      Expression: ${ounce.includeTraceDefinitive}

    includeTraceSuspect
      Include trace information in the report for suspect findings.
      Command line variable: -Dounce.includeTraceSuspect
      Example: -Dounce.includeTraceSuspect=true
      Expression: ${ounce.includeTraceSuspect}

    installDir
      The location of the Ounce client installation directory if the Ounce
      client is not on the path.
      Command line variable: -Dounce.installDir
      Example: Dounce.installDir='C:\Program Files (x86)\IBM\AppScanSource'
      Expression: ${ounce.installDir}

    pathVariableMap
      Map of Ounce variable names and paths.
      pathVariableMap variables are automatically registered with Ounce by the
      Ounce/Maven plugin if the Ounce Automation Server is installed.

    projectFile
      (no description available)
      Expression: ${ounce.projectFile}

    publish (Default: false)
      Automatically publish the assessment following the completion of the scan.
      Command line variable: -Dounce.publish
      Example: -Dounce.publish=true
      Expression: ${ounce.publish}

    reportOutputPath
      The path to which to write the report specified in reportType. Required
      with reportType.
      Command line variable: -Dounce.reportOutputPath
      Example: -Dounce.reportOutputPath='C:\MyReports'
      Expression: ${ounce.reportOutputPath}

    reportOutputType
      The output to generate for the report specified in reportType. Required
      with reportType. Output type may be html, zip, pdf-summary, pdf-detailed,
      pdf-comprehensive, or pdf-annotated.
      Command line variable: -Dounce.reportOutputType
      Example: -Dounce.reportOutputType='html'
      Expression: ${ounce.reportOutputType}

    reportType
      Generates an Ounce report of the specified type, including findings
      reports, SmartAudit Reports, and, if available, custom reports.
      Ounce/Maven generates a report for this assessment after the scan
      completes.
      The following report types are included: Findings, Findings By CWE,
      Findings By API, Findings By Classification, Findings By File, Findings By
      Type, Findings By Bundle, OWASP Top Ten, PCI Data Security Standard, Ounce
      Software Security Profile, or OWASP Top Ten 2007. If you specify
      reportType, then reportOutputType and reportOutputPath are required.
      Command line variable: -Dounce.reportType
      Example: -Dounce.reportType='Findings'
      Expression: ${ounce.reportType}

    scanConfig
      Allows a scan configuration to be specified for the scan
      Command line variable: -Dounce.scanconfig
      Example: -Dounce.scanconfig='Normal scan'
      Expression: ${ounce.scanconfig}

    skipPoms (Default: true)
      If pom packaging projects should be skipped. Typically these will not have
      source code and should be excluded. This is true by default because
      typically the application or projects will be created at a pom level and
      the poms have no source to be analyzed Only set this if you have source in
      your 'pom' packaging projects that needs to be scanned.
      Expression: ${ounce.skipPoms}

    waitForScan (Default: false)
      Forces the goal to wait until the scan finishes, thus blocking the Maven
      build. This is useful if the scan is being performed from the report mojo
      as part of integration with the site target and the site is getting
      deployed.
      Command line variable: -Dounce.wait
      Example: -Dounce.wait=true
      Expression: ${ounce.wait}

# Examples

<b>Help</b><br>
$mvn ounce:help -Ddetail=true

<b>Generate application and project files</b><br>
$mvn clean install ounce:application ounce:project

<b>Generate application and project files in AppScanSource specific folder outside the project root</b><br>
$mvn clean install -Dounce.appName=MyApp -Dounce.appDir=C:\Code\MyApp\AppScanSource\Application -Dounce.projectDir=C:\Code\MyApp\AppScanSource ounce:application ounce:project

# License

All files found in this project are licensed under the [Apache License 2.0](LICENSE).
