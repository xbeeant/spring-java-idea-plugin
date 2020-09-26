package com.xstudio.plugin.idea.sj.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class TemplateConfigurable implements SearchableConfigurable {
    private TemplateSetting templateSetting;
    @Override
    public @NotNull String getId() {
        return "Xstudio.SpringJava";
    }

    @Override
    public String getDisplayName() {
        return "Getter Setter Template";
    }

    @Override
    public JComponent createComponent() {
        templateSetting = new TemplateSetting();
        return templateSetting.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return templateSetting.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        templateSetting.apply();
    }
}
