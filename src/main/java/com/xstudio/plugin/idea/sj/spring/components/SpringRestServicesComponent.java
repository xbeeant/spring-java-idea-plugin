package com.xstudio.plugin.idea.sj.spring.components;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.DisposeAwareRunnable;
import com.xstudio.plugin.idea.sj.spring.RequestPathUtil;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author xiaobiao
 * @version 2020/4/29
 */
public class SpringRestServicesComponent implements ProjectComponent {

    protected final Project project;

    public SpringRestServicesComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        // 创建右侧的tool window
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = instance.registerToolWindow("Request List", true, ToolWindowAnchor.RIGHT);
        toolWindow.setIcon(IconLoader.getIcon("/icons/plus.png"));
        // 初始化右侧的tool window
        runWhenInitialized(project, () -> {
            if (project.isDisposed()) return;
            initialToolWindow(toolWindow);
        });
    }

    private void runWhenInitialized(final Project project, final Runnable r) {

        if (project.isDisposed()) return;

        if (isNoBackgroundMode()) {
            r.run();
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(DisposeAwareRunnable.create(r, project));
            return;
        }

        invokeLater(project, r);
    }

    public void invokeLater(Project p, Runnable r) {
        invokeLater(p, ModalityState.defaultModalityState(), r);
    }

    public void invokeLater(final Project p, final ModalityState state, final Runnable r) {
        if (isNoBackgroundMode()) {
            r.run();
        } else {
            ApplicationManager.getApplication().invokeLater(DisposeAwareRunnable.create(r, p), state);
        }
    }

    private boolean isNoBackgroundMode() {
        return (ApplicationManager.getApplication().isUnitTestMode()
                || ApplicationManager.getApplication().isHeadlessEnvironment());
    }

    private void initialToolWindow(ToolWindow toolWindow) {
        Project project = this.project;
        RestListForm restListForm = RequestPathUtil.getRestListForm(project);
        JBList<RequestPath> jbList = restListForm.getJbList();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        List<RequestPath> requestPaths = RequestPathUtil.findAllRequestInProject(project);

        // 搜索框
        restListForm.getRequestSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String value = restListForm.getRequestSearch().getText();
                List<RequestPath> requests = RequestPathUtil.findAllRequestInProject(project);
                // 模糊搜索
                requests.removeIf(item -> !item.getPath().toLowerCase().contains(value.toLowerCase()));

                DefaultListModel<RequestPath> listModel = RequestPathUtil.getListModel(requests);
                jbList.setModel(listModel);
            }
        });

        // 自定义list渲染器
        jbList.setCellRenderer((ListCellRenderer) (list, value, index, isSelected, cellHasFocus) -> {
            RequestPath requestPath = (RequestPath) value;
            JLabel jLabel = new JLabel(requestPath.getMethod() + " " + requestPath.getPath());
            if (cellHasFocus || isSelected) {
                jLabel.setBackground(JBColor.LIGHT_GRAY);
                jLabel.setOpaque(true);
            }
            return jLabel;
        });
        // 填充list
        DefaultListModel<RequestPath> listModel = RequestPathUtil.getListModel(requestPaths);
        jbList.setModel(listModel);
        // 选中监听器
        // 点击请求 跳转到对应的方法中
        jbList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                JList<RequestPath> item = (JList<RequestPath>) e.getSource();
                RequestPath selectedValue = item.getSelectedValue();
                if (null != selectedValue) {
                    NavigationUtil.activateFileWithPsiElement(selectedValue.getPsiMethod(), true);
                }
                item.clearSelection();
            }
        });
        // 创建面板
        Content content = contentFactory.createContent(restListForm.getPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }


    @Override
    public void projectClosed() {
        RequestPathUtil.removeRestListForm(project);
    }

    @Override
    public void initComponent() {

    }
}
