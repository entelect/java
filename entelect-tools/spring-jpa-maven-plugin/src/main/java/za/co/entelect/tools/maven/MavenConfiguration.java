package za.co.entelect.tools.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.MavenProject;

/**
 * Created by ronald22 on 02/10/2017.
 */
public class MavenConfiguration {
    public MavenProject project;
    public MavenSession session;
    public BuildPluginManager pluginManager;
}
