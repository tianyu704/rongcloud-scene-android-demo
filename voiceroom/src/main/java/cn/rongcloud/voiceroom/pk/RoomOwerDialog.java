package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.basis.widget.BottomDialog;
import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.bcq.net.OkApi;
import com.bcq.net.WrapperCallBack;
import com.bcq.net.wrapper.Wrapper;
import com.bcq.refresh.IRefresh;
import com.bcq.refresh.XRecyclerView;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;
import com.rongcloud.common.utils.UIKit;

import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.sdk.VoiceRoomApi;

/**
 * pk在线房主弹框
 */
public class RoomOwerDialog extends BottomDialog {
    public RoomOwerDialog(Activity activity) {
        super(activity);
        setContentView(R.layout.layout_owner_dialog, 60);
        initView();
        requestOwners();
    }

    private XRecyclerView rcyOwner;
    private IAdapte adapter;
    private String selectId;
    private List<String> alterlys;

    private void initView() {
        rcyOwner = UIKit.getView(getContentView(), R.id.rcy_owner);
        rcyOwner.setLayoutManager(new LinearLayoutManager(mActivity));

        adapter = new RcySAdapter<VoiceRoomBean, RcyHolder>(mActivity, R.layout.layout_owner_item) {

            @Override
            public void convert(RcyHolder holder, VoiceRoomBean item, int position) {
                int res = TextUtils.isEmpty(selectId) ? R.string.invitate_pk :
                        selectId.equals(item.getUserId()) ? R.string.cancel_invitate_pk
                                : R.string.alterlay_invitate_pk;
                holder.setText(R.id.tv_name, item.getCreateUser().getUserName());
                holder.rootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectId = item.getUserId();
                        alterlys.add(selectId);
                        VoiceRoomApi.getApi().sendPKInvitation(item.getRoomId(), item.getUserId(), new IResultBack<Boolean>() {
                            @Override
                            public void onResult(Boolean aBoolean) {
                                KToast.show("PK邀请成功");
                            }
                        });
                    }
                });
            }
        };
        adapter.setRefreshView(rcyOwner);
        rcyOwner.enableRefresh(false);
        rcyOwner.enableRefresh(false);
    }

    private void requestOwners() {
        OkApi.get(VRApi.online_creater, null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                List<VoiceRoomBean> rooms = result.getList("rooms", VoiceRoomBean.class);
                adapter.setData(rooms, true);
            }

            @Override
            public void onAfter() {
                if (null != rcyOwner) {
                    rcyOwner.loadComplete();
                    rcyOwner.refreshComplete();
                }
            }
        });
    }

}
