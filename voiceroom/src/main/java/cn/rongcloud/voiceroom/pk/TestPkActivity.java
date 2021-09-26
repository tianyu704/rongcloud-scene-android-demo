package cn.rongcloud.voiceroom.pk;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.basis.ui.BaseActivity;
import com.kit.UIKit;
import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;

import java.util.HashMap;
import java.util.Map;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.sdk.VoiceRoomApi;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.sdk.event.wrapper.AbsPKHelper;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.pk.widget.PKView;

public class TestPkActivity extends BaseActivity {
    @Override
    public int setLayoutId() {
        return R.layout.activity_test_pk;
    }

    private VoiceRoomBean voiceRoomBean;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PKStateManager.get().unInit();
    }

    @Override
    public void init() {
        getWrapBar().setTitle(R.string.rc_location_title).setBackHide(true).work();
        String json = getIntent().getStringExtra(UIKit.KEY_BASE);
        voiceRoomBean = GsonUtil.json2Obj(json, VoiceRoomBean.class);
        initData();
    }

    TextView pkButton;
    PKView pkVew;
    View voice_room;

    private void initData() {
        voice_room = getView(R.id.voice_room);
        pkVew = getView(R.id.pk_view);
        pkButton = getView(R.id.send_pk);
        pkButton.setText(EventHelper.helper().getPKState() == AbsPKHelper.Type.PK_INVITE ? "取消PK" : "邀请PK");
        pkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AbsPKHelper.Type state = EventHelper.helper().getPKState();
                if (AbsPKHelper.Type.PK_GOING == state) {
                    KToast.show("当前正在进行PK");
                    return;
                }
                Logger.e(TAG, "state = " + state);
                if (state == AbsPKHelper.Type.PK_INVITE) {
                    PKStateManager.get().cancelPkInvitation(activity, new IResultBack<Boolean>() {
                        @Override
                        public void onResult(Boolean aBoolean) {
                            if (aBoolean) pkButton.setText("邀请PK");
                        }
                    });
                } else {
                    PKStateManager.get().sendPkInvitation(activity, new IResultBack<Boolean>() {
                        @Override
                        public void onResult(Boolean aBoolean) {
                            if (aBoolean) pkButton.setText("取消PK");
                        }
                    });
                }
            }
        });
        join();
        PKStateManager.get().init(voiceRoomBean.getRoomId(), pkVew, new IPKState.VRStateListener() {
            @Override
            public void onPkStart() {
                PKStateManager.get().enterPkWithAnimation(voice_room, pkVew, 200);
            }

            @Override
            public void onPkStop() {
                PKStateManager.get().quitPkWithAnimation(pkVew, voice_room, 200);
            }
        });
        getView(R.id.leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leave();
            }
        });
    }

    private void join() {
        RCVoiceRoomInfo roomInfo = VoiceRoomApi.getApi().getRoomInfo();
        roomInfo.setSeatCount(8);
        roomInfo.setRoomName(voiceRoomBean.getRoomName());
        roomInfo.setMuteAll(false);
        roomInfo.setLockAll(false);
        VoiceRoomApi.getApi().createAndJoin(voiceRoomBean.getRoomId(), roomInfo, new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean aBoolean) {
                Log.e(TAG, "加入房间:" + aBoolean);
                synToService(voiceRoomBean.getRoomId());
                VoiceRoomApi.getApi().enterSeat(1, null);
            }
        });
    }

    private void leave() {
        VoiceRoomApi.getApi().leaveRoom(new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean aBoolean) {
                Log.e(TAG, "加入房间:" + aBoolean);
                synToService("");
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
                if (TextUtils.isEmpty(roomId)) {
                    onBackCode();
                }
            }
        });
    }
}
