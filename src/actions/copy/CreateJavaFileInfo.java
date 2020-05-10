package actions.copy;

import com.intellij.openapi.fileTypes.StdFileTypes;

import java.util.Optional;

/**
 * 要创建的文件信息
 *
 * @author zhengyongrui
 * Create In 2020/5/3 3:14 下午
 */
public class CreateJavaFileInfo {

    private String createDirectoryPath;

    private String domain;

    private String domainLowercase;

    private String description;

    /**
     * 创建的包名，不包含模板的包名
     */
    private String rootPackageName;

    /**
     * 模版代码文本
     */
    private String fileTemplateText;

    /**
     * 要创建的文件类或接口名称
     */
    private String className;

    /**
     * 要创建的文件名
     */
    private String fileName;

    public String getCreateDirectoryPath() {
        return createDirectoryPath;
    }

    public void setCreateDirectoryPath(String createDirectoryPath) {
        this.createDirectoryPath = createDirectoryPath;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileTemplateText() {
        return fileTemplateText;
    }

    public void setFileTemplateText(String fileTemplateText) {
        this.fileTemplateText = fileTemplateText;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDomainLowercase() {
        return Optional.ofNullable(domainLowercase).orElse(getDomain().toLowerCase());
    }

    public void setDomainLowercase(String domainLowercase) {
        this.domainLowercase = domainLowercase;
    }

    public String getFileName() {
        return Optional.ofNullable(fileName).orElse(getClassName() + "." + StdFileTypes.JAVA.getDefaultExtension());
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRootPackageName() {
        return rootPackageName;
    }

    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }
}
