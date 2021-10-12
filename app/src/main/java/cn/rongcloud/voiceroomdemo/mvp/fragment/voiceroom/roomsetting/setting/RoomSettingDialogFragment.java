package cn.rongcloud.voiceroomdemo.mvp.fragment.voiceroom.roomsetting.setting;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment;
import cn.rong.combusis.common.ui.dialog.EditDialog;
import cn.rong.combusis.common.ui.dialog.InputPasswordDialog;
import cn.rong.combusis.common.ui.widget.GridSpacingItemDecoration;
import cn.rongcloud.voiceroomdemo.R;
import cn.rongcloud.voiceroomdemo.mvp.model.VoiceRoomModel;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeEmitter;
import io.reactivex.rxjava3.core.MaybeOnSubscribe;
import io.rong.imkit.picture.tools.ToastUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * 聊天室底部弹窗
 */
public class RoomSettingDialogFragment extends BaseBottomSheetDialogFragment
        implements IRoomSettingView, View.OnClickListener {

    private AppCompatImageView ivClose;
    private RecyclerView rvFunctionList;
    private VoiceRoomModel voiceRoomModel;
    private RoomSettingPresenter present;
    private EditDialog modifyNameDialog;
    private InputPasswordDialog inputPasswordDialog;
    private IRoomSettingView view;

    public RoomSettingDialogFragment(VoiceRoomModel voiceRoomModel,IRoomSettingView view) {
        super(R.layout.fragment_room_setting);
        this.voiceRoomModel = voiceRoomModel;
        this.view=view;
    }

    @Override
    public void initView() {
        present = createPresent();
        ivClose = (AppCompatImageView) getDialog().findViewById(R.id.iv_close);
        rvFunctionList = (RecyclerView) getDialog().findViewById(R.id.rv_function_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        rvFunctionList.setLayoutManager(gridLayoutManager);
        GridSpacingItemDecoration gridSpacingItemDecoration = new GridSpacingItemDecoration(gridLayoutManager.getSpanCount(),
                getActivity().getResources().getDimensionPixelSize(R.dimen.background_setting_decoration), true);
        rvFunctionList.addItemDecoration(gridSpacingItemDecoration);
        RoomSettingAdapter roomSettingAdapter = new RoomSettingAdapter();
        rvFunctionList.setAdapter(roomSettingAdapter);
        roomSettingAdapter.refreshData(present.getButtons());
    }

    private RoomSettingPresenter createPresent() {
        RoomSettingPresenter roomSettingPresenter = new RoomSettingPresenter(view, voiceRoomModel, this);
        this.getLifecycle().addObserver(roomSettingPresenter);
        return roomSettingPresenter;
    }

    @Override
    public void initListener() {
        super.initListener();
        ivClose.setOnClickListener(this::onClick);
    }

    @Override
    public void showBackgroundFragment() {

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
        view.showError(message);
    }

    @Override
    public void onLogout() {

    }

    @Override
    public void showMessage(@Nullable String message) {
        view.showMessage(message);
    }

    /**
     * 上锁
     * @return
     */
    @Nullable
    @Override
    public Maybe<String> showPasswordDialog() {
        return Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull MaybeEmitter<String> emitter) throws Throwable {
                inputPasswordDialog = new InputPasswordDialog(getActivity(), true, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        emitter.onComplete();
                        return null;
                    }
                }, new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String password) {
                        if (password.length() < 4) {
                            ToastUtils.s(getActivity(), "请输入 4 位密码");
                            return null;
                        }
                        if (inputPasswordDialog!=null) {
                            inputPasswordDialog.dismiss();
                        }
                        emitter.onSuccess(password);
                        return null;
                    }
                }
                );
                inputPasswordDialog.show();
            }
        });

    }

    /**
     * 修改名称
     * @param roomName
     * @return
     */
    @Nullable
    @Override
    public Maybe<String> showModifyRoomNameDialog(@Nullable String roomName) {
        return Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull MaybeEmitter<String> emitter) throws Throwable {
                modifyNameDialog = new EditDialog(requireActivity(),
                        "修改房间标题",
                        "请输入房间名",
                        TextUtils.isEmpty(roomName) ? "" : roomName,
                        10,
                        false,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                emitter.onComplete();
                                return null;
                            }
                        }, new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String newName) {
                        if (TextUtils.isEmpty(newName)) {
                            ToastUtils.s(getActivity(),"房间名不能为空");
                            return null;
                        }
                        if (modifyNameDialog!=null) {
                            modifyNameDialog.dismiss();
                        }
                        emitter.onSuccess(newName);
                        return null;
                    }
                });
                modifyNameDialog.show();
            }
        });
    }

    @Override
    public void hideSettingView() {

    }

    @Override
    public void showMusicSettingFragment() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (inputPasswordDialog!=null) {
            inputPasswordDialog.dismiss();
        }
        if (modifyNameDialog!=null) {
            modifyNameDialog.dismiss();
        }
    }

}
