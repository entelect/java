package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.beans.BeanUtils;

/**
 * Created by ronald.mthombeni on 2017/09/21.
 */
@Getter
@Setter
@Mojo(name = "all", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AllMojo extends SpringJpaMojo {
    @Parameter(property = "servicePackage", required = true)
    private String servicePackage;
    @Parameter(property = "servicePostfix", required = false, defaultValue = ServiceMojo.DEFAULT_SERVICE_POSTFIX)
    private String servicePostfix;
    @Parameter(property = "repositoryPackage", required = true)
    private String repositoryPackage;
    @Parameter(property = "repositoryPostfix", required = false, defaultValue = RepositoryMojo.DEFAULT_REPOSITORY_POSTFIX)
    private String repositoryPostfix;
    @Parameter(defaultValue = "true", required = false)
    private boolean implementServices = true;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        generateRepositories();
        generateServices();
    }

    private void generateRepositories() throws MojoFailureException, MojoExecutionException {
        RepositoryMojo repositoryMojo = new RepositoryMojo();
        configureMojo(repositoryMojo);
        repositoryMojo.execute();
    }

    private void generateServices() throws MojoFailureException, MojoExecutionException {
        ServiceMojo serviceMojo = new ServiceMojo();
        configureMojo(serviceMojo);
        serviceMojo.execute();
    }

    private void configureMojo(SpringJpaMojo mojo) {
        BeanUtils.copyProperties(this, mojo);
    }
}
