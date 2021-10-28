package cn.rong.combusis.ui.roomlist;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.basis.adapter.interfaces.IAdapte;
import com.basis.adapter.recycle.RcyHolder;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.basis.ui.ListFragment;
import com.bcq.refresh.XRecyclerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cn.rong.combusis.R;
import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.common.ui.dialog.ConfirmDialog;
import cn.rong.combusis.intent.IntentWrap;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.OnItemClickRoomListListener;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @author gyn
 * @date 2021/9/15
 */
public abstract class AbsRoomListFragment extends ListFragment<VoiceRoomBean, VoiceRoomBean, RcyHolder> implements OnItemClickRoomListListener<VoiceRoomBean>, CreateRoomDialog.CreateRoomCallBack {

    private RoomListAdapter mAdapter;
    private XRecyclerView mRoomList;
    private CreateRoomDialog mCreateRoomDialog;
    private ConfirmDialog confirmDialog;
    private ActivityResultLauncher mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result != null && result.getData() != null && result.getData().getData() != null && mCreateRoomDialog != null) {
            mCreateRoomDialog.setCoverUri(result.getData().getData());
        }
    });

    @Override
    public IAdapte onSetAdapter() {
        mAdapter = new RoomListAdapter(getContext(), R.layout.item_room);
        mAdapter.setOnItemClickListener(this);
        return mAdapter;
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_room_list;
    }

    @Override
    public void initView() {
        mRoomList = (XRecyclerView) getView(R.id.xrv_room);
        getView(R.id.iv_create_room).setOnClickListener(v -> {
            createRoom();
        });
        checkUserRoom();
    }


    @Override
    public void onResume() {
        super.onResume();
        loadRoomList(true);
        //请求判断当时是否在房间内，如果弹窗不显示的话
    }

    public abstract RoomType getRoomType();

    @Override
    public void onCustomerRequestAgain(boolean refresh) {
        super.onCustomerRequestAgain(refresh);
        loadRoomList(refresh);
    }

    /**
     * 请求房间列表数据
     *
     * @param isRefresh 是否是刷新，否则是加载更多
     */
    private void loadRoomList(boolean isRefresh) {
        if (isRefresh) {
            mRoomList.setNoMore(false);
        }
        VoiceRoomProvider.provider().loadPage(isRefresh, getRoomType(), voiceRoomBeans -> {
            refresh(voiceRoomBeans, isRefresh);
            if (VoiceRoomProvider.provider().getPage() <= 2) {
                mRoomList.refreshComplete();
            } else {
                mRoomList.loadComplete();
            }

            if (voiceRoomBeans != null && !voiceRoomBeans.isEmpty()) {
            } else {
                mRoomList.setNoMore(true);
            }
        });
    }

    @Override
    public void onCreateSuccess(VoiceRoomBean voiceRoomBean) {
        mCreateRoomDialog.dismiss();
        mAdapter.getData().add(0, voiceRoomBean);
        mAdapter.notifyItemInserted(0);
        clickItem(voiceRoomBean, 0, true, Arrays.asList(voiceRoomBean));
    }

    @Override
    public void onCreateExist(VoiceRoomBean voiceRoomBean) {
        mCreateRoomDialog.dismiss();
        new ConfirmDialog(requireContext(), getString(R.string.text_you_have_created_room), true, "确定", "取消", () -> null, () -> {
            jumpRoom(voiceRoomBean);
            return null;
        }).show();
    }

    private void createRoom() {
        mCreateRoomDialog = new CreateRoomDialog(requireActivity(), mLauncher, getRoomType(), this);
        mCreateRoomDialog.show();
    }

    /**
     * 检查用户之前是否在某个房间内
     */
    private void checkUserRoom() {
        Map<String, Object> params = new HashMap<>(2);
        OkApi.get(VRApi.USER_ROOM_CHECK, params, new WrapperCallBack() {

            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    VoiceRoomBean voiceRoomBean = result.get(VoiceRoomBean.class);
                    if (voiceRoomBean != null) {
                        //说明已经在房间内了，那么给弹窗
                        confirmDialog = new ConfirmDialog(requireActivity(), "您正在直播的房间中\n是否返回？", true,
                                "确定", "取消", new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                confirmDialog.dismiss();
                                return null;
                            }
                        }, new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                jumpRoom(voiceRoomBean);
                                return null;
                            }
                        });
                        confirmDialog.show();
                    }
                }
            }
        });
    }

    /**
     * 跳转到相应的房间
     *
     * @param voiceRoomBean
     */
    private void jumpRoom(VoiceRoomBean voiceRoomBean) {
        IntentWrap.launchRoom(requireContext(), voiceRoomBean.getRoomType(), voiceRoomBean.getRoomId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VoiceRoomProvider.provider().clear();
    }
}
