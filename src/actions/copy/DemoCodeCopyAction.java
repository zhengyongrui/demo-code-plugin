package actions.copy;

import actions.config.DemoCodeConfig;
import com.google.gson.Gson;
import com.intellij.ide.IdeView;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.compile;

/**
 * @author zhengyongrui
 * create in 2020/5/2 5:48 下午
 */
public class DemoCodeCopyAction extends AnAction {

    private JavaDirectoryService javaDirectoryService;

    private AnActionEvent anActionEvent;

    private DemoCodeConfig demoCodeConfig;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        setAnActionEvent(anActionEvent);
        setDemoCodeConfig();
        DemoCodeCopyDialogWrapper dialogWrapper = new DemoCodeCopyDialogWrapper();
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            final String pageName = dialogWrapper.getPackageName();
            final String domain = dialogWrapper.getDomain();
            final String description = dialogWrapper.getDescription();
            exciteCopy(anActionEvent, pageName, description, domain);
        }
    }

    private void setDemoCodeConfig() {
        String demoCodeConfigPath = PropertiesComponent.getInstance().getValue("DemoCodeConfigPath");
        if (StringUtils.isBlank(demoCodeConfigPath)) {
            demoCodeConfigPath = showFileChooser();
            PropertiesComponent.getInstance().setValue("DemoCodeConfigPath", demoCodeConfigPath);
        }
        try {
            StringBuilder demoCodeConfigJsonString = new StringBuilder();
            FileInputStream is = new FileInputStream(new File(demoCodeConfigPath));
            this.demoCodeConfig = transferDemoCodeConfig(demoCodeConfigJsonString, is);
        } catch (IOException e) {
            Messages.showMessageDialog("请选择正确的配置文件!", "Warning", Messages.getWarningIcon());
            throw new IllegalArgumentException("获取配置文件出错", e);
        }
    }

    private DemoCodeConfig transferDemoCodeConfig(StringBuilder demoCodeConfigJsonString, FileInputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String s;
        while ((s = br.readLine()) != null) {
            demoCodeConfigJsonString.append(s);
        }
        Gson gson = new Gson();
        return gson.fromJson(demoCodeConfigJsonString.toString(), DemoCodeConfig.class);
    }

    private String showFileChooser() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.showDialog(new JLabel(), "请选择代码样例配置文件");
        File file = jfc.getSelectedFile();
        if (file != null && file.isFile()) {
            return file.getPath();
        }
        Messages.showMessageDialog("获取配置文件路径出错!", "Warning", Messages.getWarningIcon());
        throw new IllegalArgumentException("获取配置文件路径出错");
    }

    /**
     * 执行代码拷贝
     *
     * @param pageName    包名
     * @param domain      领域名称
     * @param description 描述
     */
    private void exciteCopy(AnActionEvent actionEvent, String pageName, String description, String domain) {
        List<CreateJavaFileInfo> javaFileInfoList = initCreateJavaFileInfo(pageName, description, domain);
        boolean isOverrideFile = checkRepeatFile(javaFileInfoList);
        if (!isOverrideFile) {
            this.actionPerformed(actionEvent);
            return;
        }
        createJavaFile(actionEvent, javaFileInfoList);
        Messages.showMessageDialog(domain + "创建完成!", "Information", Messages.getInformationIcon());
    }

    /**
     * 创建java文件
     *
     * @param actionEvent      actionEvent
     * @param javaFileInfoList 文件列表
     */
    private void createJavaFile(AnActionEvent actionEvent, List<CreateJavaFileInfo> javaFileInfoList) {
        Project project = actionEvent.getProject();
        assert project != null;
        for (CreateJavaFileInfo createJavaFileInfo : javaFileInfoList) {
            Map<String, String> map = new HashMap<>(3);
            map.put("ROOT_PACKAGE_NAME", createJavaFileInfo.getRootPackageName());
            map.put("DOMAIN", createJavaFileInfo.getDomain());
            map.put("DOMAIN_LOWERCASE", createJavaFileInfo.getDomainLowercase());
            map.put("DESCRIPTION", createJavaFileInfo.getDescription());
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(() -> {
                try {
                    final String createDirectoryPath = createJavaFileInfo.getCreateDirectoryPath();
                    VirtualFile createPathFile = VfsUtil.createDirectoryIfMissing(createDirectoryPath);
                    assert createPathFile != null;
                    PsiDirectory createPathDirectory = PsiDirectoryFactory.getInstance(project).createDirectory(createPathFile);
                    FileTemplate fileTemplate = new CustomFileTemplate(createJavaFileInfo.getClassName(), StdFileTypes.JAVA.getDefaultExtension());
                    fileTemplate.setText(createJavaFileInfo.getFileTemplateText());
                    FileTemplateManager.getInstance(project).setTemplates("Default", Collections.singleton(fileTemplate));
                    getDirectoryService().createClass(createPathDirectory, createJavaFileInfo.getClassName(), fileTemplate.getName(), false, map);
                } catch (Exception e) {
                    throw new IllegalArgumentException("创建文件失败", e);
                }
            });
        }
    }

    /**
     * 检查要创建的文件是否有重复，并过滤掉重复掉文件
     *
     * @param javaFileInfoList 文件列表
     * @return 是否覆盖重复文件，如果没有重复，则返回默认值true
     */
    private boolean checkRepeatFile(List<CreateJavaFileInfo> javaFileInfoList) {
        boolean isOverrideFile = true;
        List<VirtualFile> repeatFileList = new ArrayList<>();
        PsiDirectory directory = getCurrentDirectory();
        VirtualFile virtualFile = directory.getVirtualFile();
        Iterator<CreateJavaFileInfo> createJavaFileInfoIterator = javaFileInfoList.iterator();
        while (createJavaFileInfoIterator.hasNext()) {
            CreateJavaFileInfo createJavaFileInfo = createJavaFileInfoIterator.next();
            String filePath = getCreateFilePath(createJavaFileInfo);
            String fileRelativePath = filePath.replace(virtualFile.getPath(), "");
            VirtualFile repeatFile = virtualFile.findFileByRelativePath(fileRelativePath);
            if (repeatFile != null) {
                isOverrideFile = false;
                repeatFileList.add(repeatFile);
                createJavaFileInfoIterator.remove();
            }
        }
        if (!repeatFileList.isEmpty()) {
            String repeatFileWarningMessage = "存在以下文件重复，插件将不会修改这些文件\n";
            String filePathStrings = repeatFileList.stream().map(VirtualFile::getPath).collect(Collectors.joining("\n"));
            repeatFileWarningMessage += filePathStrings;
            int okCancelDialog = Messages.showOkCancelDialog(repeatFileWarningMessage, "警告", "确定", "取消", Messages.getWarningIcon());
            isOverrideFile = okCancelDialog == Messages.OK;
        }
        return isOverrideFile;
    }

    /**
     * 初始化要创建java文件的列表
     *
     * @param pageName    包名
     * @param domain      领域模型
     * @param description 描述
     * @return 。
     */
    private List<CreateJavaFileInfo> initCreateJavaFileInfo(String pageName, String description, String domain) {
        DemoCodeConfig demoCodeConfig = this.demoCodeConfig;
        // 根目录包名
        String rootPath = getCurrentDirectory().getVirtualFile().getPath();
        String rootPackageName = rootPath.replaceAll("[\\\\/]", ".").replaceAll(".*src.main.java", "").replaceAll(".*src", "");
        rootPackageName = rootPackageName.indexOf(".") == 0 ? rootPackageName.substring(1) : rootPackageName;
        final String finalRootPackageName = StringUtils.isNotBlank(rootPackageName) && StringUtils.isNotBlank(pageName) ? rootPackageName + "." + pageName : rootPackageName + pageName;
        List<CreateJavaFileInfo> javaFileInfoList = demoCodeConfig.getFileTemplateConfigList().stream().map(fileTemplateConfig -> {
            CreateJavaFileInfo createJavaFileInfo = new CreateJavaFileInfo();
            createJavaFileInfo.setDomain(domain);
            createJavaFileInfo.setDescription(description);
            createJavaFileInfo.setRootPackageName(finalRootPackageName);
            // 保存模板信息
            createJavaFileInfo.setFileTemplateText(fileTemplateConfig.getText());
            // 组合包名
            String templatePackageName = fileTemplateConfig.getPackageName();
            templatePackageName = StringUtils.isNotBlank(pageName) && StringUtils.isNotBlank(templatePackageName) ? pageName + "." + templatePackageName : pageName + templatePackageName;
            // 转换为路径
            String templatePackagePath = templatePackageName.replaceAll("\\.", "/");
            String createDirectoryPath = getCurrentDirectory().getVirtualFile().getPath() + "/" + templatePackagePath;
            createJavaFileInfo.setCreateDirectoryPath(createDirectoryPath);
            // 类或接口名
            String fileTemplateText = fileTemplateConfig.getText();
            Matcher matcher = compile("public \\w+ (\\S+)").matcher(fileTemplateText);
            if (matcher.find()) {
                String group = matcher.group(1);
                String className = group.replace("${DOMAIN}", domain);
                createJavaFileInfo.setClassName(className);
            }
            return createJavaFileInfo;
        }).collect(Collectors.toList());
        return javaFileInfoList;
    }

    /**
     * 获取当前鼠标定位的文件夹
     *
     * @return 文件夹
     */
    private PsiDirectory getCurrentDirectory() {
        IdeView ideView = this.anActionEvent.getRequiredData(LangDataKeys.IDE_VIEW);
        PsiDirectory directory = ideView.getOrChooseDirectory();
        return directory;
    }

    /**
     * 获取demo代码配置
     *
     * @return demo代码配置
     */
    private DemoCodeConfig getDemoCodeConfig() {
        try {
            StringBuilder demoCodeConfigJsonString = new StringBuilder();
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("DemoCodeConfig.json");
            assert is != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String s;
            while ((s = br.readLine()) != null) {
                demoCodeConfigJsonString.append(s);
            }
            Gson gson = new Gson();
            DemoCodeConfig demoCodeConfig = gson.fromJson(demoCodeConfigJsonString.toString(), DemoCodeConfig.class);
            return demoCodeConfig;
        } catch (IOException e) {
            throw new IllegalArgumentException("获取配置文件出错", e);
        }
    }

    /**
     * 获取要创建的文件路径
     *
     * @param createJavaFileInfo 创建的java文件对象信息
     * @return 路径
     */
    private String getCreateFilePath(CreateJavaFileInfo createJavaFileInfo) {
        final String createDirectoryPath = createJavaFileInfo.getCreateDirectoryPath();
        final String filePath = createDirectoryPath + "/" + createJavaFileInfo.getClassName() + "." + StdFileTypes.JAVA.getDefaultExtension();
        return filePath;
    }

    private void setAnActionEvent(AnActionEvent anActionEvent) {
        this.anActionEvent = anActionEvent;
    }

    private JavaDirectoryService getDirectoryService() {
        if (this.javaDirectoryService == null) {
            this.javaDirectoryService = JavaDirectoryService.getInstance();
        }
        return this.javaDirectoryService;
    }

}
