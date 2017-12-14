package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Created by ronald.mthombeni on 2017/09/22.
 */
@Getter
@Setter
public class RepositoryGenerator extends CodeGenerator {
    private String postfix;

    public RepositoryGenerator(MavenConfiguration mavenConfiguration, JpaEntityScanner scanner, ClassLoader classLoader, JavaComponentPackage entityComponentPackage, String postfix) {
        super(mavenConfiguration, scanner, classLoader, entityComponentPackage);
        this.postfix = postfix;
    }

    @Override
    protected void doGenerateCode(File outputDirectory, JavaComponentPackage generatedComponentPackage, Class<?> entityClass
            , VelocityContext context, boolean overwrite) throws Exception {
        String entitySimpleTypeName = entityClass.getSimpleName();
        String repositorySimpleTypeName = entitySimpleTypeName + postfix;

        context.put("repositoryPackage", generatedComponentPackage.asPackage());
        context.put("entityImport", entityClass.getName());
        context.put("repositorySimpleTypeName", repositorySimpleTypeName);
        context.put("entitySimpleTypeName", entitySimpleTypeName);

        String result = mergeWithTemplate(TEMPLATES_PREFIX + "templates/repository.vm", context);

        File javaComponentFile = createJavaComponentFile(outputDirectory, generatedComponentPackage, repositorySimpleTypeName);

        if (javaComponentFile.exists() && overwrite) {
            FileUtils.deleteQuietly(javaComponentFile);
        }

        if (!javaComponentFile.exists())
            FileUtils.write(javaComponentFile, result, Charset.defaultCharset());
    }
}
