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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private String formatLabel(String method, String uri) {
        return String.format("%-10s%s", method, uri);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {

            private void fillingJBList(JBList<Mapping> jbList, List<Mapping> mappings, String value, Map<MappingListForm.CheckboxFilterEnum, Boolean> checkboxFilters) {
                List<Mapping> requests = new ArrayList<>(mappings);
                // 模糊搜索
                requests.removeIf(item -> {
                            for (Map.Entry<MappingListForm.CheckboxFilterEnum, Boolean> checkboxFilter : checkboxFilters.entrySet()) {
                                if (Boolean.FALSE.equals(checkboxFilter.getValue()) && item.getType().equalsIgnoreCase(checkboxFilter.getKey().name())) {
                                    return true;
                                }
                            }

                            if (StringUtils.isNotEmpty(value)) {
                                String[] splitValues = value.split(" ");
                                return !containsWords(item.getFullpath().toLowerCase(), splitValues);
                            }
                            return false;
                        }
                );

                DefaultListModel<Mapping> listModel = MappingHelper.getListModel(requests);
                jbList.setModel(listModel);
            }

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
                            fillingJBList(jbList, mappings, value, MappingListForm.CheckboxFilter.getFilters());
                        }
                    });

                    // CHECK BOX
                    mappingListForm.getDeleteCheckBox().addChangeListener(e -> {
                        AbstractButton abstractButton = (AbstractButton) e.getSource();
                        ButtonModel buttonModel = abstractButton.getModel();
                        boolean selected = buttonModel.isSelected();

                        MappingListForm.CheckboxFilter.setFilter(MappingListForm.CheckboxFilterEnum.DELETE, selected);
                        fillingJBList(jbList, mappings, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());
                    });

                    mappingListForm.getGetCheckBox().addChangeListener(e -> {
                        AbstractButton abstractButton = (AbstractButton) e.getSource();
                        ButtonModel buttonModel = abstractButton.getModel();
                        boolean selected = buttonModel.isSelected();

                        MappingListForm.CheckboxFilter.setFilter(MappingListForm.CheckboxFilterEnum.GET, selected);
                        fillingJBList(jbList, mappings, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());
                    });

                    mappingListForm.getPostCheckBox().addChangeListener(e -> {
                        AbstractButton abstractButton = (AbstractButton) e.getSource();
                        ButtonModel buttonModel = abstractButton.getModel();
                        boolean selected = buttonModel.isSelected();

                        MappingListForm.CheckboxFilter.setFilter(MappingListForm.CheckboxFilterEnum.POST, selected);
                        fillingJBList(jbList, mappings, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());

                    });

                    mappingListForm.getPutCheckBox().addChangeListener(e -> {
                        AbstractButton abstractButton = (AbstractButton) e.getSource();
                        ButtonModel buttonModel = abstractButton.getModel();
                        boolean selected = buttonModel.isSelected();

                        MappingListForm.CheckboxFilter.setFilter(MappingListForm.CheckboxFilterEnum.PUT, selected);
                        fillingJBList(jbList, mappings, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());

                    });

                    mappingListForm.getRequestCheckBox().addChangeListener(e -> {
                        AbstractButton abstractButton = (AbstractButton) e.getSource();
                        ButtonModel buttonModel = abstractButton.getModel();
                        boolean selected = buttonModel.isSelected();

                        MappingListForm.CheckboxFilter.setFilter(MappingListForm.CheckboxFilterEnum.REQUEST, selected);
                        fillingJBList(jbList, mappings, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());

                    });

                    mappingListForm.getScheduleCheckBox().addChangeListener(e -> {
                        AbstractButton abstractButton = (AbstractButton) e.getSource();
                        ButtonModel buttonModel = abstractButton.getModel();
                        boolean selected = buttonModel.isSelected();

                        MappingListForm.CheckboxFilter.setFilter(MappingListForm.CheckboxFilterEnum.SCHEDULE, selected);
                        fillingJBList(jbList, mappings, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());

                    });

                    // reload btn
                    mappingListForm.getReloadBtn().addActionListener(e -> {
                        List<Mapping> allMapping = MappingHelper.findAllMapping(project);
                        fillingJBList(jbList, allMapping, mappingListForm.getRequestSearch().getText(), MappingListForm.CheckboxFilter.getFilters());
                    });

                    // 自定义list渲染器
                    jbList.setCellRenderer((ListCellRenderer<? super Mapping>) (list, value, index, isSelected, cellHasFocus) -> {
                        JLabel jLabel = new JLabel(formatLabel(value.getType(), value.getPath()));
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
