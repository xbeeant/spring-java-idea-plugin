package com.xstudio.plugin.idea.sj.components;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.xstudio.plugin.idea.sj.RequestPathUtil;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class RestListForm {
    private JTextField requestSearch;
    private JScrollPane scrollPane;
    private JBList<RequestPath> jbList;
    private JPanel panel;
    private JButton reloadBtn;

    public RestListForm(Project project) {
        reloadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RequestPath> requestPath = RequestPathUtil.getRequestPath(project);
                RestListForm restListForm = RequestPathUtil.getRestListForm(project);
                restListForm.getJbList().setModel(RequestPathUtil.getListModel(requestPath));
            }
        });
    }

    public JTextField getRequestSearch() {
        return requestSearch;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JBList<RequestPath> getJbList() {
        return jbList;
    }

    public JPanel getPanel() {
        return panel;
    }
}
