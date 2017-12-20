package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.util.StringUtils;

/**
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
@Getter
@Setter
@Mojo(name = "generate-services", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ServiceMojo extends SpringJpaMojo {
    public static final String DEFAULT_SERVICE_POSTFIX = "Service";


    @Parameter(property = "servicePackage", required = true)
    private String servicePackage;
    @Parameter(property = "servicePostfix", required = false, defaultValue = DEFAULT_SERVICE_POSTFIX)
    private String servicePostfix;
    @Parameter(property = "repositoryPackage", required = true)
    private String repositoryPackage;
    @Parameter(property = "repositoryPostfix", required = false, defaultValue = RepositoryMojo.DEFAULT_REPOSITORY_POSTFIX)
    private String repositoryPostfix;
    @Parameter(defaultValue = "true", required = false)
    private boolean implementServices = true;


    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        if (StringUtils.isEmpty(servicePackage)) {
            throw new MojoExecutionException("servicePackage has not been specified");
        }

        if (StringUtils.isEmpty(servicePostfix)) {
            servicePostfix = DEFAULT_SERVICE_POSTFIX;
        }

        if (StringUtils.isEmpty(repositoryPostfix)) {
            repositoryPostfix = RepositoryMojo.DEFAULT_REPOSITORY_POSTFIX;
        }

        JavaComponentPackage entityComponentPackage = new JavaComponentPackage(entityScanPackage);
        JavaComponentPackage serviceComponentPackage = new JavaComponentPackage(servicePackage);
        JavaComponentPackage repositoryComponentPackage = new JavaComponentPackage(repositoryPackage);

        JpaEntityScanner scanner = createEntityScanner();

        ServiceGenerator generator = new ServiceGenerator(buildMavenConfiguration(), scanner, classLoaderWrapper.getClassLoader(), entityComponentPackage, repositoryComponentPackage, servicePostfix, repositoryPostfix);
        generator.setAutoCompileEntities(autoCompileEntities);
        generator.setImplementServices(implementServices);
        generator.generateCode(outputDirectory, serviceComponentPackage, suppressEntities, overwriteSources);
    }
}
