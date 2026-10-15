package com.example.data.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import com.example.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

class AudioCallManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
    }
    private var toneGenerator: ToneGenerator? = null
    private var incomingRingtone: Ringtone? = null

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private var recordJob: Job? = null
    private var playbackJob: Job? = null
    private var ringtoneJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _micAmplitude = MutableStateFlow(0f)
    val micAmplitude: StateFlow<Float> = _micAmplitude.asStateFlow()

    private val sampleRate = 16000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var currentCallId: Int = 0
    private var currentUserId: Int = 0
    private var currentPartnerId: Int = 0

    // Ringing tone generator: outgoing ringback tone for caller, native ringtone for receiver
    fun startRinging(isIncoming: Boolean) {
        stopRinging()
        if (isIncoming) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 800, 1000), 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 800, 1000), 0)
                }
            } catch (e: Exception) {
                // Ignore
            }

            try {
                val ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                incomingRingtone = RingtoneManager.getRingtone(context, ringtoneUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    incomingRingtone?.isLooping = true
                }
                incomingRingtone?.play()
            } catch (e: Exception) {
                // Fallback to tone generator
                ringtoneJob = scope.launch {
                    try {
                        toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 85)
                        while (isActive) {
                            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1000)
                            delay(2500)
                        }
                    } catch (ex: Exception) {
                        // Ignore
                    }
                }
            }
        } else {
            // Outgoing call dialing sound ("tut tut" ringback tone)
            ringtoneJob = scope.launch {
                try {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 85)
                    while (isActive) {
                        toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1200)
                        delay(2800)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun stopRinging() {
        ringtoneJob?.cancel()
        ringtoneJob = null

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }

        try {
            incomingRingtone?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        incomingRingtone = null

        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
        } catch (e: Exception) {
            // Ignore
        }
        toneGenerator = null
    }

    // Start real 2-way audio call stream
    fun startAudioStream(callId: Int, userId: Int, partnerId: Int) {
        stopRinging()
        currentCallId = callId
        currentUserId = userId
        currentPartnerId = partnerId

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            applySpeakerRouting(_isSpeakerOn.value)
        } catch (e: Exception) {
            // Ignore
        }

        startRecording(callId, userId, partnerId)
        startPlayback(callId, userId, partnerId)
    }

    private fun startRecording(callId: Int, userId: Int, partnerId: Int) {
        recordJob?.cancel()
        recordJob = scope.launch {
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)
            val bufferSize = (minBuf * 2).coerceAtLeast(2048)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    channelConfigIn,
                    audioFormat,
                    bufferSize
                )
                audioRecord?.startRecording()
            } catch (e: SecurityException) {
                return@launch
            } catch (e: Exception) {
                return@launch
            }

            val buffer = ShortArray(bufferSize / 2)
            val chunkStream = ByteArrayOutputStream()
            var lastUploadTime = System.currentTimeMillis()

            while (isActive && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val readShorts = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readShorts > 0) {
                    var sum = 0.0
                    for (i in 0 until readShorts) {
                        val sample = buffer[i].toDouble()
                        sum += sample * sample
                        if (!_isMuted.value) {
                            chunkStream.write(buffer[i].toInt() and 0xFF)
                            chunkStream.write((buffer[i].toInt() shr 8) and 0xFF)
                        }
                    }
                    val rms = sqrt(sum / readShorts)
                    val normalizedAmp = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                    _micAmplitude.value = if (!_isMuted.value) normalizedAmp else 0f

                    val now = System.currentTimeMillis()
                    if (now - lastUploadTime >= 300) {
                        if (!_isMuted.value && chunkStream.size() > 0) {
                            val bytes = chunkStream.toByteArray()
                            chunkStream.reset()
                            lastUploadTime = now

                            launch(Dispatchers.IO) {
                                sendAudioChunk(callId, userId, bytes)
                            }
                        } else if (_isMuted.value) {
                            chunkStream.reset()
                            lastUploadTime = now
                        }
                    }
                } else {
                    delay(40)
                }
            }
        }
    }

    private fun startPlayback(callId: Int, userId: Int, partnerId: Int) {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            val minBuf = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)
            val bufferSize = (minBuf * 2).coerceAtLeast(2048)

            try {
                audioTrack = AudioTrack(
                    AudioManager.STREAM_VOICE_CALL,
                    sampleRate,
                    channelConfigOut,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
                audioTrack?.play()
            } catch (e: Exception) {
                return@launch
            }

            while (isActive) {
                try {
                    val partnerBytes = fetchPartnerAudio(callId, userId, partnerId)
                    if (partnerBytes != null && partnerBytes.isNotEmpty()) {
                        audioTrack?.write(partnerBytes, 0, partnerBytes.size)
                    }
                } catch (e: Exception) {
                    // Ignore transient network errors
                }
                delay(300)
            }
        }
    }

    private fun sendAudioChunk(callId: Int, userId: Int, audioBytes: ByteArray) {
        try {
            val url = "${ApiClient.BASE_URL}calls/audio_relay.php?call_id=$callId&sender_id=$userId"
            val body = audioBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            ApiClient.okHttpClient.newCall(request).execute().close()
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun fetchPartnerAudio(callId: Int, userId: Int, partnerId: Int): ByteArray? {
        return try {
            val url = "${ApiClient.BASE_URL}calls/audio_relay.php?call_id=$callId&sender_id=$userId&partner_id=$partnerId&get_raw=1"
            val request = Request.Builder()
                .url(url)
                .header("Cache-Control", "no-cache")
                .get()
                .build()
            val response = ApiClient.okHttpClient.newCall(request).execute()
            val bytes = response.body?.bytes()
            response.close()
            bytes
        } catch (e: Exception) {
            null
        }
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        if (newMute) {
            _micAmplitude.value = 0f
            // Notify server to clear buffer
            scope.launch {
                try {
                    val url = "${ApiClient.BASE_URL}calls/audio_relay.php?call_id=$currentCallId&sender_id=$currentUserId&is_muted=1"
                    val request = Request.Builder().url(url).get().build()
                    ApiClient.okHttpClient.newCall(request).execute().close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun toggleSpeaker() {
        val newState = !_isSpeakerOn.value
        _isSpeakerOn.value = newState
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            applySpeakerRouting(newState)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun applySpeakerRouting(isSpeaker: Boolean) {
        audioManager.isSpeakerphoneOn = isSpeaker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.availableCommunicationDevices
            val targetType = if (isSpeaker) AudioDeviceInfo.TYPE_BUILTIN_SPEAKER else AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
            val targetDevice = devices.firstOrNull { it.type == targetType }
            if (targetDevice != null) {
                audioManager.setCommunicationDevice(targetDevice)
            } else if (!isSpeaker) {
                audioManager.clearCommunicationDevice()
            }
        }
    }

    fun stopAudioCall() {
        stopRinging()
        recordJob?.cancel()
        playbackJob?.cancel()
        recordJob = null
        playbackJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioRecord = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            }
        } catch (e: Exception) {
            // Ignore
        }

        _micAmplitude.value = 0f
        _isMuted.value = false
        _isSpeakerOn.value = false
    }
}
