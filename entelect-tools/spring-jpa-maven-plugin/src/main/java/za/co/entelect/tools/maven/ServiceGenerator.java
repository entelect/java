package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by ronald.mthombeni on 2017/09/22.
 */
@Getter
@Setter
public class ServiceGenerator extends CodeGenerator {
    private static final String DEFAULT_ABSTRACT_SERVICE_CLASS_NAME = "AbstractService";
    private static final String DEFAULT_ABSTRACT_SERVICE_PREFIX = "Abstract";

    private JavaComponentPackage repositoryComponentPackage;
    private String servicePostfix;
    private String repositoryPostfix;
    private Boolean implementServices;
    private RepositoryGenerator repositoryGenerator;

    public ServiceGenerator(MavenConfiguration mavenConfiguration, JpaEntityScanner scanner, ClassLoader classLoader, JavaComponentPackage entityComponentPackage, JavaComponentPackage repositoryComponentPackage, String servicePostfix, String repositoryPostfix) {
        super(mavenConfiguration, scanner, classLoader, entityComponentPackage);
        this.repositoryComponentPackage = repositoryComponentPackage;
        this.servicePostfix = servicePostfix;
        this.repositoryPostfix = repositoryPostfix;

        this.repositoryGenerator = new RepositoryGenerator(mavenConfiguration, scanner, classLoader, entityComponentPackage, repositoryPostfix);
    }

    @Override
    protected void doGenerateCode(File outputDirectory, JavaComponentPackage generatedComponentPackage, Class<?> entityClass, VelocityContext context, boolean overwriteSources) throws Exception {
        possiblyGenerateRepository(outputDirectory, entityClass, context, overwriteSources);

        doGenerateAbstractServiceCode(outputDirectory, generatedComponentPackage, context);

        String entitySimpleTypeName = entityClass.getSimpleName();
        String serviceSimpleTypeName = entitySimpleTypeName + servicePostfix;
        String repositorySimpleTypeName = entitySimpleTypeName + repositoryPostfix;
        String abstractServiceSimpleTypeName = DEFAULT_ABSTRACT_SERVICE_PREFIX + serviceSimpleTypeName;

        context.put("servicePackage", generatedComponentPackage.asPackage());
        context.put("entityImport", entityClass.getName());
        context.put("repositoryImport", repositoryComponentPackage.asPackage(repositorySimpleTypeName));
        context.put("repositorySimpleTypeName", repositorySimpleTypeName);
        context.put("entitySimpleTypeName", entitySimpleTypeName);
        context.put("serviceSimpleTypeName", serviceSimpleTypeName);

        context.put("abstractServiceSimpleTypeName", abstractServiceSimpleTypeName);

        String result = mergeWithTemplate(TEMPLATES_PREFIX + getDesiredTemplate(), context);

        File javaComponentFile = createJavaComponentFile(outputDirectory, generatedComponentPackage, implementServices ? serviceSimpleTypeName : abstractServiceSimpleTypeName);

        if (javaComponentFile.exists() && overwriteSources) {
            FileUtils.deleteQuietly(javaComponentFile);
        }

        if (!javaComponentFile.exists()) {
            FileUtils.write(javaComponentFile, result, Charset.defaultCharset());
        }
    }

    private void possiblyGenerateRepository(File outputDirectory, Class<?> entityClass, VelocityContext context, boolean overwriteSources) throws Exception {
        String entitySimpleTypeName = entityClass.getSimpleName();
        String repositorySimpleTypeName = entitySimpleTypeName + repositoryPostfix;
        File javaComponentFile = createJavaComponentFile(outputDirectory, repositoryComponentPackage, repositorySimpleTypeName);
        if (!javaComponentFile.exists()) {
            repositoryGenerator.doGenerateCode(outputDirectory, repositoryComponentPackage, entityClass, context, overwriteSources);
        }
    }

    private String getDesiredTemplate() {
        if (implementServices) {
            return "templates/service.vm";
        }

        return "templates/abstract-entity-service.vm";
    }

    private void doGenerateAbstractServiceCode(File outputDirectory, JavaComponentPackage generatedComponentPackage, VelocityContext context) throws IOException {
        File javaComponentFile = createJavaComponentFile(outputDirectory, generatedComponentPackage, DEFAULT_ABSTRACT_SERVICE_CLASS_NAME);
        if (!javaComponentFile.exists()) {
            context.put("servicePackage", generatedComponentPackage.asPackage());
            String result = mergeWithTemplate(TEMPLATES_PREFIX + "templates/abstract-service.vm", context);
            FileUtils.write(javaComponentFile, result, Charset.defaultCharset());
        }
    }
}
