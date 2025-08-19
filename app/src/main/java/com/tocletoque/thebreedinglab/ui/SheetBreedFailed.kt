package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.common.Constant
import com.tocletoque.thebreedinglab.databinding.SheetBreedFailedBinding
import com.tocletoque.thebreedinglab.model.Dog
import java.util.Locale

class SheetBreedFailed: BaseSheet<SheetBreedFailedBinding>() {

    companion object {
        const val MOTHER = "mother"
    }

    private val mother by lazy {
        getExtraAs(Dog::class.java, MOTHER)
    }

    override fun SheetBreedFailedBinding.setViews() {
        val momCurrentFertility = mother.getCurrentFertilityRate(Constant.gameTime)
        tvBredDetail.text = buildString {
            append("Breeding unsuccessful.\n")
            append("${mother.name}'s fertility rate: ")
            append(mother.getFertilityCategory())
            append(" (${String.format(Locale.US, "%.1f%%", momCurrentFertility * 100)})")
        }
    }
}