package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.databinding.SheetParentProfileBinding
import com.tocletoque.thebreedinglab.model.Dog

class SheetParentProfile: BaseSheet<SheetParentProfileBinding>() {

    companion object {
        const val FATHER = "father"
        const val MOTHER = "mother"
    }

    private val mother by lazy {
        getExtraAs(Dog::class.java, MOTHER)
    }
    private val father by lazy {
        getExtraAs(Dog::class.java, FATHER)
    }

    override fun SheetParentProfileBinding.setViews() {
        tvMotherProfile.text = mother.getDetail()
        tvFatherProfile.text = father.getDetail()
    }
}