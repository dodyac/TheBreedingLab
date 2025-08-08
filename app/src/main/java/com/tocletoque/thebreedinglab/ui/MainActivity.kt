package com.tocletoque.thebreedinglab.ui

import android.widget.TextView
import com.acxdev.commonFunction.common.base.BaseActivity
import com.acxdev.commonFunction.model.Extra
import com.acxdev.commonFunction.utils.ext.putExtras
import com.acxdev.commonFunction.utils.ext.view.json
import com.acxdev.commonFunction.utils.ext.view.materialAutoComplete
import com.acxdev.commonFunction.utils.ext.view.set
import com.acxdev.commonFunction.utils.ext.view.setVStack
import com.acxdev.commonFunction.utils.ext.view.string
import com.acxdev.commonFunction.utils.toast
import com.tocletoque.thebreedinglab.common.Constant
import com.tocletoque.thebreedinglab.R
import com.tocletoque.thebreedinglab.databinding.ActivityMainBinding
import com.tocletoque.thebreedinglab.model.Dog
import com.tocletoque.thebreedinglab.model.Player
import com.tocletoque.thebreedinglab.model.Sex
import java.time.LocalDate
import kotlin.random.Random

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val puppyAdapter by lazy {
        PuppyAdapter(object : PuppyAdapter.OnClick {
            override fun onItemClick(item: Dog, position: Int) {
                val sheetPuppyDetail = SheetPuppyDetail()
                sheetPuppyDetail.putExtras(
                    Extra(
                        SheetPuppyDetail.PUPPY,
                        item.json
                    )
                )
                sheetPuppyDetail.show(supportFragmentManager)
            }
        })
    }
    private val dogs by lazy {
        Constant.dogList
    }
    private val player by lazy {
        Player()
    }
    private val puppies = mutableListOf<Dog>()

    override fun ActivityMainBinding.setViews() {
        val moms = dogs.filter { it.sex == Sex.Female }.map { it.name.plus(" ($${it.price})")  }
        val dads = dogs.filter { it.sex == Sex.Male }.map { it.name.plus(" ($${it.price})") }

        tilMother.materialAutoComplete?.set(moms)
        tilFather.materialAutoComplete?.set(dads)

        updateUI()
    }

    override fun ActivityMainBinding.doAction() {
        binding.btnBreed.setOnClickListener {
            val momName = tilMother.string.substringBefore(" ($")
            val dadName = tilFather.string.substringBefore(" ($")

            val mom = dogs.find { it.name == momName }
            val dad = dogs.find { it.name == dadName }

            if (mom != null && dad != null) {
                val cost = mom.price + dad.price
                if (player.spend(cost)) {
                    puppies.addAll(makePuppy(mom, dad))
                    val repGain = player.calculateBreedingReputation(puppies)
                    val (gainedTitle, newTitle) = player.addReputation(repGain)
                    player.totalPuppiesBred += puppies.size

                    toast("Breeding Success", false)
                    toast("You Bred $momName and $dadName for $$cost", false)
                    toast("Gained $repGain Reputation Points!", false)
                    if (gainedTitle) {
                        toast("CONGRATULATIONS! You've reached a new title: $newTitle", false)
                    }
//                    Toast.makeText(this, "Bred ${puppies.size} puppies!", Toast.LENGTH_SHORT).show()
                    updateUI()

                    puppyAdapter.setAdapterList(puppies)
                    rvPuppies.setVStack(puppyAdapter)
                } else {
                    toast("Not enough money! Breeding costs $$cost")
                }
            } else {
                toast("Please select both parents!")
            }
        }

        binding.btnParentProfiles.setOnClickListener {
            val momName = tilMother.string.substringBefore(" ($")
            val dadName = tilFather.string.substringBefore(" ($")

            val mom = dogs.find { it.name == momName }
            val dad = dogs.find { it.name == dadName }

            if (mom != null && dad != null) {
                val sheetParentProfile = SheetParentProfile()
                sheetParentProfile.putExtras(
                    Extra(
                        SheetParentProfile.MOTHER,
                        mom.json
                    ),
                    Extra(
                        SheetParentProfile.FATHER,
                        dad.json
                    )
                )
                sheetParentProfile.show(supportFragmentManager)
            } else {
                toast("Please select both parents to view their profiles.")
            }
        }

        binding.btnLitterSummary.setOnClickListener {
            val sheetLitterSummary = SheetLitterSummary()
            sheetLitterSummary.putExtras(
                Extra(
                    SheetLitterSummary.PUPPIES,
                    puppies.json
                )
            )
            sheetLitterSummary.show(supportFragmentManager)
        }

        binding.btnPlayerStats.setOnClickListener {
            val sheetPlayerStatistics = SheetPlayerStatistics()
            sheetPlayerStatistics.putExtras(
                Extra(
                    SheetPlayerStatistics.PLAYER,
                    player.json
                )
            )
            sheetPlayerStatistics.show(supportFragmentManager)
        }
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.tvMoney).text = "Money: $${player.money}"
        findViewById<TextView>(R.id.tvReputation).text = "Reputation: ${player.reputation} - ${player.getReputationTitle()}"
    }

    private fun makePuppy(mom: Dog, dad: Dog): List<Dog> {
        return (1..Random.Default.nextInt(4, 10)).map {
            val name = "Pup $it"
            val sex = Sex.entries.random()
            Dog(
                name, sex, LocalDate.now(),
                breedGenotype(mom.B, dad.B),
                breedGenotype(mom.E, dad.E),
                breedGenotype(mom.tail, dad.tail),
                breedGenotype(mom.pra, dad.pra),
                breedGenotype(mom.eic, dad.eic),
                breedGenotype(mom.hnpk, dad.hnpk),
                breedGenotype(mom.cnm, dad.cnm),
                breedGenotype(mom.sd2, dad.sd2),
                1000
            )
        }
    }

    private fun breedGenotype(mom: String, dad: String): String {
        val alleles = listOf(
            mom.random(),
            dad.random()
        ).sortedBy { it.isLowerCase() }
        return alleles.joinToString("")
    }
}