package za.co.entelect.tools.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronald22 on 29/09/2017.
 */
public class ClassLoaderWrapper {
    private URLClassLoader classLoader;

    public ClassLoaderWrapper(MavenSession session) throws MojoExecutionException {
        try {
            MavenProject project = session.getCurrentProject();
            List<URL> urls = new ArrayList<URL>();
            for (String runtimeClasspathElement : project.getRuntimeClasspathElements()) {
                urls.add(new File(runtimeClasspathElement).toURI().toURL());
            }

            for (Artifact artifact : project.getDependencyArtifacts()) {
                Artifact oFoundArtifact = session.getLocalRepository().find(artifact);
                if ("compile".equalsIgnoreCase(oFoundArtifact.getScope())) {
                    urls.add(oFoundArtifact.getFile().toURI().toURL());
                }
            }

            this.classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            PluginLogger.error(e.getMessage());
            e.printStackTrace();
            throw new MojoExecutionException("Failed to create maven runtime classLoader", e);
        }
    }

    public URLClassLoader getClassLoader() {
        return this.classLoader;
    }
}
