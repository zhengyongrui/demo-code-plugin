package actions.config;

import java.util.List;

/**
 * 模板代码配置
 * 根目录配置，对应json文件
 *
 * @author zhengyongrui
 * Create in 2020/5/11 11:03 下午
 */
public class DemoCodeConfig {

    /**
     * 描述，没啥用，就是觉得需要跟json加个描述，所以加了这个字段
     */
    private String description;

    /**
     * 模板代码列表
     */
    private List<TemplateCodeConfig> demoCodeTemplateList;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TemplateCodeConfig> getDemoCodeTemplateList() {
        return demoCodeTemplateList;
    }

    public void setDemoCodeTemplateList(List<TemplateCodeConfig> demoCodeTemplateList) {
        this.demoCodeTemplateList = demoCodeTemplateList;
    }
}
