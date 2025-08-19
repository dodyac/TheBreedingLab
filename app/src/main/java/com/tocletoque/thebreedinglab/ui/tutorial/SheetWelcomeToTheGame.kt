package com.tocletoque.thebreedinglab.ui.tutorial

import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.databinding.SheetWelcomeToTheGameBinding

class SheetWelcomeToTheGame: BaseSheet<SheetWelcomeToTheGameBinding>() {

    override val canDismiss: Boolean
        get() = false
    private var onSheetListener: OnSheetListener? = null

    override fun SheetWelcomeToTheGameBinding.doAction() {
        btnBasicControl.setOnClickListener {
            onSheetListener?.onBasicControl()
            dismiss()
        }
    }


    override fun onDestroyView() {
        onSheetListener = null
        super.onDestroyView()
    }

    fun setOnSheetListener(listener: OnSheetListener) {
        this.onSheetListener = listener
    }

    interface OnSheetListener {
        fun onBasicControl()
    }
}