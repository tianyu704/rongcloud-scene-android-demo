package cn.rong.combusis;

import android.app.Application;

import com.basis.BasisHelper;
import com.bcq.net.net.Page;
import com.bcq.net.wrapper.BaseProcessor;
import com.bcq.net.wrapper.OkHelper;
import com.bcq.net.wrapper.OkUtil;
import com.bcq.net.wrapper.Wrapper;
import com.bcq.net.wrapper.interfaces.IHeader;
import com.bcq.net.wrapper.interfaces.IPage;
import com.bcq.net.wrapper.interfaces.IParse;
import com.bcq.net.wrapper.interfaces.IResult;
import com.bcq.net.wrapper.interfaces.IWrap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kit.cache.GsonUtil;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;

public class ComApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initBasis();
    }

    private void initBasis() {
        BasisHelper.setDefaultPage(new Page() {

            @Override
            public int getFirstIndex() {
                return 1;
            }

            @Override
            public int geSize() {
                return 12;
            }

            @Override
            public String getKeyPage() {
                return "page";
            }

            @Override
            public String getKeySize() {
                return "size";
            }
        });
        OkHelper.get().setDefaultParser(new IParse() {
            @Override
            public IWrap parse(int httpcode, String json) throws Exception {
                Wrapper info = new Wrapper();
                info.setCode(httpcode);
                Logger.e("CommonApplication", "json = " + json);
                JsonElement result = JsonParser.parseString(json);
                if (result instanceof JsonObject) {
                    JsonObject resulObj = (JsonObject) result;
                    info.setCode(resulObj.get("code").getAsInt());
                    info.setMessage(resulObj.get("msg").getAsString());
                    JsonElement data = resulObj.get("data");
                    if (data.isJsonObject()) {
                        int total = ((JsonObject) data).get("total").getAsInt();
                        info.setPage(0, total);
                        JsonArray list = ((JsonObject) data).get("list").getAsJsonArray();
                        info.setBody(list);
                    } else {
                        info.setBody(resulObj.get("data"));
                    }

                    info.setPage(0, resulObj.get("page_count").getAsInt());
                }
                Logger.e("CommonApplication", "wrapper = " + GsonUtil.obj2Json(info));
                return info;
            }

            @Override
            public boolean ok(int code) {
                return code == 10000;
            }
        });
        OkHelper.get().setDefaultProcessor(new MyProcessor());
        OkHelper.get().setHeadCacher(new IHeader() {
            @Override
            public Map<String, String> onAddHeader() {
                Map map = new HashMap<String, String>();
                map.put("Authorization", AccountStore.INSTANCE.getAuthorization());
                return map;
            }

            @Override
            public void onCacheHeader(Headers headers) {

            }
        });
    }

    class MyProcessor<IR extends IResult<R, E>, R, E, T> extends BaseProcessor<IR, R, E, T> {
        @Override
        public IR processResult(IWrap wrap, Class<T> clazz) {
            if (null == clazz) {
                return (IR) new IResult.StatusResult(wrap.getCode(), wrap.getMessage());
            } else {
                OkUtil.e("processResult", "clazz:" + clazz.getSimpleName());
                IPage page = wrap.getPage();
                E extra = (E) page;
                R result = null;
                JsonElement element = wrap.getBody();
                if (null != element) {
                    result = (R) OkUtil.json2List(wrap.getBody(), clazz);
                }
                return (IR) new IResult.WrapResult<>(result, extra);
            }
        }
    }
}
