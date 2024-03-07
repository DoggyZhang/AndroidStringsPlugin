package com.doggyzhang.plugin.translate.youdao;

import com.doggyzhang.plugin.translate.*;
import com.doggyzhang.plugin.utils.*;
import com.google.common.reflect.*;
import com.intellij.credentialStore.*;
import com.intellij.ide.passwordSafe.*;
import com.intellij.openapi.project.*;
import org.apache.commons.lang3.*;
import org.apache.http.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.util.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

public class YouDaoTranslate implements ITranslate {

    private static final String YOUDAO_URL = "https://openapi.youdao.com/v2/api";

    private String authKey = null;

    private String APP_KEY = null;
    private String APP_SECRET = null;


    /**
     * 有道授权码格式
     * [APP_KEY]_[APP_SECRET]
     */
    @Override
    public void setAuthKey(Project project, String authKey) {
        this.authKey = authKey;
        if (!StringUtils.isEmpty(authKey)) {
            String[] keys = authKey.split("_");
            if (keys.length == 2) {
                APP_KEY = keys[0];
                APP_SECRET = keys[1];
            }
        }
        storeAuthKey(project, authKey);
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(
                CredentialAttributesKt.generateServiceName("YouDaoTranslate", key)
        );
    }

    @Override
    public String getLastAuthKey(Project project) {
        return YouDaoAuth.getInstance(project).getLastAuthKey();
//        CredentialAttributes attributes = createCredentialAttributes("AuthKey");
//        PasswordSafe passwordSafe = PasswordSafe.getInstance();
//
//        Credentials credentials = passwordSafe.get(attributes);
//        if (credentials != null) {
//            String password = credentials.getPasswordAsString();
//        }
//
//// or get password only
//        String password = passwordSafe.getPassword(attributes);
    }

    private void storeAuthKey(Project project, String authKey) {
        YouDaoAuth.getInstance(project).setAuthKey(authKey);
//        CredentialAttributes attributes = createCredentialAttributes("AuthKey");
//        Credentials credentials = new Credentials(username, password);
//        PasswordSafe.getInstance().set(attributes, credentials);
    }

    @Override
    public Map<Language, Map<String, String>> translate(
            List<String> inputList,
            Language fromLanguage,
            List<Language> toLanguages,
            ITranslateProgress progressListener
    ) {
        Map<Language, Map<String, String>> translateMap =
                new HashMap<>();
        if (StringUtils.isEmpty(authKey)) {
            return translateMap;
        }
        for (int ii = 0; ii < toLanguages.size(); ii++) {
            Language toLanguage = toLanguages.get(ii);
            //处理翻译文本过长
            progressListener.onProgressUpdate("翻译语言: " + toLanguage.getLanguageCode());
            int pageOffset = 5;
            Map<String, String> toLanguageMap = new HashMap<>();
            for (int i = 0; i < inputList.size(); i = i + pageOffset) {
                //每次翻译5条
                int fromI = Math.min(i, inputList.size() - 1);
                int toI = Math.min(i + pageOffset, inputList.size());
                if (fromI > toI) {
                    break;
                }
                progressListener.onProgressUpdate(
                        "翻译语言: " + toLanguage.getLanguageCode()
                                + "(" + fromI + "/" + inputList.size() + ")"
                );
                List<String> targetInputList = inputList.subList(fromI, toI);
                Map<String, String> translateResult;
                try {
                    translateResult = translateInner(
                            targetInputList,
                            getLanguageCodeBy(fromLanguage),
                            getLanguageCodeBy(toLanguage)
                    );
                } catch (Exception e) {
                    progressListener.onError(e.getMessage());
                    continue;
                }
                toLanguageMap.putAll(translateResult);
            }
            translateMap.put(toLanguage, toLanguageMap);
            /*
            有道会限制API的请求频率, 这里延时等待一下
            https://ai.youdao.com/DOCSIRMA/html/trans/api/wbfy/index.html
            {"msg":"Error Code:411","requestId":"0ca9fb52-484c-4b03-9f70-74e19ff5efde","errorCode":"411"}
             */
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return translateMap;
    }

    /**
     * https://ai.youdao.com/DOCSIRMA/html/transapi/trans/api/plwbfy/index.html
     * 支持语言
     */
    private String getLanguageCodeBy(Language language) {
        switch (language) {
            case ZH:
                return "zh-CHS";
            case EN:
                return "en";
            case HI:
                return "hi";
            case TR:
                return "tr";
            case TH:
                return "th";
            case VI:
                return "vi";
            case ID:
                return "id";
            case TL:
                return "tl";
            case PT:
                return "pt";
            case BN:
                return "bn";
            case UR:
                return "ur";
            case MS:
                return "ms";
            case ES:
                return "es";
            default:
                return "";
        }
    }

    private Map<String, String> translateInner(
            List<String> inputList,
            String fromLanguage,
            String toLanguage
    ) throws Exception {
        if (inputList == null || inputList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        String[] qArray = new String[inputList.size()];
        inputList.toArray(qArray);
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("from", fromLanguage); //zh-CHS
        params.put("to", toLanguage); //en
        params.put("signType", "v3");
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        params.put("curtime", curtime);
        String signStr = APP_KEY + truncate(qArray) + salt + curtime + APP_SECRET;
        String sign = getDigest(signStr);
        params.put("appKey", APP_KEY);
        params.put("salt", salt);
        params.put("sign", sign);
        params.put("detectFilter", "false");
        //params.put("vocabId", "您的用户词表ID");
        /** 处理结果 */
        try {
            String json = requestForHttp(YOUDAO_URL, params, qArray);
            System.out.println(json);
            Object o = GsonUtil.fromJson(json, YouDaoTranslateRes.class);
            if (o != null) {
                YouDaoTranslateRes res = (YouDaoTranslateRes) o;
                Map<String, String> translateMap = new HashMap<>();
                for (String input : inputList) {
                    String translate = res.findTranslate(input);
                    if (StringUtils.isEmpty(translate)) {
                        continue;
                    }
                    translateMap.put(input, translate);
                }
                return translateMap;
            } else {
                throw new RuntimeException(json);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private String requestForHttp(String url, Map<String, String> params, String[] qArray) throws IOException {
        /** 创建HttpClient */
        CloseableHttpClient httpClient = HttpClients.createDefault();

        /** httpPost */
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> en = it.next();
            String key = en.getKey();
            String value = en.getValue();
            paramsList.add(new BasicNameValuePair(key, value));
        }
        for (int i = 0; i < qArray.length; i++) {
            paramsList.add(new BasicNameValuePair("q", qArray[i]));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(paramsList, "UTF-8"));
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        String json;
        try {
            Header[] contentType = httpResponse.getHeaders("Content-Type");
            System.out.println("Content-Type:" + contentType[0].getValue());
            /** 响应不是音频流，直接显示结果 */
            HttpEntity httpEntity = httpResponse.getEntity();
            json = EntityUtils.toString(httpEntity, "UTF-8");
            EntityUtils.consume(httpEntity);
        } finally {
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                System.out.println("## release resouce error ##" + e);
            }
        }
        return json;
    }

    /**
     * 生成加密字段
     */
    private String getDigest(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest mdInst = MessageDigest.getInstance("SHA-256");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String truncate(String[] qArray) {
        if (qArray == null) {
            return null;
        }
        String batchQStr = String.join("", qArray);
        int len = batchQStr.length();
        return len <= 20 ? batchQStr : (batchQStr.substring(0, 10) + len + batchQStr.substring(len - 10, len));
    }
}
