package cn.rong.combusis.ui.roomlist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.basis.adapter.interfaces.IAdapte;
import com.basis.adapter.recycle.RcyHolder;
import com.basis.ui.ListFragment;
import com.bcq.refresh.XRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.rong.combusis.R;
import cn.rong.combusis.common.ui.dialog.ConfirmDialog;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.OnItemClickRoomListListener;
import io.rong.imkit.picture.tools.ToastUtils;

/**
 * @author gyn
 * @date 2021/9/15
 */
public abstract class AbsRoomListFragment extends ListFragment<VoiceRoomBean, VoiceRoomBean, RcyHolder> implements OnItemClickRoomListListener<VoiceRoomBean>, CreateRoomDialog.CreateRoomCallBack {

    private RoomListAdapter mAdapter;
    private int mCurrentPage = 1;
    private XRecyclerView mRoomList;
    private CreateRoomDialog mCreateRoomDialog;

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
            mCurrentPage = 1;
            mRoomList.setNoMore(false);
        }
        VoiceRoomProvider.provider().loadPage(mCurrentPage, getRoomType(), voiceRoomBeans -> {
            refresh(voiceRoomBeans, isRefresh);
            if (mCurrentPage == 1) {
                mRoomList.refreshComplete();
            } else {
                mRoomList.loadComplete();
            }

            if (voiceRoomBeans != null && !voiceRoomBeans.isEmpty()) {
                mCurrentPage++;
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
            ToastUtils.s(requireContext(), voiceRoomBean.getRoomId());
            return null;
        }).show();
    }

    private void createRoom() {
        mCreateRoomDialog = new CreateRoomDialog(requireActivity(), mLauncher, getRoomType(), this);
        mCreateRoomDialog.show();
    }

}
