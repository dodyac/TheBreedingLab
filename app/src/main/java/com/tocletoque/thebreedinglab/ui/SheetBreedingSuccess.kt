package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.acxdev.commonFunction.utils.ext.view.visible
import com.tocletoque.thebreedinglab.databinding.SheetBreedingSuccessBinding

class SheetBreedingSuccess: BaseSheet<SheetBreedingSuccessBinding>() {

    companion object {
        const val MOM_NAME = "mom_name"
        const val DAD_NAME = "dad_name"
        const val COST = "cost"
        const val GAIN_REPUTATION = "gain_reputation"
        const val NEW_TITLE = "new_title"
    }

    private val momName by lazy {
        arguments?.getString(MOM_NAME, "-")
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
        tvBred.text = "You Bred $momName and $dadName for $$cost"
        tvGainReputation.text = "Gained $gainReputation Reputation Points!"

        newTitle?.let {
            tvCongrats.visible()
            tvCongrats.text = "CONGRATULATIONS! You've reached a new title: $newTitle"
        }
    }
}