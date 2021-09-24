package cn.rongcloud.voiceroom.pk;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.basis.ui.BaseActivity;
import com.bcq.net.OkApi;
import com.bcq.net.WrapperCallBack;
import com.bcq.net.wrapper.Wrapper;
import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;
import com.rongcloud.common.utils.UIKit;
import com.umeng.commonsdk.debug.I;

import java.util.HashMap;
import java.util.Map;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.sdk.event.wrapper.AbsPKHelper;
import cn.rongcloud.voiceroom.R;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.EventBus;
import cn.rong.combusis.sdk.VoiceRoomApi;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.pk.widget.PKProcessbar;

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

    private void initData() {
        getView(R.id.leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VoiceRoomApi.getApi().leaveRoom(null);
                synToService("");
            }
        });
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
                pkButton.setText(EventHelper.helper().getPKState() == AbsPKHelper.Type.PK_INVITE ? "取消PK" : "邀请PK");
            }
        });
        join();

        PKStateManager.get().init(voiceRoomBean.getRoomId(), new IPKState.VRStateListener() {
            @Override
            public void onPkStart() {

            }

            @Override
            public void onPkStop() {

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
