package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.acxdev.commonFunction.utils.ext.view.visible
import com.tocletoque.thebreedinglab.common.Constant
import com.tocletoque.thebreedinglab.databinding.SheetBreedingSuccessBinding
import com.tocletoque.thebreedinglab.model.Dog
import java.util.Locale

class SheetBreedingSuccess: BaseSheet<SheetBreedingSuccessBinding>() {

    companion object {
        const val MOTHER = "mother"
        const val DAD_NAME = "dad_name"
        const val COST = "cost"
        const val GAIN_REPUTATION = "gain_reputation"
        const val NEW_TITLE = "new_title"
    }

    private val mother by lazy {
        getExtraAs(Dog::class.java, MOTHER)
    }

    private val dadName by lazy {
        arguments?.getString(DAD_NAME, "-")
    }

    private val cost by lazy {
        arguments?.getString(COST, "0")
    }

    private val gainReputation by lazy {
        arguments?.getString(GAIN_REPUTATION, "-")
    }

    private val newTitle by lazy {
        arguments?.getString(NEW_TITLE, null)
    }

    override fun SheetBreedingSuccessBinding.setViews() {
        val momCurrentFertility = mother.getCurrentFertilityRate(Constant.gameTime)
        tvBred.text = "You Bred ${mother.name} and $dadName for $$cost"
        tvMotherFertility.text = "Mother's Fertility: ${mother.getFertilityCategory()}  " +
                "(${String.format(Locale.US, "%.1f%%", momCurrentFertility * 100)})"
        tvSurvivability.text = "Survivability: ${mother.getSurvivabilityCategory()}  " +
            "(${String.format(Locale.US, "%.1f%%", mother.survivabilityRate * 100)})"
        tvGainReputation.text = "Gained $gainReputation Reputation Points!"

        newTitle?.let {
            tvCongrats.visible()
            tvCongrats.text = "CONGRATULATIONS! You've reached a new title: $newTitle"
        }
    }
}