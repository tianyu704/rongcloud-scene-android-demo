/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.mvp.model.message.*
import io.rong.imlib.model.MessageContent
import kotlinx.android.synthetic.main.layout_confirm_dialog.view.*
import kotlinx.android.synthetic.main.layout_system_message_item.view.*

/**
 * @author gusd
 * @Date 2021/06/27
 */

private const val MESSAGE_TYPE_SYSTEM = 0
private const val MESSAGE_TYPE_NORMAL = 1

class VoiceRoomMessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = arrayListOf<MessageContent>()


    override fun getItemViewType(position: Int): Int {
        return if (data[position] is RCChatroomLocationMessage) MESSAGE_TYPE_SYSTEM else MESSAGE_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MESSAGE_TYPE_SYSTEM) SystemMessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_system_message_item, parent, false
            )
        ) else NormalMessageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_normal_message_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == MESSAGE_TYPE_SYSTEM) {
            (holder as SystemMessageViewHolder).bind(data[position] as RCChatroomLocationMessage)
        } else {
            (holder as NormalMessageViewHolder).bind(data[position])
        }
    }

    fun addMessage(message: MessageContent) {
        data.add(message)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }


    class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: RCChatroomLocationMessage) {
            with(itemView) {
                tv_message_content.text = message.content
            }
        }
    }

    class NormalMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: MessageContent) {
            with(itemView) {
                when (message) {
                    is RCChatroomBarrage -> {
                        tv_message_content.text = "${message.userName}：${message.content}"
                    }
                    is RCChatroomEnter->{
                        tv_message_content.text = "${message.userName} 进来了"
                    }
                    is RCChatroomKickOut -> {
                        tv_message_content.text = "${message.targetName} 被 ${message.userName} 踢出去了"
                    }
                    is RCChatroomGiftAll -> {
                        tv_message_content.text = "${message.userName} 全麦打赏 ${message.giftName} x${message.number}"
                    }
                    is RCChatroomGift -> {
                        tv_message_content.text = "${message.userName} 送给 ${message.targetName} ${message.giftName} x${message.number}"
                    }
                    is RCChatroomAdmin -> {
                        tv_message_content.text = "${message.userName} ${if(message.isAdmin) "成为管理员" else "被撤回管理员"}"
                    }
                    is RCChatroomSeats -> {
                        tv_message_content.text = "房间更换为 ${message.count} 座模式，请重新上麦"
                    }
                }
            }
        }
    }


}

