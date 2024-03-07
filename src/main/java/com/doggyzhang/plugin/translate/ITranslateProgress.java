package com.doggyzhang.plugin.translate;

public interface ITranslateProgress {
    void onProgressUpdate(String progress);

    void onError(String msg);
}