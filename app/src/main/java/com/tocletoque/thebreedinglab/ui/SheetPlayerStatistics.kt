package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseSheet
import com.tocletoque.thebreedinglab.databinding.SheetPlayerStatitisticBinding
import com.tocletoque.thebreedinglab.model.Player

class SheetPlayerStatistics: BaseSheet<SheetPlayerStatitisticBinding>() {

    companion object {
        const val PLAYER = "player"
    }

    private val player by lazy {
        getExtraAs(Player::class.java, PLAYER)
    }

    override fun SheetPlayerStatitisticBinding.setViews() {
        tvMoney.text = "Money: $${player.money}"
        tvReputation.text = "Reputation: ${player.reputation}"
        tvCurrentTitle.text = "Current Title: ${player.getReputationTitle()}"

        tvNextTitle.text = "Next Title: ${player.getNextReputationMilestone()?.first}"
        tvPointNeeded.text = "Points Needed: ${player.getNextReputationMilestone()?.second}"

        tvTotalPuppiesBred.text = "Total Puppies Bred: ${player.totalPuppiesBred}"
        tvTotalPuppiesSold.text = "Total Puppies Sold: ${player.totalPuppiesSold}"
        if (player.totalPuppiesBred > 0) {
            val sellRate = (player.totalPuppiesSold / player.totalPuppiesBred) * 100
            tvSaleRate.text = "Sale Rate: $sellRate%"
        }

        tvReputationTitles.text = player.reputationMilestones.map { (points, title) ->
            val prefix = if (title.displayName == player.getReputationTitle()) "=> " else "     "
            "$prefix$title ($points pts)"
        }.joinToString("\n")
    }
}