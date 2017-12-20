package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
@Getter
@Setter
public abstract class SpringJpaMojo extends AbstractMojo {
    protected ClassLoaderWrapper classLoaderWrapper = null;

    @Parameter(property = "outputDirectory", required = false, defaultValue = "${project.build.sourceDirectory}")
    protected File outputDirectory;
    @Parameter(property = "entityScanPackage", required = true)
    protected String entityScanPackage;
    @Parameter(property = "suppressEntities", required = true)
    protected List<String> suppressEntities;
    @Parameter(property = "skip", defaultValue = "false")
    protected Boolean skip = false;
    @Parameter(property = "overwriteSources")
    protected Boolean overwriteSources = false;
    @Parameter(property = "autoCompileEntities")
    protected Boolean autoCompileEntities = true;

    @Component
    protected MavenProject mavenProject;
    @Component
    protected MavenSession mavenSession;
    @Component
    protected BuildPluginManager pluginManager;

    ServiceLocator serviceLocator;

    public final void execute() throws MojoExecutionException, MojoFailureException {
        PluginLogger.configure(getLog());

        if (skip) {
            PluginLogger.info("Skipped execution of Spring Jpa Plugin as per configuration");
            return;
        }

        if (StringUtils.isEmpty(entityScanPackage)) {
            throw new MojoExecutionException("entityScanPackage has not been specified");
        }

        if (StringUtils.isEmpty(outputDirectory)) {
            outputDirectory = new File(mavenProject.getBuild().getSourceDirectory());
        }

        classLoaderWrapper = new ClassLoaderWrapper(mavenSession);

        doExecute();
    }

    protected void doExecute() throws MojoExecutionException, MojoFailureException {
    }

    protected JpaEntityScanner createEntityScanner() throws MojoFailureException {
        try {
            return new JpaEntityScanner(classLoaderWrapper.getClassLoader());
        } catch (Exception e) {
            throw new MojoFailureException("Failed to create jpa entity scanner", e);
        }
    }

    protected MavenConfiguration buildMavenConfiguration() {
        MavenConfiguration configuration = new MavenConfiguration();
        configuration.project = mavenProject;
        configuration.session = mavenSession;
        configuration.pluginManager = pluginManager;
        return configuration;
    }
}
