package cn.rongcloud.voiceroom.pk;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.basis.ui.BaseActivity;
import com.bcq.net.OkApi;
import com.bcq.net.WrapperCallBack;
import com.bcq.net.wrapper.Wrapper;
import com.kit.cache.GsonUtil;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;
import com.rongcloud.common.utils.UIKit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.event.EventHelper;
import cn.rongcloud.voiceroom.event.listener.RoomListener;
import cn.rongcloud.voiceroom.model.EventBus;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rong.combusis.sdk.VoiceRoomApi;

public class TestPkActivity extends BaseActivity {
    @Override
    public int setLayoutId() {
        return R.layout.activity_test_pk;
    }

    String V_TAG = "voice_room";
    String PK_TAG = "voice_pk";
    private VoiceRoomBean voiceRoomBean;

    @Override
    public void init() {
        getWrapBar().setTitle(R.string.rc_location_title).setBackHide(true).work();
        String json = getIntent().getStringExtra(UIKit.KEY_BASE);
        voiceRoomBean = GsonUtil.json2Obj(json, VoiceRoomBean.class);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        VoomFragment current = new VoomFragment();
        transaction.add(R.id.container, current, V_TAG);
        transaction.commitAllowingStateLoss();
        initData();
    }

    private void initData() {
        getView(R.id.leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VoiceRoomApi.getApi().leaveRoom(null);
                synToService("");
            }
        });
        EventHelper.helper().regeister(voiceRoomBean.getRoomId());
        EventHelper.helper().addRoomListener(new RoomListener() {
            @Override
            public void onRoomInfo(@NonNull RCVoiceRoomInfo roomInfo) {
                Logger.e(TAG, "roomInfo = " + GsonUtil.obj2Json(roomInfo));
            }

            @Override
            public void onSeatList(@NonNull List<RCVoiceSeatInfo> seatInfos) {
                Logger.e(TAG, "seatInfos = " + GsonUtil.obj2Json(seatInfos));
            }

            @Override
            public void onNotify(String code, String content) {
            }

            @Override
            public void onOnLineUserIds(@Nullable List<String> userIds) {

            }
        });
        VoiceRoomApi.getApi().joinRoom(voiceRoomBean.getRoomId(), new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean aBoolean) {
                Log.e(TAG, "加入房间:" + aBoolean);
                synToService(voiceRoomBean.getRoomId());
                VoiceRoomApi.getApi().enterSeat(0, null);
            }
        });
//        RCVoiceRoomInfo roomInfo = VoiceRoomApi.getApi().getRoomInfo();
//        roomInfo.setSeatCount(8);
//        roomInfo.setRoomName(voiceRoomBean.getRoomName());
//        roomInfo.setMuteAll(false);
//        roomInfo.setLockAll(false);
//        VoiceRoomApi.getApi().createAndJoin(voiceRoomBean.getRoomId(), roomInfo, new IResultBack<Boolean>() {
//            @Override
//            public void onResult(Boolean aBoolean) {
//                Log.e(TAG, "加入房间:" + aBoolean);
//                synToService(voiceRoomBean.getRoomId());
//                VoiceRoomApi.getApi().enterSeat(1, null);
//            }
//        });
        EventBus.get().on(EventBus.TAG.PK_GOING, new EventBus.EventCallback() {
            @Override
            public void onEvent(Object args) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                PKFragment current = new PKFragment();
                transaction.replace(R.id.container, current, PK_TAG);
            }
        });

        EventBus.get().on(EventBus.TAG.PK_END, new EventBus.EventCallback() {
            @Override
            public void onEvent(Object args) {
                Fragment f = getSupportFragmentManager().findFragmentByTag(V_TAG);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, f, V_TAG);
                transaction.commitAllowingStateLoss();
            }
        });
    }

    private void synToService(String roomId) {
        //add 进房间标识
        Map<String, Object> params = new HashMap<>(2);
        params.put("roomId", roomId);
        OkApi.get(VRApi.USER_ROOM_CHANGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (TextUtils.isEmpty(roomId)){
                    onBackCode();
                }
            }
        });
    }
}
