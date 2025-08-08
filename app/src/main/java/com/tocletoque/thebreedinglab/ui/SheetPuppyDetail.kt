package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.databinding.SheetPuppyDetailBinding
import com.tocletoque.thebreedinglab.model.Dog

class SheetPuppyDetail: BaseSheet<SheetPuppyDetailBinding>() {

    companion object {
        const val PUPPY = "puppy"
    }

    private val puppy by lazy {
        getExtraAs(Dog::class.java, PUPPY)
    }

    override fun SheetPuppyDetailBinding.setViews() {
        tvPuppy.text = puppy.getDetail()
    }
}