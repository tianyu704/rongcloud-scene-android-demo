package cn.rong.combusis.ui.room;

import android.graphics.Rect;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.basis.ui.BaseActivity;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.UiUtils;

import java.util.HashMap;
import java.util.List;

import cn.rong.combusis.R;
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

    @Override
    public int setLayoutId() {
        return R.layout.activity_room;
    }

    @Override
    public void init() {
        initRoom();
        // 状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        getWrapBar().setHide(true).work();
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
                String roomId = mRoomAdapter.getItemData(position);
                if (!TextUtils.equals(roomId, currentRoomId)) {
                    if (currentRoomId != null && switchRoomListenerMap.containsKey(currentRoomId)) {
                        switchRoomListenerMap.get(currentRoomId).destroyRoom();
                        Logger.d("==================destroyRoom:" + currentRoomId);
                    }
                    currentRoomId = roomId;
                    Logger.d("==================joinRoom:" + switchRoomListenerMap.containsKey(currentRoomId));
                    if (switchRoomListenerMap.containsKey(currentRoomId)) {
                        switchRoomListenerMap.get(currentRoomId).joinRoom();
                        Logger.e("==================joinRoom:" + currentRoomId);
                    }
                }
                Logger.d("==================end选中了第几个：" + position + ",current:" + currentRoomId);
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

    protected abstract void initRoom();

    // 当前页的位置
    protected abstract int getCurrentItem();

    // 返回要初始化的Fragment
    protected abstract Fragment getFragment(String roomId);

    // 加载数据
    protected abstract List<String> loadData();

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
        Logger.e("=================addSwitchRoomListener");
    }

    public void removeSwitchRoomListener(String roomId) {
        switchRoomListenerMap.remove(roomId);
        Logger.e("=================removeSwitchRoomListener");
    }

}
