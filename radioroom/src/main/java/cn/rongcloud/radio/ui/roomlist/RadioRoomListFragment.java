package cn.rongcloud.radio.ui.roomlist;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.common.ui.dialog.ConfirmDialog;
import cn.rong.combusis.common.ui.dialog.InputPasswordDialog;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.roomlist.AbsRoomListFragment;
import cn.rongcloud.radio.ui.room.RadioRoomActivity;
import io.rong.imkit.picture.tools.ToastUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomListFragment extends AbsRoomListFragment {

    private ConfirmDialog confirmDialog;

    public static Fragment getInstance() {
        return new RadioRoomListFragment();
    }

    private InputPasswordDialog inputPasswordDialog;

    @Override
    public void clickItem(VoiceRoomBean item, int position , boolean isCreate, List<VoiceRoomBean> voiceRoomBeans) {
        if (TextUtils.equals(item.getUserId(), AccountStore.INSTANCE.getUserId())) {
            ArrayList list = new ArrayList();
            list.add(item.getRoomId());
            RadioRoomActivity.startActivity(getActivity(), list, 0);
        } else if (item.isPrivate()) {
            inputPasswordDialog = new InputPasswordDialog(requireContext(), false, () -> null, s -> {
                if (TextUtils.isEmpty(s)) {
                    return null;
                }
                if (s.length() < 4) {
                    ToastUtils.s(requireContext(), requireContext().getString(cn.rong.combusis.R.string.text_please_input_four_number));
                    return null;
                }
                if (TextUtils.equals(s, item.getPassword())) {
                    inputPasswordDialog.dismiss();
                    ArrayList list = new ArrayList();
                    list.add(item.getRoomId());
                    RadioRoomActivity.startActivity(getActivity(), list, 0);
                } else {
                    showToast("密码错误");
                }
                return null;
            });
            inputPasswordDialog.show();
        } else {
            ArrayList<String> list = new ArrayList<>();
            for (VoiceRoomBean voiceRoomBean : voiceRoomBeans) {
                if (!voiceRoomBean.getCreateUserId().equals(AccountStore.INSTANCE.getUserId())&&!voiceRoomBean.isPrivate()) {
                    //过滤掉上锁的房间和自己创建的房间
                    list.add(voiceRoomBean.getRoomId());
                }
            }
            RadioRoomActivity.startActivity(getActivity(), list, position);
        }
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.RADIO_ROOM;
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }

    @Override
    public void initListener() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUserRoom();
    }

    private void checkUserRoom() {
        Map<String, Object> params = new HashMap<>(2);
        OkApi.get(VRApi.USER_ROOM_CHECK,params,new WrapperCallBack(){

            @Override
            public void onResult(Wrapper result) {
                if (result.ok()){
                    VoiceRoomBean voiceRoomBean = result.get(VoiceRoomBean.class);
                    if (voiceRoomBean!=null){
                        //说明已经在房间内了，那么给弹窗
                        confirmDialog = new ConfirmDialog(requireActivity(), "您有正在直播的房间\n是否返回？", true,
                                "确定", "取消", new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                confirmDialog.dismiss();
                                return null;
                            }
                        }, new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                ArrayList<String> list = new ArrayList<>();
                                list.add(voiceRoomBean.getRoomId());
                                RadioRoomActivity.startActivity(getActivity(), list, 0);
                                return null;
                            }
                        });
                        confirmDialog.show();
                    }
                }
            }
        });
    }
}
