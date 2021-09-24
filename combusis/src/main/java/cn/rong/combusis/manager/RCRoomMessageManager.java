package cn.rong.combusis.manager;

import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/23
 */
public class RCRoomMessageManager {
    private static final RCRoomMessageManager instance = new RCRoomMessageManager();

    public static RCRoomMessageManager getInstance() {
        return instance;
    }

    public void sendMessage(String roomId, MessageContent messageContent) {
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM,
                roomId,
                messageContent,
                null,
                null, new IRongCoreCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {

                    }

                    @Override
                    public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {

                    }
                });
    }
}
