package com.xstudio.plugin.idea.sj.components;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.xstudio.plugin.idea.sj.RequestPathUtil;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
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
        initialToolWindow(toolWindow);
    }

    private void initialToolWindow(ToolWindow toolWindow) {
        Project project = this.project;
        RestListForm restListForm = RequestPathUtil.getRestListForm(project);
        JBList<RequestPath> jbList = restListForm.getJbList();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        List<RequestPath> requestPaths = RequestPathUtil.getRequestPath(project);

        // 搜索框
        restListForm.getRequestSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String value = restListForm.getRequestSearch().getText();
                List<RequestPath> requests = RequestPathUtil.getRequestPath(project);
                requests.removeIf(item -> !item.getPath().contains(value));

                DefaultListModel<RequestPath> listModel = RequestPathUtil.getListModel(requests);
                jbList.setModel(listModel);
            }
        });

        // 自定义list渲染器
        jbList.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                RequestPath requestPath = (RequestPath) value;
                JLabel jLabel = new JLabel(requestPath.getMethod() + " " + requestPath.getPath());
                if (cellHasFocus || isSelected) {
                    jLabel.setBackground(JBColor.LIGHT_GRAY);
                    jLabel.setOpaque(true);
                }
                return jLabel;
            }
        });
        // 填充list
        DefaultListModel<RequestPath> listModel = RequestPathUtil.getListModel(requestPaths);
        jbList.setModel(listModel);
        // 选中监听器
        // 点击请求 跳转到对应的方法中
        jbList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    JList<RequestPath> item = (JList<RequestPath>) e.getSource();
                    RequestPath selectedValue = item.getSelectedValue();
                    if (null != selectedValue) {
                        NavigationUtil.activateFileWithPsiElement(selectedValue.getPsiMethod(), true);
                    }
                    item.clearSelection();
                }
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
