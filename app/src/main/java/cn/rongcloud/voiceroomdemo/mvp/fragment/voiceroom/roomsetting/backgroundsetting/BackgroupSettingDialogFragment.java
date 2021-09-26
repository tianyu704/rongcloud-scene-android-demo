package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.backgroundsetting;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rongcloud.common.base.BaseBottomSheetDialogFragment;


import java.util.List;

import cn.rong.combusis.common.ui.widget.GridSpacingItemDecoration;
import cn.rongcloud.voiceroomdemo.R;
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.rong.imkit.picture.tools.ToastUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * 修改房间背景
 */
public class BackgroupSettingDialogFragment extends BaseBottomSheetDialogFragment
        implements IBackgroundSettingView, View.OnClickListener {

    private IBackgroundSettingView view;
    private VoiceRoomModel voiceRoomModel;
    private AppCompatTextView tvTitle;
    private AppCompatTextView tvConfirm;
    private RecyclerView rvBackgroundList;
    private BackgroundSettingAdapter roomSettingAdapter;
    private BackgroundSettingPresenter present;

    public BackgroupSettingDialogFragment(VoiceRoomModel voiceRoomModel, IBackgroundSettingView view) {
        super(R.layout.fragment_background_setting);
        this.voiceRoomModel = voiceRoomModel;
        this.view = view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_confirm:
                voiceRoomModel.setRoomBackground(roomSettingAdapter.getCurrentSelectedBackground())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean result) throws Throwable {
                                if (result){
                                    ToastUtils.s(getActivity(),"设置成功");
                                }else {
                                    ToastUtils.s(getActivity(),"设置失败");
                                }
                                dismiss();
                            }
                        });
                break;
        }
    }

    @Override
    public void initView() {
        present = createPresent();
        tvTitle = (AppCompatTextView) getDialog().findViewById(R.id.tv_title);
        tvConfirm = (AppCompatTextView) getDialog().findViewById(R.id.tv_confirm);
        rvBackgroundList = (RecyclerView) getDialog().findViewById(R.id.rv_background_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rvBackgroundList.setLayoutManager(gridLayoutManager);
        GridSpacingItemDecoration gridSpacingItemDecoration = new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(),
                getActivity().getResources().getDimensionPixelSize(R.dimen.background_setting_decoration), true);
        rvBackgroundList.addItemDecoration(gridSpacingItemDecoration);
        roomSettingAdapter = new BackgroundSettingAdapter(new Function1<String, Unit>() {
            @Override
            public Unit invoke(String selectBackground) {
                roomSettingAdapter.selectBackground(selectBackground);
                return null;
            }
        });
        rvBackgroundList.setAdapter(roomSettingAdapter);
    }

    @Override
    public void initListener() {
        tvConfirm.setOnClickListener(this::onClick);
        super.initListener();
    }

    private BackgroundSettingPresenter createPresent() {
        BackgroundSettingPresenter roomSettingPresenter = new BackgroundSettingPresenter(this, this);
        this.getLifecycle().addObserver(roomSettingPresenter);
        return roomSettingPresenter;
    }

    @Override
    public void showWaitingDialog() {

    }

    @Override
    public void hideWaitingDialog() {

    }

    @Override
    public void showLoadingView() {

    }

    @Override
    public void showNormal() {

    }

    @Override
    public void showEmpty() {

    }

    @Override
    public void showError(int errorCode, @Nullable String message) {

    }

    @Override
    public void showError(@Nullable String message) {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public void showMessage(@Nullable String message) {

    }

    @Override
    public void onBackgroundList(@NonNull List<String> backGroundUrlList) {
        roomSettingAdapter.refreshData(backGroundUrlList,
                voiceRoomModel.getCurrentUIRoomInfo().getRoomBean().getBackgroundUrl());
    }
}
