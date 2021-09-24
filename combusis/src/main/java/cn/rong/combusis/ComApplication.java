package cn.rong.combusis;

import android.app.Application;

import com.basis.BasisHelper;
import com.basis.net.oklib.net.Page;
import com.basis.net.oklib.wrapper.OkHelper;
import com.basis.net.oklib.wrapper.interfaces.IHeader;
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
}


