package com.doggyzhang.plugin.translate.youdao;

import com.google.gson.annotations.*;

/**
 * 有道结构体
 * https://ai.youdao.com/DOCSIRMA/html/transapi/trans/api/plwbfy/index.html
 */
public class YouDaoTranslateResult {
    @SerializedName("query")
    String query;
    @SerializedName("translation")
    String translation;
    @SerializedName("type")
    String type;

}
