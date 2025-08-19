package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.databinding.SheetSeasonAdvanceBinding

class SheetSeasonAdvance: BaseSheet<SheetSeasonAdvanceBinding>() {

    companion object {
        const val NOTE = "note"
    }

    private val note by lazy {
        arguments?.getString(NOTE, "-") ?: "-"
    }

    override fun SheetSeasonAdvanceBinding.setViews() {
        tvSeasonDetail.text = note
    }
}