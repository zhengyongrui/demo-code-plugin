package actions.config;

import java.util.List;

/**
 * 模板代码
 *
 * @author zhengyongrui
 * Create In 2020/5/4 5:36 下午
 */
public class TemplateCodeConfig {

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板代码，为啥是列表的，因为要创建很多个，不然用idea自动的就够了，干嘛自己写插件
     */
    private List<FileTemplateConfig> fileTemplateConfigList;

    public List<FileTemplateConfig> getFileTemplateConfigList() {
        return fileTemplateConfigList;
    }

    public void setFileTemplateConfigList(List<FileTemplateConfig> fileTemplateConfigList) {
        this.fileTemplateConfigList = fileTemplateConfigList;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
