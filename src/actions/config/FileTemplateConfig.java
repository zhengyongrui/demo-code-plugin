package actions.config;

/**
 * 模板文件配置
 *
 * @author zhengyongrui
 * Create In 2020/5/4 5:37 下午
 */
public class FileTemplateConfig {

    /**
     * 包名
     */
    private String packageName;

    /**
     * 模板代码内容
     */
    private String text;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
