package com.doggyzhang.plugin.bean;

import java.util.ArrayList;
import java.util.List;

public class LanguageFile {
    private String fileName;

    private List<MultiLanguageBean> languageBeans;

    public LanguageFile(String fileName, List<MultiLanguageBean> languageBeans) {
        this.fileName = fileName;
        if (languageBeans == null) {
            this.languageBeans = new ArrayList<>();
        } else {
            this.languageBeans = languageBeans;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public List<MultiLanguageBean> getLanguageBeans() {
        return languageBeans;
    }
}
