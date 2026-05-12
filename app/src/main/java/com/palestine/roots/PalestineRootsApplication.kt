package com.palestine.roots

import android.app.Application
import android.content.Context
import com.palestine.roots.util.LocaleHelper

class PalestineRootsApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(base))
    }
}
