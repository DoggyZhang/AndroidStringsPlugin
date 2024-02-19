package com.doggyzhang.plugin.translate;

import org.apache.commons.lang3.*;

public enum Language {
    ZH("zh"),
    EN("en"),
    AR("ar"),
    HI("hi"),
    TR("tr"),
    TH("th"),
    VI("vi"),
    ID("id"),
    TL("tl"),
    PT("pt"),
    BN("bn"),
    UR("ur"),
    MS("ms"),
    ES("es");

    /**
     * 对用string-**.xml
     */
    private String languageCode;

    Language(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public static Language getLanguageBy(String languageCode){
        if(StringUtils.isEmpty(languageCode)){
            return null;
        }
        for (Language value : Language.values()) {
            if (value.languageCode.equals(languageCode)) {
                return value;
            }
        }
        return null;
    }

}
