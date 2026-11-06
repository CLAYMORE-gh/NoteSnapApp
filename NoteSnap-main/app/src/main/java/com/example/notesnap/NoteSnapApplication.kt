package com.example.notesnap

import android.app.Application
import com.example.notesnap.data.ServiceLocator

class NoteSnapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
