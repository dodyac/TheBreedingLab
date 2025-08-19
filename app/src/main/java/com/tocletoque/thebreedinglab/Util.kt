package com.tocletoque.thebreedinglab

import android.content.res.Resources

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val String.isGenesis: Boolean
    get() {
        return this == "Genesis"
    }