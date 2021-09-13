package cn.rong.combusis;

import android.app.Application;

import com.basis.BasisHelper;
import com.bcq.net.net.Page;
import com.bcq.net.wrapper.OkHelper;
import com.bcq.net.wrapper.Wrapper;
import com.bcq.net.wrapper.interfaces.IParse;
import com.bcq.net.wrapper.interfaces.IWrap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kit.cache.GsonUtil;
import com.kit.utils.Logger;

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
                    info.setBody(resulObj.get("data"));
                    info.setPage(resulObj.get("page").getAsInt(), resulObj.get("page_count").getAsInt());
                }
                Logger.e("CommonApplication", "wrapper = " + GsonUtil.obj2Json(info));
                return info;
            }

            @Override
            public boolean ok(int code) {
                return code == 10000;
            }
        });
    }
}
