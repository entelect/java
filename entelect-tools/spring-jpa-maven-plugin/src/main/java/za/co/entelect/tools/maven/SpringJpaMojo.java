package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Created by ronald.mthombeni on 2017/09/28.
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

    //@Component
    ServiceLocator serviceLocator;

    @Override
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

    private DependencyRequest dependencyRequest(org.eclipse.aether.artifact.DefaultArtifact artifact, String scope) {
        final DependencyFilter filter =
                DependencyFilterUtils.classpathFilter(scope);
        CollectRequest request = new CollectRequest();
        request.setRoot(new Dependency(artifact, scope));
        request.setRepositories(mavenProject.getRemoteProjectRepositories());

        return new DependencyRequest(request, filter);
    }

    private void configureClassLoader() throws MojoExecutionException {
        try {
            DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler("jar");
            DefaultArtifact artifact = new DefaultArtifact("org.eclipse.aether", "aether-spi", "0.9.1.v20140329", "compile", "jar", "", artifactHandler);
            Artifact resolvedArtifact = mavenSession.getLocalRepository().find(artifact);

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader instanceof ClassRealm) {
                ClassRealm classRealm = (ClassRealm) contextClassLoader;
                classRealm.addURL(resolvedArtifact.getFile().toURI().toURL());
            }
        } catch (Exception e) {
            PluginLogger.error(e.getMessage());
            e.printStackTrace();
            throw new MojoExecutionException("Failed to create parent-last child-first class loader", e);
        }
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
