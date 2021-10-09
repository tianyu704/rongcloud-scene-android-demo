package cn.rongcloud.voiceroom.room.dialogFragment.seatoperation;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.room.NewVoiceRoomModel;

/**
 * 撤销麦位fragment
 */
public class NewRevokeSeatRequestFragment extends BaseBottomSheetDialogFragment {


    private AppCompatTextView tvTitle;
    private AppCompatTextView btnCancelRequest;
    private AppCompatTextView btnCancel;
    private NewVoiceRoomModel newVoiceRoomModel;
    private boolean cancel = false;
    public NewRevokeSeatRequestFragment(NewVoiceRoomModel newVoiceRoomModel) {
        super(R.layout.fragment_new_cancel_request_seat);
        this.newVoiceRoomModel=newVoiceRoomModel;
    }

    @Override
    public void initView() {
        tvTitle = (AppCompatTextView) getView().findViewById(R.id.tv_title);
        btnCancelRequest = (AppCompatTextView) getView().findViewById(R.id.btn_cancel_request);
        btnCancel = (AppCompatTextView) getView().findViewById(R.id.btn_cancel);
    }

    @Override
    public void initListener() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newVoiceRoomModel.cancelRequestSeat(new ClickCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result, String msg) {
                        if (result){
                            dismiss();
                            EToast.showToast("已撤回连线申请");
                            cancel = false;
                        }else {
                            //撤销失败，判断是否已经被同意了在麦位上了
                            if (newVoiceRoomModel.userInSeat()) {
                                EToast.showToast("您已经在麦上了哦");
                            } else {
                                EToast.showToast(msg);
                            }
                            cancel = false;
                        }
                    }
                });
            }
        });
        super.initListener();
    }
}
