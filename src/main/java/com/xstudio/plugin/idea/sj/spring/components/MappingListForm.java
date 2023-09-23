package com.xstudio.plugin.idea.sj.spring.components;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.MappingHelper;
import com.xstudio.plugin.idea.sj.util.PluginNotifier;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingListForm {
    private JTextField requestSearch;
    private JScrollPane scrollPane;
    private JBList<Mapping> jbList;
    private JPanel panel;
    private JButton reloadBtn;
    private JCheckBox getCheckBox;
    private JCheckBox scheduleCheckBox;
    private JCheckBox postCheckBox;
    private JCheckBox putCheckBox;
    private JCheckBox deleteCheckBox;
    private JCheckBox requestCheckBox;

    public JButton getReloadBtn() {
        return reloadBtn;
    }

    public MappingListForm(Project project) {
        jbList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 有 1，2，3。1代表鼠标左键，3代表鼠标右键
                if (3 == e.getButton()) {
                    int index = jbList.locationToIndex(e.getPoint());
                    Mapping mapping = jbList.getModel().getElementAt(index);

                    // 获取系统剪贴板
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    // 封装文本内容
                    Transferable trans = new StringSelection(mapping.getPath());
                    // 把文本内容设置到系统剪贴板
                    clipboard.setContents(trans, null);

                    // 提示框
                    PluginNotifier.notify(project, "Copied", mapping.getPath());
                }
            }
        });
    }

    public JTextField getRequestSearch() {
        return requestSearch;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JBList<Mapping> getJbList() {
        return jbList;
    }

    public JPanel getPanel() {
        return panel;
    }

    public JCheckBox getGetCheckBox() {
        return getCheckBox;
    }

    public JCheckBox getScheduleCheckBox() {
        return scheduleCheckBox;
    }

    public JCheckBox getPostCheckBox() {
        return postCheckBox;
    }

    public JCheckBox getPutCheckBox() {
        return putCheckBox;
    }

    public JCheckBox getDeleteCheckBox() {
        return deleteCheckBox;
    }

    public JCheckBox getRequestCheckBox() {
        return requestCheckBox;
    }

    public enum CheckboxFilterEnum {
        GET,
        POST,
        PUT,
        DELETE,
        REQUEST,
        SCHEDULE,
    }





    public static class CheckboxFilter {
        private static final Map<CheckboxFilterEnum, Boolean> filters = new HashMap<>();

        static {
            filters.put(CheckboxFilterEnum.PUT, true);
            filters.put(CheckboxFilterEnum.POST, true);
            filters.put(CheckboxFilterEnum.GET, true);
            filters.put(CheckboxFilterEnum.DELETE, true);
            filters.put(CheckboxFilterEnum.REQUEST, true);
            filters.put(CheckboxFilterEnum.SCHEDULE, true);
        }

        public static Map<CheckboxFilterEnum, Boolean> getFilters() {
            return filters;
        }

        public static void setFilter(CheckboxFilterEnum type, Boolean value) {
            filters.put(type, value);
        }
    }
}
