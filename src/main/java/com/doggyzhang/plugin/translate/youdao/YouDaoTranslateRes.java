package com.doggyzhang.plugin.translate.youdao;

import com.google.gson.annotations.*;
import org.apache.commons.lang3.*;

import java.util.*;

/**
 * 有道结构体
 * https://ai.youdao.com/DOCSIRMA/html/transapi/trans/api/plwbfy/index.html
 */
public class YouDaoTranslateRes {
    @SerializedName("errorCode")
    int errorCode = 0;
    @SerializedName("l")
    String l;
    @SerializedName("translateResults")
    List<YouDaoTranslateResult> translateResults;

    public String findTranslate(String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }
        if (translateResults == null || translateResults.size() == 0) {
            return "";
        }
        for (YouDaoTranslateResult translateResult : translateResults) {
            if (translateResult.query.equals(input)) {
                return translateResult.translation;
            }
        }
        return "";
    }
}
