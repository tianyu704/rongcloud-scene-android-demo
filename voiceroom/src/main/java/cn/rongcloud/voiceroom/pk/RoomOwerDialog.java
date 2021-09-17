package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.basis.widget.BottomDialog;
import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.bcq.net.OkApi;
import com.bcq.net.WrapperCallBack;
import com.bcq.net.wrapper.Wrapper;
import com.bcq.refresh.XRecyclerView;
import com.kit.cache.GsonUtil;
import com.kit.utils.ImageLoader;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;
import com.rongcloud.common.utils.UIKit;

import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rongcloud.voiceroom.R;
import cn.rong.combusis.sdk.VoiceRoomApi;

/**
 * pk在线房主弹框
 */
public class RoomOwerDialog extends BottomDialog {
    public RoomOwerDialog(Activity activity, IResultBack resultBack) {
        super(activity);
        setContentView(R.layout.layout_owner_dialog, 60);
        initView();
        requestOwners();
    }

    private XRecyclerView rcyOwner;
    private IAdapte adapter;
    private IResultBack resultBack;

    private void initView() {
        rcyOwner = UIKit.getView(getContentView(), R.id.rcy_owner);
        rcyOwner.setLayoutManager(new LinearLayoutManager(mActivity));

        adapter = new RcySAdapter<VoiceRoomBean, RcyHolder>(mActivity, R.layout.layout_owner_item) {

            @Override
            public void convert(RcyHolder holder, VoiceRoomBean item, int position) {
                holder.setText(R.id.tv_name, item.getCreateUser().getUserName());
                ImageLoader.loadUrl(holder.getView(R.id.head),
                        item.getCreateUser().getPortrait(),
                        R.drawable.default_portrait,
                        ImageLoader.Size.SZ_200);
                holder.rootView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                        VoiceRoomApi.getApi().sendPKInvitation(item.getRoomId(), item.getCreateUser().getUserId(),
                                new IResultBack<Boolean>() {
                                    @Override
                                    public void onResult(Boolean aBoolean) {
                                        KToast.show(aBoolean ? "已邀请PK,等的对方接受" : "PK邀请失败");
                                        if (null != resultBack) resultBack.onResult(aBoolean);
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
        OkApi.get(VRApi.ONLINE_CREATER, null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                Logger.e(TAG, "requestOwners#onResult:" + GsonUtil.obj2Json(result));
                List<VoiceRoomBean> rooms = result.getList(VoiceRoomBean.class);
                adapter.setData(rooms, true);
                VoiceRoomProvider.provider().update(rooms);
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
