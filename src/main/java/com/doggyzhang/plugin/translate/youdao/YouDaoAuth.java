package com.doggyzhang.plugin.translate.youdao;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.*;

@Service(Service.Level.PROJECT)
@State(name = "YouDaoAuthKey")
@Storage(value = StoragePathMacros.WORKSPACE_FILE, roamingType = RoamingType.DISABLED)
public class YouDaoAuth implements PersistentStateComponent<YouDaoAuth.AuthKey>, IYouDaoAuth {

    public static YouDaoAuth getInstance(Project project) {
        return (YouDaoAuth) project.getService(IYouDaoAuth.class);
    }

    @Override
    public void setAuthKey(String authKey) {
        authKeyState.value = authKey;
    }

    @Override
    public String getLastAuthKey() {
        return authKeyState.value;
    }

    static class AuthKey {
        public String value;
    }

    private AuthKey authKeyState = new AuthKey();

    @Override
    public AuthKey getState() {
        return authKeyState;
    }

    @Override
    public void loadState(AuthKey state) {
        authKeyState = state;
    }
}
