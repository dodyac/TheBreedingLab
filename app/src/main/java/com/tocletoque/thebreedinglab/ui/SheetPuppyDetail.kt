package com.tocletoque.thebreedinglab.ui

import androidx.core.widget.doOnTextChanged
import com.acxdev.commonFunction.common.base.BaseSheet
import com.acxdev.commonFunction.utils.ext.view.setText
import com.acxdev.commonFunction.utils.ext.view.string
import com.tocletoque.thebreedinglab.databinding.SheetPuppyDetailBinding
import com.tocletoque.thebreedinglab.isGenesis
import com.tocletoque.thebreedinglab.model.Dog

class SheetPuppyDetail: BaseSheet<SheetPuppyDetailBinding>() {

    companion object {
        const val PUPPY = "puppy"
        const val PUPPIES_NAME = "puppies_name"
    }
    private var onSheetListener: OnSheetListener? = null

    private val puppy by lazy {
        getExtraAs(Dog::class.java, PUPPY)
    }
    private val puppiesName by lazy {
        getExtraAs(Array<String>::class.java, PUPPIES_NAME).toList()
    }

    override fun SheetPuppyDetailBinding.setViews() {
        ivDogImage.setImageResource(puppy.image)
        tilName.setText(puppy.name)
        tilName.isEnabled = !puppy.name.isGenesis
        tvPuppy.text = puppy.getDetailWithoutName()
    }

    override fun SheetPuppyDetailBinding.doAction() {
        tilName.editText?.doOnTextChanged { text, _, _, _ ->
            if (text.toString().lowercase() in puppiesName.map { it.lowercase() }) {
                tilName.error = "Name already taken"
                tilName.isErrorEnabled = true
            } else {
                tilName.error = null
                tilName.isErrorEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        val currentName = binding.tilName.string
        if (currentName != puppy.name && currentName.lowercase() !in puppiesName.map { it.lowercase() }) {
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