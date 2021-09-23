package cn.rong.combusis.ui.room;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.basis.ui.BaseActivity;
import com.kit.utils.Logger;

import java.util.List;

import cn.rong.combusis.R;


/**
 * @author gyn
 * @date 2021/9/14
 */
public abstract class AbsRoomActivity<T> extends BaseActivity {

    private ViewPager2 mViewPager;
    private RoomVPAdapter<String> mRoomAdapter;
    private AbsRoomFragment mCurrentFragment;

    @Override
    public int setLayoutId() {
        return R.layout.activity_room;
    }

    @Override
    public void init() {
        initRoom();
        getWrapBar().setHide(true).work();
        // 初始化viewpager并设置数据和监听
        mViewPager = getView(R.id.vp_room);
        mRoomAdapter = new RoomVPAdapter<String>(this);
        mViewPager.setAdapter(mRoomAdapter);
        mRoomAdapter.setData(loadData());
        mViewPager.setCurrentItem(getCurrentItem(), false);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 上一个滑走
                Fragment lastFragment = getSupportFragmentManager().findFragmentByTag("f" + position);
                if (lastFragment instanceof AbsRoomFragment) {
                    Logger.e("last page dismiss");
                    ((AbsRoomFragment) lastFragment).destroyRoom();
                }
                // 当前显示
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + position);
                if (currentFragment instanceof AbsRoomFragment) {
                    Logger.e("current page show");
                    switchRoom(mRoomAdapter.getItemData(position));
                    mCurrentFragment = (AbsRoomFragment) currentFragment;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    protected abstract void initRoom();

    // 当前页的位置
    protected abstract int getCurrentItem();

    // 返回要初始化的Fragment
    protected abstract Fragment getFragment();

    // 加载数据
    protected abstract List<String> loadData();

    // 切换房间，先退出上个房间，根据id查询当前房间数据，再切换房间
    protected abstract void switchRoom(String roomId);

    // 处理完数据，把数据放到fragment中
    public void joinRoom(T t) {
        if (mCurrentFragment != null) {
            mCurrentFragment.joinRoom(t);
        }
    }
}
