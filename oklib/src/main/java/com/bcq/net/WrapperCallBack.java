package com.bcq.net;


import android.text.TextUtils;

import com.bcq.net.api.OCallBack;
import com.bcq.net.wrapper.OkHelper;
import com.bcq.net.wrapper.OkUtil;
import com.bcq.net.wrapper.Wrapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public abstract class WrapperCallBack extends OCallBack<Wrapper> {//IOCallBack

    @Override
    public void onBefore(Request.Builder builder) {
        if (null != OkHelper.get().getHeadCacher()) { //添加header
            Map<String, String> hs = OkHelper.get().getHeadCacher().onAddHeader();
            if (null == hs || hs.isEmpty()) return;
            for (Map.Entry<String, String> en : hs.entrySet()) {
                builder.addHeader(en.getKey(), en.getValue());
            }
        }
    }

    @Override
    public Wrapper onParse(Response response) throws Exception {
        Wrapper wrapper = new Wrapper();
        wrapper.setCode(response.code());
        try {
            String string = response.body().string();
            OkUtil.i("Wrapper", "string = " + string);
            if (!TextUtils.isEmpty(string)) {
                JsonObject result = JsonParser.parseString(string).getAsJsonObject();
                if (null != result) {
                    wrapper.setCode(result.get("code").getAsInt());
                    wrapper.setMessage(result.get("msg").getAsString());
                    wrapper.setBody(result.get("data"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wrapper;
    }

    @Override
    public void onProgress(float progress, long total) {
    }

    @Override
    public void onAfter() {
    }

    @Override
    public abstract void onResult(Wrapper result);

    @Override
    public abstract void onError(int code, String msg);
}
