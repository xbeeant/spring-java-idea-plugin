package com.xstudio.plugin.idea.sj.getset;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.xstudio.plugin.idea.sj.getset.po.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "SpringJavaPersistentConfiguration", storages = {@Storage("spring-java-config.xml")})
public class TemplatePersistentConfiguration implements PersistentStateComponent<TemplatePersistentConfiguration> {
    private Template template = new Template();

    public Template getTemplate() {
        return template;
    }

    public static TemplatePersistentConfiguration getInstance() {
        return ApplicationManager.getApplication().getService(TemplatePersistentConfiguration.class);
    }

    @Override
    public @Nullable
    TemplatePersistentConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TemplatePersistentConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}