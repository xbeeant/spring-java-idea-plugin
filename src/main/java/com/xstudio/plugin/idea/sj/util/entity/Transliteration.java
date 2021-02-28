package com.xstudio.plugin.idea.sj.util.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transliteration {
    /**
     * script
     */
    @JsonProperty("script")
    private String script;
    /**
     * text
     */
    @JsonProperty("text")
    private String text;

    public String getScript() {
        return script;
    }

    public String getText() {
        return text;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setText(String text) {
        this.text = text;
    }
}
