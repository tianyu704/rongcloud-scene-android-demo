/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package com.example.voiceroomdemo.mvp.fragment.voiceroom.musicsetting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceroomdemo.R
import com.example.voiceroomdemo.common.ui
import com.example.voiceroomdemo.mvp.fragment.BaseFragment
import com.example.voiceroomdemo.ui.uimodel.MUSIC_FROM_TYPE_SYSTEM
import com.example.voiceroomdemo.ui.uimodel.MUSIC_FUNCTION_LOCAL_ADD
import com.example.voiceroomdemo.ui.uimodel.UiMusicModel
import kotlinx.android.synthetic.main.fragment_music_add.*
import kotlinx.android.synthetic.main.layout_add_music_item.view.*

/**
 * @author gusd
 * @Date 2021/07/06
 */
private const val TAG = "MusicAddFragment"

class MusicAddFragment(view: IMusicAddView, val roomId: String) :
    BaseFragment<MusicAddPresenter, IMusicAddView>(R.layout.fragment_music_add),
    IMusicAddView by view {

    override fun initPresenter(): MusicAddPresenter {
        return MusicAddPresenter(this, roomId)
    }

    override fun initView() {
        rv_list.adapter = MyAdapter()

    }

    override fun initData() {
        super.initData()
    }

    override fun showMusicList(list: List<UiMusicModel>) {
        ui {
            (rv_list.adapter as MyAdapter).refreshData(list)
        }
    }

    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {

        private val data = arrayListOf<UiMusicModel>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
            MyViewHolder(
                LayoutInflater.from(this@MusicAddFragment.requireContext())
                    .inflate(R.layout.layout_add_music_item, parent, false)
            )

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        fun refreshData(list: List<UiMusicModel>) {
            data.clear()
            data.addAll(list)
            notifyDataSetChanged()
        }

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(model: UiMusicModel) {
            with(itemView) {
                if (model.type >= 0) {
                    this.iv_music_icon.setImageResource(R.drawable.ic_add_music_list_icon)
                    this.tv_music_name.text = model.name
                    this.tv_music_author.isVisible = true
                    this.tv_music_author.text = model.author
                    this.tv_music_size.isVisible = true
                    this.tv_music_size.text = "${model.size}M"
                    this.iv_music_status.setImageResource(
                        if (model.addAlready)
                            R.drawable.ic_add_music_had_add
                        else R.drawable.ic_add_music_not_add
                    )
                    this.iv_music_status.setOnClickListener {
                        if (!model.addAlready) {
                            presenter.addMusic(
                                model.name,
                                model.author,
                                MUSIC_FROM_TYPE_SYSTEM,
                                model.url ?: ""
                            )
                        }
                    }
                } else if (model.type == MUSIC_FUNCTION_LOCAL_ADD) {
                    this.tv_music_author.isVisible = false
                    this.tv_music_size.isVisible = false
                    this.tv_music_name.text = "本地上传"
                    this.iv_music_icon.setImageResource(R.drawable.ic_add_music_from_local)
                    this.iv_music_status.setImageResource(R.drawable.ic_add_music_not_add)
                    this.iv_music_status.setOnClickListener {

                    }
                }
            }
        }
    }
}