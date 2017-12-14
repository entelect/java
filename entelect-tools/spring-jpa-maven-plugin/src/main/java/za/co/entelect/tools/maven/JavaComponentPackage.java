package za.co.entelect.tools.maven;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.ClassUtils;

/**
 * Created by ronald.mthombeni on 2017/09/22.
 */
public class JavaComponentPackage {
    private String componentPackage;

    public JavaComponentPackage(String sPackage) {
        this.componentPackage = sPackage;
    }

    public String asPackage() {
        return componentPackage;
    }

    public String asPackage(String postfix) {
        postfix = postfix.startsWith(".") ? postfix : "." + postfix;
        return componentPackage + postfix;
    }

    public String asResourcePath() {
        return ClassUtils.convertClassNameToResourcePath(componentPackage);
    }

    public String asResourcePath(String postfix) {
        return ClassUtils.convertClassNameToResourcePath(asPackage(postfix));
    }

    public String asResourceFile(String filename) {
        return ClassUtils.convertClassNameToResourcePath(asPackage(FilenameUtils.getBaseName(filename))) + "." + FilenameUtils.getExtension(filename);
    }
}
