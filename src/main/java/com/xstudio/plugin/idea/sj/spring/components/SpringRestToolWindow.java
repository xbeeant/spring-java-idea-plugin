package com.xstudio.plugin.idea.sj.spring.components;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.xstudio.plugin.idea.sj.spring.RequestPath;
import com.xstudio.plugin.idea.sj.spring.RequestPathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class SpringRestToolWindow implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        if (project.isInitialized()) {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            RestListForm restListForm = RequestPathUtil.getRestListForm(project);

            // 创建面板
            Content content = contentFactory.createContent(restListForm.getPanel(), "", false);
            toolWindow.getContentManager().addContent(content);
            JBList<RequestPath> jbList = restListForm.getJbList();
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
        }

    }
}
