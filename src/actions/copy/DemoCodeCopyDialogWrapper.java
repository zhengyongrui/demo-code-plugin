package actions.copy;

import actions.config.DemoCodeConfig;
import actions.config.FileTemplateConfig;
import actions.config.TemplateCodeConfig;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.containers.ArrayListSet;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.regex.Pattern.compile;

/**
 * @author zhengyongrui
 * Create In 2020/5/2 6:03 下午
 */
public class DemoCodeCopyDialogWrapper extends DialogWrapper {

    private Map<String, TemplateCodeConfig> templateCodeConfigMap;

    /**
     * 选择的模板
     */
    private TemplateCodeConfig selectTemplateCodeConfig;

    /**
     * 模板参数值
     */
    final private Map<String, JXTextField> templateParamTextMap = new HashMap<>();

    /**
     * 表单项的默认大小
     */
    final private Dimension labelDimension = new Dimension(200, 26);
    final private Dimension valueDimension = new Dimension(200, 26);

    /**
     * idea的默认模板参数，这些模板参数是不需要用户自己输入的
     */
    final private String[] DEFAULT_TEMPLATE_PARAMS = new String[]{
            "ROOT_PACKAGE_NAME", "PACKAGE_NAME", "USER", "DATE", "TIME", "YEAR", "MONTH", "MONTH_NAME_SHORT", "MONTH_NAME_FULL", "DAY", "DAY_NAME_SHORT", "DAY_NAME_FULL", "HOUR", "MINUTE", "PROJECT_NAME"
    };

    public DemoCodeCopyDialogWrapper(DemoCodeConfig demoCodeConfig) {
        super(true);
        initConfig(demoCodeConfig);
        init();
        setTitle("根据Demo创建代码");
    }

    /**
     * 获取模板参数
     *
     * @return map
     */
    public Map<String, String> getTemplateParamMap() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, JXTextField> templateParamTextEntry : templateParamTextMap.entrySet()) {
            result.put(templateParamTextEntry.getKey(), templateParamTextEntry.getValue().getText());
        }
        return result;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JXPanel rootPanel = new JXPanel();

        BoxLayout rootBoxLayout = new BoxLayout(rootPanel, BoxLayout.Y_AXIS);
        rootPanel.setLayout(rootBoxLayout);

        JXPanel templatePanel = new JXPanel();
        rootPanel.add(templatePanel);

        JXPanel templatePropertyPanel = new JXPanel();
        BoxLayout templatePropertyLayout = new BoxLayout(templatePropertyPanel, BoxLayout.Y_AXIS);
        templatePropertyPanel.setLayout(templatePropertyLayout);
        rootPanel.add(templatePropertyPanel);

        JXLabel jxLabel = new JXLabel("请选择模板");
        jxLabel.setPreferredSize(labelDimension);
        templatePanel.add(jxLabel);
        Collection<TemplateCodeConfig> templateCodeConfigCollection = this.templateCodeConfigMap.values();
        JXComboBox jxComboBox = new JXComboBox();
        jxComboBox.setPreferredSize(valueDimension);
        templateCodeConfigCollection.forEach(templateCodeConfig -> jxComboBox.addItem(templateCodeConfig.getTemplateName()));
        Iterator<TemplateCodeConfig> iterator = templateCodeConfigCollection.iterator();
        jxComboBox.setSelectedIndex(0);
        setTemplatePropertyPanel(templatePropertyPanel, templateCodeConfigCollection.iterator().next());
        jxComboBox.addActionListener(e -> {
            String templateName = (String) jxComboBox.getSelectedItem();
            TemplateCodeConfig templateCodeConfig = this.templateCodeConfigMap.get(templateName);
            this.setSelectTemplateCodeConfig(templateCodeConfig);
            setTemplatePropertyPanel(templatePropertyPanel, templateCodeConfig);
        });
        templatePanel.add(jxComboBox);

        return rootPanel;
    }

    /**
     * 设置属性面板
     *
     * @param templatePropertyPanel 模板属性面板
     * @param templateCodeConfig    模板代码配置
     */
    private void setTemplatePropertyPanel(JXPanel templatePropertyPanel, TemplateCodeConfig templateCodeConfig) {
        templatePropertyPanel.removeAll();
        templateParamTextMap.clear();
        Set<String> templateParamList = new ArrayListSet<>();
        // 获取所有面板
        final String allTemplateText = templateCodeConfig.getFileTemplateConfigList().stream().map(FileTemplateConfig::getText).collect(Collectors.joining(""));
        Matcher matcher = compile("\\$\\{(\\w+)\\}").matcher(allTemplateText);
        while (matcher.find()) {
            String group = matcher.group(1);
            // 不属于idea默认模板参数
            if (Stream.of(DEFAULT_TEMPLATE_PARAMS).noneMatch(defaultTemplateParam -> defaultTemplateParam.equals(group))) {
                templateParamList.add(group);
            }
        }
        templateParamList.forEach(templateParam -> {
            JXPanel jxPanel = new JXPanel();
            JXLabel jxLabel = new JXLabel(templateParam);
            jxLabel.setPreferredSize(labelDimension);
            jxPanel.add(jxLabel);
            JXTextField jxTextField = new JXTextField();
            jxTextField.setPreferredSize(valueDimension);
            jxPanel.add(jxTextField);
            templateParamTextMap.put(templateParam, jxTextField);
            templatePropertyPanel.add(jxPanel);
        });
        templatePropertyPanel.revalidate();
    }

    /**
     * 初始化配置
     */
    private void initConfig(DemoCodeConfig demoCodeConfig) {
        final List<TemplateCodeConfig> demoCodeTemplateList = demoCodeConfig.getDemoCodeTemplateList();
        Map<String, TemplateCodeConfig> templateCodeConfigMap = demoCodeTemplateList.stream().collect(Collectors.toMap(TemplateCodeConfig::getTemplateName, val -> val, (newVal, oldVal) -> newVal, LinkedHashMap::new));
        this.templateCodeConfigMap = templateCodeConfigMap;
        setSelectTemplateCodeConfig(demoCodeTemplateList.get(0));
    }

    public TemplateCodeConfig getSelectTemplateCodeConfig() {
        return selectTemplateCodeConfig;
    }

    private void setSelectTemplateCodeConfig(TemplateCodeConfig selectTemplateCodeConfig) {
        this.selectTemplateCodeConfig = selectTemplateCodeConfig;
    }
}
