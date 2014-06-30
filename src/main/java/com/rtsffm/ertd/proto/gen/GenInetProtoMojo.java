package com.rtsffm.ertd.proto.gen;

import com.rtsffm.ertd.proto.gen.sheetprocessor.XLSXTableModel;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import org.sonatype.plexus.build.incremental.BuildContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//~--- classes ----------------------------------------------------------------

/**
 * Goal which touches a timestamp file.
 *
 */
@Mojo(
    name         = "generate-protocol",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    threadSafe   = true
)
@Execute(
    goal         = "generate-protocol",
    phase        = LifecyclePhase.GENERATE_SOURCES
)
public class GenInetProtoMojo extends AbstractMojo implements LogChute {
    private static final String CLASS_LOADER = "classloader";

    //~--- fields -------------------------------------------------------------

    @Component
    private BuildContext  buildContext;
    @Component
    private MojoExecution execution;

    /**
     * The working directory where the generated Java protocol source files are created.
     */
    @Parameter(
        defaultValue = "${project.build.directory}/generated-sources/proto",
        required     = true
    )
    private File outputDirectory;

    /**
     * The package under which the source files will be generated.
     */
    @Parameter
    protected String packageName;

    /**
     * The default maven project object.
     */
    @Component
    private MavenProject project;

    /**
     * The file location for inet protocol (xlsx file)
     */
    @Parameter(
        defaultValue = "${project.basedir}/src/main/proto",
        required     = true
    )
    private File protoFile;

    /**
     * Sheet Mapping to table columns - separated by comma (,)
     * <pre>
     * &lt;configuration&gt;
     *     &lt;sheetColumnMapping&gt;
     *         &lt;xlsx_sheet_name&gt;FidType, FidBase&lt;/xlsx_sheet_name&gt;
     *     &lt;/sheetColumnMapping&gt;
     * &lt;/configuration&gt;
     * </pre>
     */
    @Parameter
    private Map<String, String> sheetColumnMapping;

    //~--- methods ------------------------------------------------------------

    public void execute() throws MojoExecutionException {
        VelocityEngine engine = new VelocityEngine();

        engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this);
        engine.setProperty(VelocityEngine.RESOURCE_LOADER, "file,classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();

        Map<String, List<String>> sheetColMap = new HashMap<String, List<String>>();

        for (Entry<String, String> entry : sheetColumnMapping.entrySet()) {
            List<String> columns = new ArrayList<String>();

            for (String split : entry.getValue().split("\\s*,\\s*")) {
                columns.add(split.trim());
            }

            sheetColMap.put(entry.getKey(), columns);
        }

        Template     template = engine.getTemplate("/fid_template.vm");
        StringWriter writer   = new StringWriter(10000);

        template.merge(createVelocityContext(sheetColMap), writer);
        getLog().info(writer.toString());
    }

    private VelocityContext createVelocityContext(Map<String, List<String>> sheetColMap) {
        VelocityContext context      = new VelocityContext();
        ModelCreator    modelCreator = new ModelCreator(protoFile, getLog(), sheetColMap);

        for (Entry<String, List<XLSXTableModel>> entry : modelCreator.getProcessedMap().entrySet()) {
            if (entry.getValue().size() == 1) {
                context.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        for (Entry<String, String> ent : System.getenv().entrySet()) {
            context.put(ent.getKey(), ent.getValue());
        }

        context.put(CLASS_LOADER, Thread.currentThread().getContextClassLoader());

        return context;
    }

    @Override
    public void init(RuntimeServices rs) throws Exception {}

    @Override
    public void log(int level, String message) {
        switch (level) {
            case TRACE_ID :
            case DEBUG_ID :
                getLog().debug(message);

                break;

            case WARN_ID :
                getLog().warn(message);

                break;

            case ERROR_ID :
                getLog().error(message);

                break;

            case INFO_ID :
                getLog().info(message);

                break;
        }
    }

    @Override
    public void log(int level, String message, Throwable t) {
        switch (level) {
            case TRACE_ID :
            case DEBUG_ID :
                getLog().debug(message, t);

                break;

            case WARN_ID :
                getLog().warn(message, t);

                break;

            case ERROR_ID :
                getLog().error(message, t);

                break;

            case INFO_ID :
                getLog().info(message, t);

                break;
        }
    }

    //~--- get methods --------------------------------------------------------

    @Override
    public boolean isLevelEnabled(int level) {
        switch (level) {
            case TRACE_ID :
            case DEBUG_ID :
                getLog().isDebugEnabled();

                break;

            case WARN_ID :
                getLog().isWarnEnabled();

                break;

            case ERROR_ID :
                getLog().isErrorEnabled();

                break;

            case INFO_ID :
                getLog().isInfoEnabled();

                break;
        }

        return true;
    }
}
