package com.doggyzhang.plugin.bean;

public class MultiLanguageBean {
    private String name;
    private String value;
    private String languageCode;
    private String language;

    private boolean translateError = false;

    public MultiLanguageBean() {
    }

    public MultiLanguageBean(String languageCode) {
        this.languageCode = languageCode;
        this.value = languageCode;
    }

    public MultiLanguageBean(MultiLanguageBean other) {
        this.name = other.name;
        this.value = other.value;
        this.languageCode = other.languageCode;
        this.language = other.language;
        this.translateError = other.translateError;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTranslateError(boolean error) {
        this.translateError = error;
    }

    public boolean isTranslateError() {
        return this.translateError;
    }

    @Override
    public String toString() {
        return "MultiLanguageBean{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", languageCode='" + languageCode + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
