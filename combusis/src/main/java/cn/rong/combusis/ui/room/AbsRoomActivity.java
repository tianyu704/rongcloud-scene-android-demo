package cn.rong.combusis.ui.room;

import android.graphics.Rect;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.basis.ui.BaseActivity;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.UiUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rong.combusis.DataShareManager;
import cn.rong.combusis.R;
import cn.rong.combusis.intent.IntentWrap;
import cn.rong.combusis.provider.voiceroom.RoomType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import io.rong.imkit.utils.StatusBarUtil;


/**
 * @author gyn
 * @date 2021/9/14
 */
public abstract class AbsRoomActivity extends BaseActivity {

    private ViewPager2 mViewPager;
    private RoomVPAdapter mRoomAdapter;
    private int mCurrentPosition;
    private int bottomMargin = 0;
    private HashMap<String, SwitchRoomListener> switchRoomListenerMap = new HashMap<>();
    private String currentRoomId;
    private SmartRefreshLayout refreshLayout;
    private boolean canRefreshAndLoadMore;

    @Override
    public int setLayoutId() {
        return R.layout.activity_room;
    }

    @Override
    protected void onDestroy() {
        // 取消忽略av call
        DataShareManager.get().setIgnoreIncomingCall(false);
        super.onDestroy();
    }

    @Override
    public void init() {
        // 忽略av call
        DataShareManager.get().setIgnoreIncomingCall(true);
        initRoom();
        // 状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        getWrapBar().setHide(true).work();
        // 下拉刷新和加载更多
        refreshLayout = getView(R.id.layout_refresh);
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refresh();
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadMore();
            }
        });
        initRefreshAndLoadMore();
        refreshLayout.setEnableRefresh(canRefreshAndLoadMore);
        refreshLayout.setEnableLoadMore(canRefreshAndLoadMore);

        // 初始化viewpager并设置数据和监听
        mViewPager = getView(R.id.vp_room);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPosition = position;
                if (canRefreshAndLoadMore) {
                    refreshLayout.setEnableLoadMore(mCurrentPosition == mRoomAdapter.getItemCount() - 1);
                    refreshLayout.setEnableRefresh(mCurrentPosition == 0);
                }
                getIntent().putExtra(IntentWrap.KEY_ROOM_POSITION, position);
                String roomId = mRoomAdapter.getItemData(position);
                Logger.d("==================end选中了第几个：" + position + ",current:" + roomId);
                switchViewPager(roomId);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        mRoomAdapter = new RoomVPAdapter(this);
        mViewPager.setAdapter(mRoomAdapter);
        List<String> roomIds = loadData();
        mRoomAdapter.setData(roomIds);
        mCurrentPosition = getCurrentItem();
        mViewPager.setCurrentItem(mCurrentPosition, false);

        // 由于状态栏透明和键盘有冲突，所以监听键盘弹出时顶起布局
        int screenHeight = UiUtils.INSTANCE.getFullScreenHeight(activity);
        getLayout().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            getLayout().getWindowVisibleDisplayFrame(rect);
            int height = screenHeight - rect.bottom;
            if (bottomMargin != height) {
                // 假定高度超过屏幕的1/4代表键盘弹出了
                if (height > screenHeight / 4) {
                    bottomMargin = height;
                } else {
                    bottomMargin = 0;
                }
                getLayout().setPadding(0, 0, 0, bottomMargin);
            }
        });

    }


    protected void refresh() {
        VoiceRoomProvider.provider().loadPage(true, getRoomType(), voiceRoomBeans -> {
            if (voiceRoomBeans == null) {
                getRefreshLayout().finishRefresh(false);
            } else {
                List<String> ids = new ArrayList<>();
                for (VoiceRoomBean voiceRoomBean : voiceRoomBeans) {
                    if (!voiceRoomBean.isPrivate() && !TextUtils.equals(voiceRoomBean.getCreateUserId(), AccountStore.INSTANCE.getUserId())) {
                        ids.add(voiceRoomBean.getRoomId());
                    }
                }
                refreshViewPagerFinished(ids);
            }
        });
    }

    protected void loadMore() {
        VoiceRoomProvider.provider().loadPage(false, getRoomType(), voiceRoomBeans -> {
            if (voiceRoomBeans == null) {
                getRefreshLayout().finishLoadMore();
                getRefreshLayout().setNoMoreData(true);
            } else {
                List<String> ids = new ArrayList<>();
                for (VoiceRoomBean voiceRoomBean : voiceRoomBeans) {
                    if (!voiceRoomBean.isPrivate() && !TextUtils.equals(voiceRoomBean.getCreateUserId(), AccountStore.INSTANCE.getUserId())) {
                        ids.add(voiceRoomBean.getRoomId());
                    }
                }
                loadMoreViewPagerFinished(ids);
            }
        });
    }

    private void switchViewPager(String roomId) {
        if (!TextUtils.equals(roomId, currentRoomId)) {
            if (currentRoomId != null && switchRoomListenerMap.containsKey(currentRoomId)) {
                switchRoomListenerMap.get(currentRoomId).destroyRoom();
                Logger.d("==================destroyRoom:" + currentRoomId);
            }
            currentRoomId = roomId;
            Logger.d("==================joinRoom:" + switchRoomListenerMap.containsKey(currentRoomId));
            if (switchRoomListenerMap.containsKey(currentRoomId)) {
                switchRoomListenerMap.get(currentRoomId).preJoinRoom();
                Logger.e("==================joinRoom:" + currentRoomId);
            }
        }
    }

    protected void refreshViewPagerFinished(List<String> ids) {
        if (ids == null) return;
        if (ids.size() > 0) {
            String roomId = ids.get(0);
            if (!TextUtils.equals(roomId, currentRoomId)) {
                if (currentRoomId != null && switchRoomListenerMap.containsKey(currentRoomId)) {
                    switchRoomListenerMap.get(currentRoomId).destroyRoom();
                    Logger.d("==================destroyRoom:" + currentRoomId);
                }
                currentRoomId = roomId;
                mRoomAdapter.setData(ids);
            }
        }
        refreshLayout.finishRefresh();
        refreshLayout.setNoMoreData(false);
    }

    protected void loadMoreViewPagerFinished(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            refreshLayout.finishLoadMore();
            refreshLayout.setNoMoreData(true);
            return;
        }
        mRoomAdapter.addData(ids);
        refreshLayout.finishLoadMore();
    }

    protected abstract void initRoom();

    // 当前页的位置
    protected abstract int getCurrentItem();

    // 返回要初始化的Fragment
    protected abstract Fragment getFragment(String roomId);

    // 加载数据
    protected abstract List<String> loadData();

    protected abstract RoomType getRoomType();

    protected SmartRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    protected void initRefreshAndLoadMore() {
        canRefreshAndLoadMore = false;
        List<String> ids = loadData();
        if (ids != null && ids.size() > 0) {
            // 要打开的房间id
            String targetId = ids.get(getCurrentItem());
            // 从缓存中拿voiceRoomBean，判断是不是房主自己
            VoiceRoomBean bean = VoiceRoomProvider.provider().getSync(targetId);
            if (bean != null) {
                if (TextUtils.equals(bean.getCreateUserId(), AccountStore.INSTANCE.getUserId()) || bean.isPrivate()) {
                    canRefreshAndLoadMore = false;
                } else {
                    canRefreshAndLoadMore = true;
                }
            } else {
                canRefreshAndLoadMore = false;
            }
        }
    }

    // 控制是否可以上下滑动，不能上下滑动也不能刷新和加载
    public void setCanSwitch(boolean canSwitch) {
        mViewPager.setUserInputEnabled(canSwitch);
        if (canSwitch) {
            refreshLayout.setEnableRefresh(canRefreshAndLoadMore);
            refreshLayout.setEnableLoadMore(canRefreshAndLoadMore);
        } else {
            refreshLayout.setEnableRefresh(false);
            refreshLayout.setEnableLoadMore(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (switchRoomListenerMap.containsKey(currentRoomId)) {
            switchRoomListenerMap.get(currentRoomId).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public void addSwitchRoomListener(String roomId, SwitchRoomListener switchRoomListener) {
        switchRoomListenerMap.put(roomId, switchRoomListener);
        Logger.d("=================addSwitchRoomListener");
    }

    public void removeSwitchRoomListener(String roomId) {
        switchRoomListenerMap.remove(roomId);
        Logger.d("=================removeSwitchRoomListener");
    }


    public void switchOtherRoom(String roomId) {
        mViewPager.setCurrentItem(mRoomAdapter.getItemPosition(roomId), false);
    }
}
