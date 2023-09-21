package com.xstudio.plugin.idea.sj.translate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xstudio.plugin.idea.sj.util.RequestUtil;
import com.xstudio.plugin.idea.sj.util.entity.BiyingTranslateResponse;
import com.xstudio.plugin.idea.sj.util.entity.Translations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiyingTranslate {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<BiyingTranslateResponse>> typeReference = new TypeReference<>() {
    };

    public static String translate(String text) {
        Map<String, String> params = new HashMap<>(3);
        params.put("fromLang", "en");
        params.put("text", text);
        params.put("to", "zh-Hans");
        String result = text;
        try {
            String json = RequestUtil.post("https://cn.bing.com/ttranslatev3", params);
            List<BiyingTranslateResponse> responses = (List<BiyingTranslateResponse>) objectMapper.readValue(json, typeReference);
            BiyingTranslateResponse translate = responses.get(0);
            if (null != translate) {
                List<Translations> translations = translate.getTranslations();
                if (null != translations && !translations.isEmpty()) {
                    result = translations.get(0).getText();
                }
            }
        } catch (Exception e) {
            result = text;
        }

        return result;
    }
}
