package com.xstudio.plugin.idea.sj.spring.components;

import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.MappingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author xbeeant
 */
public class MappingWindow implements ToolWindowFactory {

    public static boolean containsWords(String inputString, String[] items) {
        boolean found = true;
        for (String item : items) {
            if (!inputString.contains(item.toLowerCase())) {
                found = false;
                break;
            }
        }
        return found;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                if (project.isInitialized()) {
                    ContentFactory contentFactory = ContentFactory.getInstance();
                    // 获取请求列表panel
                    MappingListForm mappingListForm = MappingHelper.getMappingListForm(project);

                    // 创建面板
                    Content content = contentFactory.createContent(mappingListForm.getPanel(), "", false);
                    toolWindow.getContentManager().addContent(content);
                    JBList<Mapping> jbList = mappingListForm.getJbList();
                    List<Mapping> mappings = MappingHelper.findAllMapping(project);

                    // 搜索框
                    mappingListForm.getRequestSearch().addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            String value = mappingListForm.getRequestSearch().getText();
                            List<Mapping> requests = MappingHelper.findAllMapping(project);
                            // 模糊搜索
                            requests.removeIf(item -> {
                                        String[] splitValues = value.split(" ");
                                        return !containsWords(item.getFullpath().toLowerCase(), splitValues);
                                    }
                            );

                            DefaultListModel<Mapping> listModel = MappingHelper.getListModel(requests);
                            jbList.setModel(listModel);
                        }
                    });

                    // 自定义list渲染器
                    jbList.setCellRenderer((ListCellRenderer<? super Mapping>) (list, value, index, isSelected, cellHasFocus) -> {
                        JLabel jLabel = new JLabel(value.getType() + " " + value.getPath());
                        if (cellHasFocus || isSelected) {
                            jLabel.setBackground(JBColor.LIGHT_GRAY);
                            jLabel.setOpaque(true);
                        }
                        return jLabel;
                    });

                    // 填充list
                    DefaultListModel<Mapping> listModel = MappingHelper.getListModel(mappings);
                    jbList.setModel(listModel);
                    // 选中监听器
                    // 点击请求 跳转到对应的方法中
                    jbList.addListSelectionListener(e -> {
                        if (e.getValueIsAdjusting()) {
                            JList<Mapping> item = (JList<Mapping>) e.getSource();
                            Mapping selectedValue = item.getSelectedValue();
                            if (null != selectedValue) {
                                NavigationUtil.activateFileWithPsiElement(selectedValue.getPsiMethod(), true);
                            }
                            item.clearSelection();
                        }
                    });
                }
            }
        });
    }
}
