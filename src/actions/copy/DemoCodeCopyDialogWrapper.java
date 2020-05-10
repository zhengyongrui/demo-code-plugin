package actions.copy;

import com.intellij.openapi.ui.DialogWrapper;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static javax.swing.BoxLayout.Y_AXIS;

/**
 * @author zhengyongrui
 * Create In 2020/5/2 6:03 下午
 */
public class DemoCodeCopyDialogWrapper extends DialogWrapper {

    private final JXTextField pageNameText = new JXTextField("pageName:包路径");

    private final JXTextField domainText = new JXTextField("domainName:对象名称");

    private final JXTextField descriptionText = new JXTextField("description:对象描述");

    public DemoCodeCopyDialogWrapper() {
        super(true);
        init();
        setTitle("根据Demo创建代码");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JXPanel panel = new JXPanel();

        panel.setSize(100, 100);

        BoxLayout lo = new BoxLayout(panel, Y_AXIS);
        panel.setLayout(lo);

        panel.add(domainText);

        panel.add(pageNameText);

        panel.add(descriptionText);

        return panel;
    }

    /**
     * 获取包名
     *
     * @return String
     */
    public String getPackageName() {
        return pageNameText.getText();
    }

    public String getDomain() {
        return domainText.getText();
    }

    public String getDescription() {
        return descriptionText.getText();
    }

}
