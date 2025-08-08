package com.tocletoque.thebreedinglab.ui

import androidx.core.view.isVisible
import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.databinding.SheetLitterSummaryBinding
import com.tocletoque.thebreedinglab.model.Dog
import com.tocletoque.thebreedinglab.model.Sex

class SheetLitterSummary: BaseSheet<SheetLitterSummaryBinding>() {

    companion object {
        const val PUPPIES = "puppies"
    }

    private val puppies by lazy {
        getExtraAs(Array<Dog>::class.java, PUPPIES)
    }

    override fun SheetLitterSummaryBinding.setViews() {
        tvEmptyPuppies.isVisible = puppies.isEmpty()
        layoutPuppies.isVisible = puppies.isNotEmpty()

        tvTotalPuppies.text = "Total Puppies: ${puppies.size}"
        tvSexRatio.text = "Sex Ratio (Males:Females) = ${puppies.count { it.sex == Sex.Male }}:${puppies.count { it.sex == Sex.Female }}"
        tvYellowCoat.text = "Yellow Coat: ${puppies.count { it.isYellowCoat }}"

        val longTail = "Long tail"
        val shortTail = "Short tail"
        tvTailTypes.text = "Tail Types: ${puppies.count { it.tailLength == longTail }} ($longTail), ${puppies.count { it.tailLength == shortTail }} ($shortTail)"

        tvPRA.text = formatHealthText("PRA", "pra") ?: ""
        tvEIC.text = formatHealthText("EIC", "eic") ?: ""
        tvHNPK.text = formatHealthText("HNPK", "hnpk") ?: ""
        tvCNM.text = formatHealthText("CNM", "cnm") ?: ""
        tvSD2.text = formatHealthText("SD2", "sd2") ?: ""
    }

    private fun formatHealthText(label: String, key: String): String? {
        val carrier = "Carrier"
        val affected = "Affected"

        val carrierCount = puppies.count { it.healthStatuses[key] == carrier }
        val affectedCount = puppies.count { it.healthStatuses[key] == affected }

        val parts = mutableListOf<String>()
        if (carrierCount > 0) parts.add("$carrierCount ($carrier)")
        if (affectedCount > 0) parts.add("$affectedCount ($affected)")

        return if (parts.isNotEmpty()) {
            "$label: ${parts.joinToString(", ")}"
        } else {
            null
        }
    }
}