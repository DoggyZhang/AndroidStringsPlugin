package com.doggyzhang.plugin.translate;

import com.intellij.openapi.project.*;

import java.util.*;

public interface ITranslate {

    /**
     * 输入授权码
     */
    void setAuthKey(Project project, String authKey);

    String getLastAuthKey(Project project);

    /**
     * 翻译
     *
     * @param inputList    要翻译的文案
     * @param fromLanguage 原语言
     * @param toLanguages  目标语言
     * @return <目标语言, <原翻译文案, 目标翻译文案>>
     */
    Map<Language, Map<String, String>> translate(
            List<String> inputList,
            Language fromLanguage,
            List<Language> toLanguages,
            ITranslateProgress progress
    );
}