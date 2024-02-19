package com.obsfreegdsc.obsfree

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

class TextToSpeechManager private constructor() {
    private lateinit var textToSpeech: TextToSpeech
    private var initalized = false
    private var speakQueue: Queue<String> = LinkedList()

    fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.KOREAN

                initalized = true

                while (speakQueue.isNotEmpty()) {
                    val text = speakQueue.poll()
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "TTS")
                }
            }
        }
    }

    fun speak(text: String) {
        if (initalized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "TTS")
        } else {
            speakQueue.add(text)
        }
    }

    companion object {
        @Volatile private var instance: TextToSpeechManager? = null
        private lateinit var context: Context

        fun getInstance(context: Context): TextToSpeechManager {
            return instance ?: synchronized(this) {
                instance ?: TextToSpeechManager().also {
                    this.context = context
                    instance = it
                    it.initTextToSpeech()
                }
            }
        }
    }
}