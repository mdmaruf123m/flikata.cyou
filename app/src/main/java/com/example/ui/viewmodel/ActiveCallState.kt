package com.example.ui.viewmodel

data class ActiveCallState(
    val callId: Int = 0,
    val partnerId: Int = 0,
    val partnerName: String = "",
    val partnerAvatar: String? = null,
    val isVerified: Boolean = false,
    val isCaller: Boolean = false,
    val status: String = "ringing",
    val durationSeconds: Int = 0
)
