package cn.rong.combusis.ui.room;

import android.graphics.Rect;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.basis.ui.BaseActivity;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.UiUtils;

import java.util.List;

import cn.rong.combusis.R;
import io.rong.imkit.utils.StatusBarUtil;


/**
 * @author gyn
 * @date 2021/9/14
 */
public abstract class AbsRoomActivity<T> extends BaseActivity {

    private ViewPager2 mViewPager;
    private RoomVPAdapter<String> mRoomAdapter;
    private AbsRoomFragment mCurrentFragment;
    private int mCurrentPosition;
    private int bottomMargin = 0;

    @Override
    public int setLayoutId() {
        return R.layout.activity_room;
    }

    @Override
    public void init() {
        // 状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        initRoom();
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
                // 当前显示
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + position);
                if (currentFragment instanceof AbsRoomFragment && mCurrentFragment != currentFragment) {
                    Logger.e("current page show");
                    if (!TextUtils.equals(mCurrentFragment.getTag(), currentFragment.getTag())) {
                        // 上一个滑走要销毁
                        mCurrentFragment.destroyRoom();
                        // 要显示的
                        mCurrentFragment = (AbsRoomFragment) currentFragment;
                        switchRoom(mRoomAdapter.getItemData(position));
                    }
                }
                Logger.e("==================选中了第几个：" + position + ",current:" + currentFragment);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        // 第一次进入非选中第一页时onPageSelected获得不了fragment
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            Logger.e("==========" + fragmentManager.getFragments().size() + "  " + fragment.getTag());
            if (mCurrentFragment == null) {
                mCurrentFragment = (AbsRoomFragment) fragment;
                switchRoom(mRoomAdapter.getItemData(mCurrentPosition));
            }
        });

        mRoomAdapter = new RoomVPAdapter<String>(this);
        mViewPager.setAdapter(mRoomAdapter);
        mRoomAdapter.setData(loadData());
        mViewPager.setCurrentItem(getCurrentItem(), false);

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

    //当前页面的fragment
    protected Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    // 返回要初始化的Fragment
    protected abstract Fragment getFragment();

    // 加载数据
    protected abstract List<String> loadData();

    // 切换房间，先退出上个房间，根据id查询当前房间数据，再切换房间
    protected abstract void switchRoom(String roomId);

    //加载房间之前需要先处理

    // 处理完数据，把数据放到fragment中
    public void joinRoom(T t) {
        if (mCurrentFragment != null) {
            mCurrentFragment.joinRoom(t);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment != null) {
            mCurrentFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
