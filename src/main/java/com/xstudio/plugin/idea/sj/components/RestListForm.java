package com.xstudio.plugin.idea.sj.components;

import com.intellij.ui.components.JBList;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;

public class RestListForm {
    private JTextField requestSearch;
    private JScrollPane scrollPane;
    private JBList<RequestPath> jbList;
    private JPanel panel;

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
