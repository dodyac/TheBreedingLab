package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.acxdev.commonFunction.utils.ext.view.setText
import com.acxdev.commonFunction.utils.ext.view.string
import com.tocletoque.thebreedinglab.databinding.SheetPuppyDetailBinding
import com.tocletoque.thebreedinglab.isGenesis
import com.tocletoque.thebreedinglab.model.Dog

class SheetPuppyDetail: BaseSheet<SheetPuppyDetailBinding>() {

    companion object {
        const val PUPPY = "puppy"
    }
    private var onSheetListener: OnSheetListener? = null

    private val puppy by lazy {
        getExtraAs(Dog::class.java, PUPPY)
    }

    override fun SheetPuppyDetailBinding.setViews() {
        ivDogImage.setImageResource(puppy.image)
        tilName.setText(puppy.name)
        tilName.isEnabled = !puppy.name.isGenesis
        tvPuppy.text = puppy.getDetailWithoutName()
    }

    override fun onDestroyView() {
        val currentName = binding.tilName.string
        if (currentName != puppy.name) {
            onSheetListener?.onNameChanged(currentName)
        }
        onSheetListener = null
        super.onDestroyView()
    }

    fun setOnSheetListener(listener: OnSheetListener) {
        this.onSheetListener = listener
    }

    interface OnSheetListener {
        fun onNameChanged(newName: String)
    }
}