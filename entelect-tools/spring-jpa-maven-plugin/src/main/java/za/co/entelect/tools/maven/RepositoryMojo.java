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
 * Created by ronald.mthombeni on 2017/09/21.
 */
@Getter
@Setter
@Mojo(name = "generate-repositories", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = true, threadSafe = true,requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepositoryMojo extends SpringJpaMojo {
    public static final String DEFAULT_REPOSITORY_POSTFIX = "Repository";


    @Parameter(property = "repositoryPackage", required = true)
    private String repositoryPackage;
    @Parameter(property = "repositoryPostfix", required = false, defaultValue = DEFAULT_REPOSITORY_POSTFIX)
    private String repositoryPostfix;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        if (StringUtils.isEmpty(repositoryPackage)) {
            throw new MojoExecutionException("repositoryPackage has not been specified");
        }

        if (StringUtils.isEmpty(repositoryPostfix)) {
            repositoryPostfix = DEFAULT_REPOSITORY_POSTFIX;
        }

        JavaComponentPackage entityComponentPackage = new JavaComponentPackage(entityScanPackage);
        JavaComponentPackage repositoryComponentPackage = new JavaComponentPackage(repositoryPackage);

        JpaEntityScanner scanner = createEntityScanner();

        CodeGenerator generator = new RepositoryGenerator(buildMavenConfiguration(), scanner, classLoaderWrapper.getClassLoader(), entityComponentPackage, repositoryPostfix);
        generator.setAutoCompileEntities(autoCompileEntities);
        generator.generateCode(outputDirectory, repositoryComponentPackage, suppressEntities, overwriteSources);
    }
}
