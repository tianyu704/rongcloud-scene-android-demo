package cn.rong.combusis.provider.voiceroom;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bcq.net.OkApi;
import com.kit.cache.GsonUtil;
import com.rongcloud.common.net.ApiConstant;
import com.kit.wapper.IResultBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bcq.net.WrapperCallBack;
import com.bcq.net.wrapper.Wrapper;

import cn.rong.combusis.provider.wrapper.AbsProvider;
import cn.rong.combusis.provider.wrapper.IListProvider;

public class VoiceRoomProvider extends AbsProvider<VoiceRoomBean> implements IListProvider<VoiceRoomBean> {
    private final static String API_ROOM = ApiConstant.INSTANCE.getBASE_URL() + "/mic/room/";
    private final static String API_ROOMS = ApiConstant.INSTANCE.getBASE_URL() + "/mic/room/list";
    private final static VoiceRoomProvider _provider = new VoiceRoomProvider();
    private List<String> bgImages = new ArrayList<>();

    private VoiceRoomProvider() {
        super(-1);
    }

    public static VoiceRoomProvider provider() {
        return _provider;
    }

    @Override
    public void provideFromService(@NonNull List<String> ids, @Nullable IResultBack<List<VoiceRoomBean>> resultBack) {
        if (null == ids || ids.isEmpty()) {
            if (null != resultBack) resultBack.onResult(new ArrayList<>());
            return;
        }
        String roomId = ids.get(0);
        OkApi.get(API_ROOM + roomId, null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                Log.e(TAG, GsonUtil.obj2Json(result));
                List<VoiceRoomBean> rooms = result.getList(VoiceRoomBean.class);
                if (null != resultBack) resultBack.onResult(rooms);

            }

            @Override
            public void onError(int code, String msg) {
                if (null != resultBack) resultBack.onResult(null);
            }
        });
    }

    public List<String> getImages() {
        return new ArrayList<>(bgImages);
    }

    @Override
    public void loadPage(int page, IResultBack<List<VoiceRoomBean>> resultBack) {
        if (page < 1) page = 1;
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", PAGE_SIZE);
        params.put("type", 1);//1 聊天室(默认) 2 电台
        OkApi.get(API_ROOMS, null, new WrapperCallBack() {
            @Override
            public void onError(int code, String msg) {
                if (null != resultBack) resultBack.onResult(null);
            }

            @Override
            public void onResult(Wrapper wrapper) {
                List<VoiceRoomBean> rooms = wrapper.getList("rooms", VoiceRoomBean.class);
                List<String> images = wrapper.getList("images", String.class);
                if (null != images && !images.isEmpty()) {
                    bgImages.clear();
                    bgImages.addAll(images);
                }
                Log.e(TAG, "provideFromService: size = " + (null == rooms ? 0 : rooms.size()));
                updateCache(rooms);
                if (null != resultBack) resultBack.onResult(rooms);
            }
        });
    }
}
