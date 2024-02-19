package com.doggyzhang.plugin.bean;

import java.io.Serializable;

public class ElementBean implements Serializable {
    private String key;
    private String value;

    private boolean translateError = false;

    public ElementBean() {
    }

    public ElementBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public ElementBean(String key, String value, boolean translateError) {
        this.key = key;
        this.value = value;
        this.translateError = translateError;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTranslateError(boolean error) {
        this.translateError = error;
    }

    public boolean isTranslateError() {
        return this.translateError;
    }
}
