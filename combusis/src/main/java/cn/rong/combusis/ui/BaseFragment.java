package cn.rong.combusis.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public abstract class BaseFragment extends Fragment {
    protected final String TAG = this.getClass().getSimpleName();
    protected FragmentActivity activity;
    private View layout;
    private boolean init = false;//init 和 onRefresh()的执行的先后问题

    @Override
    public final void onAttach(Context context) {
        super.onAttach(context);
        activity = (FragmentActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach");
    }

    @Deprecated
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(setLayoutId(), null);
        Log.e(TAG, "onCreateView");
        return layout;
    }

    @Override
    public final void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated");
        init();
        init = true;
    }

    public abstract int setLayoutId();

    public abstract void init();

    protected View getLayout() {
        return layout;
    }

    protected <T extends View> T getView(@IdRes int id) {
        return layout.findViewById(id);
    }

    /**
     * 首次刷新尽量先于init执行
     *
     * @param obj
     */
    public void onRefresh(Object obj) {
        Log.e(TAG, "onRefresh");
    }


    public boolean isInit() {
        return init;
    }
}
