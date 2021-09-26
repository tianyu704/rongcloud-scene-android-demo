package cn.rongcloud.radioroom.ui.room;

import cn.rong.combusis.ui.mvp.IBaseView;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/24
 */
public interface RadioRoomView extends IBaseView {

    void showMessage(MessageContent messageContent, boolean isRefresh);

}
