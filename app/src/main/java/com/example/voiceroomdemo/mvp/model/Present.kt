package com.example.voiceroomdemo.mvp.model

import androidx.annotation.DrawableRes

data class Present(
    val index: Int,
    @DrawableRes val icon: Int,
    val name: String,
    val price: Int,
)