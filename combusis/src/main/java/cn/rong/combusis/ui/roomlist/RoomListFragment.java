package cn.rong.combusis.ui.roomlist;

import android.widget.ImageView;

import com.basis.ui.ListFragment;
import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.refresh.XRecyclerView;

import cn.rong.combusis.R;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.OnItemClickListener;

/**
 * @author gyn
 * @date 2021/9/15
 */
public abstract class RoomListFragment extends ListFragment<VoiceRoomBean, VoiceRoomBean, RcyHolder> implements OnItemClickListener<VoiceRoomBean> {

    private RoomListAdapter mAdapter;
    private int mCurrentPage = 1;
    private ImageView mCreateButton;
    private XRecyclerView mRoomList;

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
        mCreateButton = getView(R.id.iv_create_room);
        mRoomList = getView(R.id.xrv_room);

        loadRoomList(true);

        mCreateButton.setOnClickListener(v -> {

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

    private void createRoom() {
//        VoiceRoomNetManager.INSTANCE.getARoomApi().getRoomList()
    }
//
//
//    private void showCreateVoiceRoomDialog() {
//        createVoiceRoomDialogFragment = CreateVoiceRoomDialogFragment(this);
//        createVoiceRoomDialogFragment?.show(supportFragmentManager, "CreateRoomDialog")
//    }
}
