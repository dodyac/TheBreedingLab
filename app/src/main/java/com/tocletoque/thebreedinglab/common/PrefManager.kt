package com.tocletoque.thebreedinglab.common

import android.content.Context
import com.acxdev.commonFunction.utils.Preference

class PrefManager(private val context: Context) {

    private val preference by lazy {
        Preference(context)
    }

    enum class Type {
        Welcome,
        FirstLitter
    }

    fun isShown(type: Type): Boolean {
        return preference.get().getBoolean(type.name, false)
    }

    fun show(type: Type) {
        preference.put(type.name, true)
    }

    fun reset() {
        preference.clear()
    }
}