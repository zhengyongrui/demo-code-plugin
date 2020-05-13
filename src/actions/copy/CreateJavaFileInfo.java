package actions.copy;

import com.intellij.openapi.fileTypes.StdFileTypes;

import java.util.Map;
import java.util.Optional;

/**
 * 要创建的文件信息
 *
 * @author zhengyongrui
 * Create In 2020/5/3 3:14 下午
 */
public class CreateJavaFileInfo {

    /**
     * 创建的java文件的路径
     */
    private String createDirectoryPath;

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

    /**
     * 模板参数
     */
    private Map<String, String> templateParamMap;

    public String getCreateDirectoryPath() {
        return createDirectoryPath;
    }

    public void setCreateDirectoryPath(String createDirectoryPath) {
        this.createDirectoryPath = createDirectoryPath;
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

    public Map<String, String> getTemplateParamMap() {
        return templateParamMap;
    }

    public void setTemplateParamMap(Map<String, String> templateParamMap) {
        this.templateParamMap = templateParamMap;
    }
}
