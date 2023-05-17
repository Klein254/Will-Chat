package com.example.medilink.listeners

import com.example.medilink.models.User

interface ConversionListener {
    fun onConversionClicker(user: User?)
}