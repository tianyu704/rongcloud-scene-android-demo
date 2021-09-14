package cn.rong.combusis.ui.roomlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class VPAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private String[] titles;

    public VPAdapter(FragmentManager fragmentManager, List<Fragment> fragments, String[] titles) {
        super(fragmentManager);
        this.fragments = fragments;
        this.titles = titles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return null == fragments ? 0 : fragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}