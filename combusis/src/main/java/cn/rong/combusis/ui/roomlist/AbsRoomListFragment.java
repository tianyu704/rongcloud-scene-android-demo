package cn.rong.combusis.ui.roomlist;


import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.basis.ui.BaseFragment;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

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
import cn.rong.combusis.ui.room.widget.RecyclerViewAtVP2;
import cn.rong.combusis.widget.miniroom.MiniRoomManager;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @author gyn
 * @date 2021/9/15
 */
public abstract class AbsRoomListFragment extends BaseFragment implements OnItemClickRoomListListener<VoiceRoomBean>, CreateRoomDialog.CreateRoomCallBack {

    private RoomListAdapter mAdapter;
    private RecyclerViewAtVP2 mRoomList;
    private CreateRoomDialog mCreateRoomDialog;
    private ConfirmDialog confirmDialog;
    private SmartRefreshLayout refreshLayout;
    private View emptyView;

    private ActivityResultLauncher mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result != null && result.getData() != null && result.getData().getData() != null && mCreateRoomDialog != null) {
            mCreateRoomDialog.setCoverUri(result.getData().getData());
        }
    });

    @Override
    public void init() {
        mRoomList = (RecyclerViewAtVP2) getView(R.id.xrv_room);
        refreshLayout = (SmartRefreshLayout) getView(R.id.layout_refresh);
        emptyView = (View) getView(R.id.layout_empty);
        getView(R.id.iv_create_room).setOnClickListener(v -> {
            createRoom();
        });
        mAdapter = new RoomListAdapter(getContext(), R.layout.item_room);
        mAdapter.setOnItemClickListener(this);
        mRoomList.setAdapter(mAdapter);
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            loadRoomList(true);
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            loadRoomList(false);
        });
        emptyView.setOnClickListener(v -> {
            loadRoomList(true);
        });
        checkUserRoom();
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_room_list;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadRoomList(true);
        //请求判断当时是否在房间内，如果弹窗不显示的话
    }

    public abstract RoomType getRoomType();

    /**
     * 请求房间列表数据
     *
     * @param isRefresh 是否是刷新，否则是加载更多
     */
    private void loadRoomList(boolean isRefresh) {
        if (isRefresh) {
            refreshLayout.resetNoMoreData();
        }
        VoiceRoomProvider.provider().loadPage(isRefresh, getRoomType(), voiceRoomBeans -> {
            mAdapter.setData(voiceRoomBeans, isRefresh);

            if (VoiceRoomProvider.provider().getPage() <= 2) {
                refreshLayout.finishRefresh();
            } else {
                refreshLayout.finishLoadMore();
            }

            if (voiceRoomBeans != null && !voiceRoomBeans.isEmpty()) {
                emptyView.setVisibility(View.GONE);
            } else {
                refreshLayout.setNoMoreData(true);
                if (VoiceRoomProvider.provider().getPage() == 1) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onCreateSuccess(VoiceRoomBean voiceRoomBean) {
        mAdapter.getData().add(0, voiceRoomBean);
        mAdapter.notifyItemInserted(0);
        clickItem(voiceRoomBean, 0, true, Arrays.asList(voiceRoomBean));
    }

    @Override
    public void onCreateExist(VoiceRoomBean voiceRoomBean) {
        new ConfirmDialog(requireContext(), getString(R.string.text_you_have_created_room), true, "确定", "取消", () -> null, () -> {
            jumpRoom(voiceRoomBean);
            return null;
        }).show();
    }

    private void createRoom() {
        showLoading("");
        // 创建之前检查是否已有创建的房间
        OkApi.put(VRApi.ROOM_CREATE_CHECK, null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                dismissLoading();
                if (result.ok()) {
                    mCreateRoomDialog = new CreateRoomDialog(requireActivity(), mLauncher, getRoomType(), AbsRoomListFragment.this);
                    mCreateRoomDialog.show();
                } else if (result.getCode() == 30016) {
                    VoiceRoomBean voiceRoomBean = result.get(VoiceRoomBean.class);
                    if (voiceRoomBean != null) {
                        onCreateExist(voiceRoomBean);
                    } else {
                        mCreateRoomDialog = new CreateRoomDialog(requireActivity(), mLauncher, getRoomType(), AbsRoomListFragment.this);
                        mCreateRoomDialog.show();
                    }
                }
            }
        });
    }

    /**
     * 检查用户之前是否在某个房间内
     */
    private void checkUserRoom() {
        if (MiniRoomManager.getInstance().isShowing()) {
            //如果有小窗口存在的情况下，不显示
            return;
        }
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
                                changeUserRoom();
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

    //更改所属房间
    private void changeUserRoom() {
        HashMap<String, Object> params = new OkParams()
                .add("roomId", "")
                .build();
        OkApi.get(VRApi.USER_ROOM_CHANGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
            }
        });
    }
}
