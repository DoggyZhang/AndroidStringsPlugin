package com.doggyzhang.plugin.translate;

import org.apache.commons.lang3.*;

public enum Language {
    ZH("zh", "zh"),
    EN("en", "en"),
    AR("ar", "ar"),
    HI("hi", "hi"),
    TR("tr", "tr"),
    TH("th", "th"),
    VI("vi", "vi"),
    ID("id", "in"), //印尼语言码是"in"
    TL("tl", "tl"),
    PT("pt", "pt"),
    BN("bn", "bn"),
    UR("ur", "ur"),
    MS("ms", "ms"),
    ES("es", "es");

    /**
     * 对用string-**.xml
     */
    private String languageCode;

    private String stringsCode;

    Language(String languageCode, String stringsCode) {

        this.languageCode = languageCode;
        this.stringsCode = stringsCode;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public String getStringsCode() {
        return this.stringsCode;
    }

    public static Language getLanguageBy(String languageCode) {
        if (StringUtils.isEmpty(languageCode)) {
            return null;
        }
        for (Language value : Language.values()) {
            if (value.languageCode.equals(languageCode)) {
                return value;
            }
        }
        return null;
    }

    public static Language getLanguageFrom(String stringsCode) {
        if (StringUtils.isEmpty(stringsCode)) {
            return null;
        }
        for (Language value : Language.values()) {
            if (value.stringsCode.equals(stringsCode)) {
                return value;
            }
        }
        return null;
    }

    public static String getStringsCodeBy(String languageCode) {
        if (StringUtils.isEmpty(languageCode)) {
            return null;
        }
        for (Language value : Language.values()) {
            if (value.languageCode.equals(languageCode)) {
                return value.stringsCode;
            }
        }
        return null;
    }

}
