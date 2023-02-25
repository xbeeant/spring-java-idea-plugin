package com.xstudio.plugin.idea.sj.spring.components;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.MappingHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MappingListForm {
    private JTextField requestSearch;
    private JScrollPane scrollPane;
    private JBList<Mapping> jbList;
    private JPanel panel;
    private JButton reloadBtn;

    public MappingListForm(Project project) {
        reloadBtn.addActionListener(e -> {

            List<Mapping> mappings = MappingHelper.findAllMapping(project);

            MappingListForm mappingListForm = MappingHelper.getMappingListForm(project);
            mappingListForm.getJbList().setModel(MappingHelper.getListModel(mappings));
        });
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
                    NotificationGroup balloonNotifications = new NotificationGroup("RequestList", NotificationDisplayType.TOOL_WINDOW, false);
                    Notification notification = balloonNotifications.createNotification("Copyed", mapping.getPath(),
                            NotificationType.INFORMATION, (notification1, hyperlinkEvent) -> {
                            });
                    Notifications.Bus.notify(notification);
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
}
