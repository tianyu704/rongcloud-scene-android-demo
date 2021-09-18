package cn.rong.combusis.ui.roomlist;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.basis.ui.ListFragment;
import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.refresh.XRecyclerView;
import com.rongcloud.common.ui.dialog.ConfirmDialog;

import cn.rong.combusis.R;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.OnItemClickListener;
import io.rong.imkit.picture.tools.ToastUtils;

/**
 * @author gyn
 * @date 2021/9/15
 */
public abstract class RoomListFragment extends ListFragment<VoiceRoomBean, VoiceRoomBean, RcyHolder> implements OnItemClickListener<VoiceRoomBean>, CreateRoomDialog.CreateRoomCallBack {

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
        mRoomList = getView(R.id.xrv_room);

        loadRoomList(true);

        getView(R.id.iv_create_room).setOnClickListener(v -> {
            createRoom();
        });
    }

    @Override
    public void onCustomerRequestAgain(boolean refresh) {
        super.onCustomerRequestAgain(refresh);
        loadRoomList(refresh);
    }

    public abstract RoomType getRoomType();

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
            mAdapter.setData(voiceRoomBeans, isRefresh);
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
