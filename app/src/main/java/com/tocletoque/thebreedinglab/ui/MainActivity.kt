package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseActivity
import com.acxdev.commonFunction.model.Extra
import com.acxdev.commonFunction.utils.ext.putExtras
import com.acxdev.commonFunction.utils.ext.view.json
import com.acxdev.commonFunction.utils.ext.view.materialAutoComplete
import com.acxdev.commonFunction.utils.ext.view.set
import com.acxdev.commonFunction.utils.ext.view.setVStack
import com.acxdev.commonFunction.utils.ext.view.string
import com.acxdev.commonFunction.utils.toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.tocletoque.thebreedinglab.common.Constant
import com.tocletoque.thebreedinglab.common.PrefManager
import com.tocletoque.thebreedinglab.common.calculateDilutePattern
import com.tocletoque.thebreedinglab.common.calculateEpigeneticTrait
import com.tocletoque.thebreedinglab.databinding.ActivityMainBinding
import com.tocletoque.thebreedinglab.isGenesis
import com.tocletoque.thebreedinglab.model.Dog
import com.tocletoque.thebreedinglab.model.Player
import com.tocletoque.thebreedinglab.model.Sex
import com.tocletoque.thebreedinglab.ui.tutorial.SheetBasicControl
import com.tocletoque.thebreedinglab.ui.tutorial.SheetFirstLitter
import com.tocletoque.thebreedinglab.ui.tutorial.SheetWelcomeToTheGame
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
                sheetPuppyDetail.setOnSheetListener(object : SheetPuppyDetail.OnSheetListener {
                    override fun onNameChanged(newName: String) {
                        puppies.find { it == item }?.name = newName
                        binding.rvPuppies.adapter?.notifyItemChanged(position)
                    }
                })
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

    private val prefManager by lazy {
        PrefManager(this)
    }

    override fun ActivityMainBinding.setViews() {
        val moms = dogs.filter { it.sex == Sex.Female }.map { it.name.plus(" ($${it.price})")  }
        val dads = dogs.filter { it.sex == Sex.Male }.map { it.name.plus(" ($${it.price})") }

        tilMother.materialAutoComplete?.set(moms)
        tilFather.materialAutoComplete?.set(dads)

        updateUI()

//        val isGuideShown = prefManager.isShown(PrefManager.Type.Welcome)

        val isGuideShown = false

        if (!isGuideShown) {
            val sheetWelcomeToTheGame = SheetWelcomeToTheGame()
            sheetWelcomeToTheGame.setOnSheetListener(object : SheetWelcomeToTheGame.OnSheetListener {
                override fun onBasicControl() {
                    val sheetBasicControl = SheetBasicControl()
                    sheetBasicControl.show(supportFragmentManager)
                }
            })
            sheetWelcomeToTheGame.show(supportFragmentManager)
        }
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

                    val sheetBreedingSuccess = SheetBreedingSuccess()

                    sheetBreedingSuccess.putExtras(
                        Extra(
                            key = SheetBreedingSuccess.MOM_NAME,
                            value = momName
                        ),
                        Extra(
                            key = SheetBreedingSuccess.DAD_NAME,
                            value = dadName
                        ),
                        Extra(
                            key = SheetBreedingSuccess.COST,
                            value = cost.toString()
                        ),
                        Extra(
                            key = SheetBreedingSuccess.GAIN_REPUTATION,
                            value = repGain.toString()
                        )
                    )
                    // Only add NEW_TITLE if gainedTitle == true
                    if (gainedTitle) {
                        sheetBreedingSuccess.putExtras(
                            Extra(SheetBreedingSuccess.NEW_TITLE, newTitle)
                        )
                    }
//                    toast("Breeding Success", false)
//                    toast("You Bred $momName and $dadName for $$cost", false)
//                    toast("Gained $repGain Reputation Points!", false)
//                    if (gainedTitle) {
//                        toast("CONGRATULATIONS! You've reached a new title: $newTitle", false)
//                    }
//                    toast("Bred ${puppies.size} puppies!", false)

                    sheetBreedingSuccess.show(supportFragmentManager)
                    val isFirstLitter = !prefManager.isShown(PrefManager.Type.FirstLitter)
                    if (isFirstLitter) {
                        val sheetFirstLitter = SheetFirstLitter()
                        sheetFirstLitter.show(supportFragmentManager)
                    }
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
        btnPriceMenu.setOnClickListener {
            if (puppies.isEmpty()) {
                toast("You need to breed some dogs first")
                return@setOnClickListener
            }
            val gson = GsonBuilder()
                .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
                    JsonPrimitive(src.toString()) // serialize as "yyyy-MM-dd"
                })
                .create()

            val json = gson.toJson(puppies)
            val sheetPuppyPriceMenu = SheetPuppyPriceMenu()
            sheetPuppyPriceMenu.putExtras(
                Extra(
                    SheetPuppyPriceMenu.PUPPIES,
                    json
                )
            )
            sheetPuppyPriceMenu.setOnSheetListener(object : SheetPuppyPriceMenu.OnSheetListener {
                override fun onSellPuppy(puppy: Dog) {
                    val index = puppies.indexOf(puppy)
                    val puppyPrice = puppy.calculatePuppyPrice()
                    puppies.remove(puppy)
                    puppyAdapter.notifyItemRemoved(index)
                    val repGain = player.calculateSaleReputation(puppy, puppyPrice)
                    player.totalPuppiesSold += 1
                    val (gainedTitle, newTitle) = player.addReputation(repGain)
                    toast("Sold ${puppy.name} for $${puppyPrice}!", false)
                    toast("Gained $repGain Reputation Points!", false)
                    if (gainedTitle) {
                        toast("CONGRATULATIONS! You've reached a new title: $newTitle", false)
                    }
                    player.earn(puppyPrice)
                    updateUI()
                }

                override fun onSellAll() {
                    var totalEarned = 0
                    var totalRepGain = 0
                    val puppiesSell = puppies.filter { !it.name.isGenesis }
                    val numPuppies = puppiesSell.size

                    for (puppy in puppiesSell) {
                        val price = puppy.calculatePuppyPrice()
                        totalEarned += price
                        totalRepGain += player.calculateSaleReputation(puppy, price)
                    }
                    puppies.removeAll { it.name in puppiesSell.map { it.name } }
                    puppyAdapter.setAdapterList(puppies)
                    val (gainedTitle, newTitle) = player.addReputation(totalRepGain)
                    player.totalPuppiesSold += numPuppies
                    toast("Sold $numPuppies puppies for $${totalEarned}!", false)
                    toast("Gained $totalRepGain Reputation Points!", false)
                    if (gainedTitle) {
                        toast("CONGRATULATIONS! You've reached a new title: $newTitle", false)
                    }
                    player.earn(totalEarned)
                    updateUI()
                }
            })
            sheetPuppyPriceMenu.show(supportFragmentManager)
        }
    }

    private fun updateUI() {
        binding.tvMoney.text = "Money: $${player.money}"
        binding.tvReputation.text = "Reputation: ${player.reputation} - ${player.getReputationTitle()}"
    }

    private fun makePuppy(mom: Dog, dad: Dog): List<Dog> {
        val temperamentValues = listOf(
            "shy", "friendly", "reactive"
        )
        val trainabilityValues = listOf(
            "low", "medium", "high"
        )
        val sociabilityValues = listOf(
            "low", "medium", "high"
        )
        val isFirstLitter = !prefManager.isShown(PrefManager.Type.FirstLitter)

        return (1..Random.Default.nextInt(4, 10)).mapIndexed { index, it ->
            val name = if (index == 0 && isFirstLitter) {
                "Genesis"
            } else {
                "Pup $it"
            }
            val sex = Sex.entries.random()
            val birthday = LocalDate.now()
//          Genetic inheritance
            val B = breedGenotype(mom.B, dad.B)
            val E = breedGenotype(mom.E, dad.E)
            val tail = breedGenotype(mom.tail, dad.tail)
            val pra = breedGenotype(mom.pra, dad.pra)
            val eic = breedGenotype(mom.eic, dad.eic)
            val hnpk = breedGenotype(mom.hnpk, dad.hnpk)
            val cnm = breedGenotype(mom.cnm, dad.cnm)
            val sd2 = breedGenotype(mom.sd2, dad.sd2)

            val temperament = calculateEpigeneticTrait(
                parent1Trait = mom.temperament,
                parent2Trait = dad.temperament,
                traitValues = temperamentValues,
                heritability = 0.6
            )
            val trainability = calculateEpigeneticTrait(
                parent1Trait = mom.trainability,
                parent2Trait = dad.trainability,
                traitValues = trainabilityValues,
                heritability = 0.7
            )
            val sociability = calculateEpigeneticTrait(
                parent1Trait = mom.sociability,
                parent2Trait = dad.sociability,
                traitValues = sociabilityValues,
                heritability = 0.5
            )

            val baseCoatColor : String = when {
                E == "ee" -> "Yellow coat"
                B == "bb" -> "Brown coat"
                else -> "Black coat"
            }
            val hasDilute = calculateDilutePattern(
                parent1HasDilute = mom.hasDilute,
                parent2HasDilute = dad.hasDilute,
                coatColor = baseCoatColor
            )

            Dog(
                name = name,
                sex = sex,
                birthday = birthday,
                B = B,
                E = E,
                tail = tail,
                pra = pra,
                eic = eic,
                hnpk = hnpk,
                cnm = cnm,
                sd2 = sd2,
                temperament = temperament,
                trainability = trainability,
                sociability = sociability,
                hasDilute = hasDilute
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