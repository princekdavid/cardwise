package com.cardwise.app

import android.app.Application
import com.cardwise.app.data.AppContainer

class CardWiseApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
