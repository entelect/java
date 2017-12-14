package za.co.entelect.tools.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.persistence.Entity;
import java.net.MalformedURLException;
import java.util.Set;

/**
 * Created by ronald.mthombeni on 2017/09/22.
 */
public class JpaEntityScanner {
    ClassPathScanningCandidateComponentProvider scanProvider;

    public JpaEntityScanner(ClassLoader classLoader) throws MalformedURLException, DependencyResolutionRequiredException {
        this.scanProvider = new ClassPathScanningCandidateComponentProvider(false);
        this.scanProvider.setResourceLoader(new DefaultResourceLoader(classLoader));
        this.scanProvider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
    }

    public void setResourceClassLoader(ClassLoader classLoader) {
        this.scanProvider.setResourceLoader(new DefaultResourceLoader(classLoader));
    }

    public Set<BeanDefinition> scanForEntities(JavaComponentPackage componentPackage) {
        return scanProvider.findCandidateComponents(componentPackage.asPackage());
    }
}
