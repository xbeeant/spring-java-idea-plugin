package com.xstudio.plugin.idea.sj.getset;

import com.xstudio.plugin.idea.sj.getset.po.Template;

import javax.swing.*;
import java.util.Objects;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class TemplateSetting extends JDialog {
    private JTextArea setterTemplate;
    private JTextArea getterTemplate;
    private JPanel mainPanel;
    private JTextArea templateDescription;

    private final Template template;

    public TemplateSetting() {
        TemplatePersistentConfiguration instance = TemplatePersistentConfiguration.getInstance();
        assert instance != null;
        template = instance.getTemplate();
        if (null != template) {
            setterTemplate.setText(template.getSetter());
            getterTemplate.setText(template.getGetter());
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean isModified() {
        boolean modified = !getterTemplate.getText().equals(template.getGetter());
        modified |= !setterTemplate.getText().equals(template.getSetter());
        return modified;
    }

    public void apply() {
        template.setSetter(setterTemplate.getText());
        template.setGetter(getterTemplate.getText());

        Objects.requireNonNull(TemplatePersistentConfiguration.getInstance()).setTemplate(template);
    }
}
