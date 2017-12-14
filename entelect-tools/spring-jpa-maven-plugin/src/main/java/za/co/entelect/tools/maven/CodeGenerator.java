package za.co.entelect.tools.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Created by ronald22 on 29/09/2017.
 */
@Getter
@Setter
public abstract class CodeGenerator {
    public static final String TEMPLATES_PREFIX = "templates/";

    private MavenConfiguration mavenConfiguration;
    protected JpaEntityScanner scanner;
    private ClassLoader classLoader;
    protected JavaComponentPackage entityComponentPackage;
    protected VelocityEngine templateEngine;
    protected Boolean autoCompileEntities;

    public CodeGenerator(MavenConfiguration mavenConfiguration, JpaEntityScanner scanner, ClassLoader classLoader, JavaComponentPackage entityComponentPackage) {
        this.mavenConfiguration = mavenConfiguration;
        this.scanner = scanner;
        this.classLoader = classLoader;
        this.entityComponentPackage = entityComponentPackage;
        initTemplateEngine();
    }

    public void setAutoCompileEntities(Boolean autoCompileEntities) {
        this.autoCompileEntities = autoCompileEntities;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void generateCode(File outputDirectory, JavaComponentPackage generatedComponentPackage, List<String> suppressEntities, boolean overwrite) throws MojoExecutionException {
        Set<BeanDefinition> candidateComponents = scanForEntities();
        for (BeanDefinition beanDefinition : candidateComponents) {
            if (isEntitySuppressed(beanDefinition.getBeanClassName(), suppressEntities))
                continue;

            try {
                Class<?> beanClass = ClassUtils.forName(beanDefinition.getBeanClassName(), classLoader);
                Class<?> idClass = discoverIdentityClass(beanClass);

                VelocityContext context = new VelocityContext();
                context.put("entityIdImport", idClass.getName());
                context.put("entityIdSimpleTypeName", idClass.getSimpleName());
                context.put("author", System.getProperty("user.name"));
                context.put("dateGenerated", new Date());

                doGenerateCode(outputDirectory, generatedComponentPackage, beanClass, context, overwrite);
            } catch (Exception e) {
                PluginLogger.warn(e.getMessage());
            }
        }
    }

    private Set<BeanDefinition> scanForEntities() throws MojoExecutionException {
        possiblyAutoCompileEntities();
        return scanner.scanForEntities(entityComponentPackage);
    }

    private void possiblyAutoCompileEntities() throws MojoExecutionException {
        if (shouldAutoCompileEntities()) {
            try {
                executeMojo(
                        plugin(
                                groupId("org.apache.maven.plugins"),
                                artifactId("maven-compiler-plugin"),
                                version("3.7.0")
                        ),
                        goal("compile"),
                        configuration(
                                element(name("compilePath"), compilerClasspathElements()),
                                element(name("outputDirectory"), "${project.build.outputDirectory}"),
                                element(name("includes"), element("includes", entityComponentPackage.asResourcePath() + "/**/*.java"))
                        ),
                        executionEnvironment(
                                mavenConfiguration.project,
                                mavenConfiguration.session,
                                mavenConfiguration.pluginManager
                        )
                );
            } catch (Exception e) {
                PluginLogger.error(e.getMessage());
                e.printStackTrace();
                throw new MojoExecutionException("failed to auto compile entities", e);
            }

            ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper(mavenConfiguration.session);
            this.setClassLoader(classLoaderWrapper.getClassLoader());
            scanner.setResourceClassLoader(classLoaderWrapper.getClassLoader());
        }
    }

    private boolean shouldAutoCompileEntities() {
        File dir = new File(mavenConfiguration.project.getCompileSourceRoots().get(0), entityComponentPackage.asResourcePath());
        return autoCompileEntities && dir.exists() && dir.isDirectory();
    }

    private Element[] compilerClasspathElements() {
        List<String> classpathElements = getMavenRuntimeClasspathElements();
        List<Element> allClasspathElements = new ArrayList<Element>();
        Set<Artifact> artifacts = mavenConfiguration.project.getArtifacts();
        for (Artifact artifact : artifacts) {
            classpathElements.add(artifact.getFile().getAbsolutePath());
        }

        for (String classpathElement : classpathElements) {
            allClasspathElements.add(element("compilePath", classpathElement));
        }

        return allClasspathElements.toArray(new Element[allClasspathElements.size()]);
    }

    private List<String> getMavenRuntimeClasspathElements() {
        List<String> classpathElements = new ArrayList<String>();
        try {
            classpathElements.addAll(
                    mavenConfiguration.project.getRuntimeClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
        }

        return classpathElements;
    }

    private boolean isEntitySuppressed(String className, List<String> suppressEntities) {
        return !CollectionUtils.isEmpty(suppressEntities) && suppressEntities.contains(className);

    }

    protected Class<?> discoverIdentityClass(Class<?> entityClass) throws ClassNotFoundException {
        Class<?> idClass = null;

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
                mustImplementsSerializable(field.getType());
                idClass = field.getType();
                break;
            }
        }

        for (Method method : entityClass.getDeclaredMethods()) {
            if (!method.getReturnType().equals(Void.TYPE) && (method.isAnnotationPresent(Id.class) || method.isAnnotationPresent(EmbeddedId.class))) {
                mustImplementsSerializable(method.getReturnType());
                idClass = method.getReturnType();
                break;
            }
        }

        if (idClass == null) {
            throw new IllegalStateException("Entity class '" + entityClass.getName() + "' does not have @Id field or property");
        }

        return org.apache.commons.lang.ClassUtils.primitiveToWrapper(idClass);
    }

    private void mustImplementsSerializable(Class<?> clazz) {
        if (!Serializable.class.isAssignableFrom(clazz) && !clazz.isPrimitive()) {
            throw new IllegalArgumentException("Type parameter '" + clazz.getName() + "' must implement 'java.io.Serializable'");
        }
    }

    protected abstract void doGenerateCode(File outputDirectory, JavaComponentPackage generatedComponentPackage, Class<?> beanDefinition, VelocityContext context, boolean overwrite) throws Exception;

    private void initTemplateEngine() {
        templateEngine = new VelocityEngine();
        templateEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        templateEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        try {
            templateEngine.init();
        } catch (Exception e) {
            PluginLogger.warn(e.getMessage());
        }
    }

    protected File createJavaComponentFile(File base, JavaComponentPackage componentPackage, String simpleTypeName) throws IOException {
        File javaComponentFile = new File(base, componentPackage.asResourceFile(simpleTypeName + ".java"));
        if (!javaComponentFile.getParentFile().exists()) {
            FileUtils.forceMkdir(javaComponentFile.getParentFile());
        }

        return javaComponentFile;
    }

    protected String mergeWithTemplate(String templateName, VelocityContext context) {
        Writer writer = new StringWriter();
        Template template = templateEngine.getTemplate(templateName);
        template.merge(context, writer);
        return writer.toString();
    }


}
