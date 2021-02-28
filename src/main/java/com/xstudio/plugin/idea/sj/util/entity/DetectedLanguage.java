package com.xstudio.plugin.idea.sj.util.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetectedLanguage {
    /**
     * language
     */
    @JsonProperty("language")
    private String language;
    /**
     * score
     */
    @JsonProperty("score")
    private Double score;

    public String getLanguage() {
        return language;
    }

    public Double getScore() {
        return score;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
