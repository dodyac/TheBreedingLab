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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.tocletoque.thebreedinglab.common.Constant
import com.tocletoque.thebreedinglab.common.PrefManager
import com.tocletoque.thebreedinglab.common.State
import com.tocletoque.thebreedinglab.common.calculateDilutePattern
import com.tocletoque.thebreedinglab.common.calculateEpigeneticTrait
import com.tocletoque.thebreedinglab.databinding.ActivityMainBinding
import com.tocletoque.thebreedinglab.isGenesis
import com.tocletoque.thebreedinglab.model.Dog
import com.tocletoque.thebreedinglab.model.Reputation
import com.tocletoque.thebreedinglab.model.Sex
import com.tocletoque.thebreedinglab.model.areFullSiblings
import com.tocletoque.thebreedinglab.model.areHalfSiblings
import com.tocletoque.thebreedinglab.model.isParentChild
import com.tocletoque.thebreedinglab.model.registerDog
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
                    ),
                    Extra(
                        SheetPuppyDetail.PUPPIES_NAME,
                        puppies.map { it.name }.json
                    )
                )
                sheetPuppyDetail.setOnSheetListener(object : SheetPuppyDetail.OnSheetListener {
                    override fun onNameChanged(newName: String) {
                        puppies.find { it == item }?.name = newName
                        registerDog(item)
                        binding.rvPuppies.adapter?.notifyItemChanged(position)
                    }
                })
                sheetPuppyDetail.show(supportFragmentManager)
            }
        })
    }
    private var dogs = Constant.dogList.filter {
        it.unlockedAt == Reputation.NoviceBreeder
    }.toMutableList()

    private val player by lazy {
        State(this).getPlayer()
    }
    private val puppies = mutableListOf<Dog>()

    private val prefManager by lazy {
        PrefManager(this)
    }
    private var optInPuppyTraining = true
    private var optInGrooming = true

    override fun doFetch() {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        puppies.addAll(State(this).getPuppies())

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0 // disable cache for development
        }
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val isPaid = firebaseRemoteConfig.getBoolean("is_paid")
                    if (!isPaid) {
                        finish()
                    }
                } else {
                    finish()
                }
            }
            .addOnFailureListener {
                finish()
            }
        Constant.dogList.forEach {
            registerDog(it)
        }
        puppies.forEach {
            registerDog(it)
        }
        puppyAdapter.setAdapterList(puppies)
    }

    override fun ActivityMainBinding.setViews() {
        rvPuppies.setVStack(puppyAdapter)
        updateUI()

        val isGuideShown = prefManager.isShown(PrefManager.Type.Welcome)

        if (!isGuideShown) {
            val sheetWelcomeToTheGame = SheetWelcomeToTheGame()
            sheetWelcomeToTheGame.setOnSheetListener(object : SheetWelcomeToTheGame.OnSheetListener {
                override fun onBasicControl() {
                    val sheetBasicControl = SheetBasicControl()
                    sheetBasicControl.show(supportFragmentManager)
                }
            })
            sheetWelcomeToTheGame.show(supportFragmentManager)
            prefManager.show(PrefManager.Type.Welcome)
        }
    }

    private fun configureParents() {
        val moms = dogs.filter { it.sex == Sex.Female }.map { it.name.plus(" ($${it.price})")  }
        val dads = dogs.filter { it.sex == Sex.Male }.map { it.name.plus(" ($${it.price})") }

        binding.tilMother.materialAutoComplete?.set(moms)
        binding.tilFather.materialAutoComplete?.set(dads)
    }

    override fun ActivityMainBinding.doAction() {
        btnRestart.setOnClickListener {
            prefManager.reset()
            State(this@MainActivity).restart()
            recreate()
        }
        btnBreed.setOnClickListener {
            val momName = tilMother.string.substringBefore(" ($")
            val dadName = tilFather.string.substringBefore(" ($")

            val mom = dogs.find { it.name == momName }
            val dad = dogs.find { it.name == dadName }

            if (puppies.size >= player.getMaximumPuppies()) {
                toast("You have reached the maximum number of puppies!")
                return@setOnClickListener
            }
            if (mom != null && dad != null) {
                // Inbreeding checks
                if (isParentChild(mom, dad)) {
                    toast("Breeding between parent and child is not allowed.")
                    return@setOnClickListener
                }
                if (areFullSiblings(mom, dad)) {
                    toast( "Breeding between full siblings is not allowed.")
                    return@setOnClickListener
                }
                if (areHalfSiblings(mom, dad)) {
                    toast("Breeding between half siblings is not allowed.")
                    return@setOnClickListener
                }

                val cost = mom.price + dad.price
                val canSpend = player.spendCheck(cost)
                if (canSpend) {
                    // Check breeding success based on mother's season-adjusted fertility rate
                    val momCurrentFertility = mom.getCurrentFertilityRate(Constant.gameTime)
                    if (Random.nextDouble() > momCurrentFertility) {
                        val sheetBreedFailed = SheetBreedFailed()
                        sheetBreedFailed.putExtras(
                            Extra(
                                SheetParentProfile.MOTHER,
                                mom.json
                            )
                        )
                        sheetBreedFailed.show(supportFragmentManager)
                        return@setOnClickListener
                    }
                    if (player.spend(cost)) {
                        puppies.addAll(makePuppy(mom, dad, momCurrentFertility))
                        val repGain = player.calculateBreedingReputation(puppies)
                        val (gainedTitle, newTitle) = player.addReputation(repGain)
                        player.totalPuppiesBred += puppies.size

                        val sheetBreedingSuccess = SheetBreedingSuccess()

                        sheetBreedingSuccess.putExtras(
                            Extra(
                                key = SheetBreedingSuccess.MOTHER,
                                value = mom.json
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
                            prefManager.show(PrefManager.Type.FirstLitter)
                        }
                        updateUI()

                        puppyAdapter.setAdapterList(puppies)
                    } else {
                        toast("Not enough money! Breeding costs $$cost")
                    }
                } else {
                    toast("Not enough money! Breeding costs $$cost")
                }
            } else {
                toast("Please select both parents!")
            }
        }
        btnPuppyTraining.setOnClickListener {
            toggleTraining()
        }
        btnGrooming.setOnClickListener {
            toggleGrooming()
        }
        btnParentProfiles.setOnClickListener {
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
        btnLitterSummary.setOnClickListener {
            val sheetLitterSummary = SheetLitterSummary()
            sheetLitterSummary.putExtras(
                Extra(
                    SheetLitterSummary.PUPPIES,
                    puppies.json
                )
            )
            sheetLitterSummary.show(supportFragmentManager)
        }
        btnPlayerStats.setOnClickListener {
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
        btnAdvanceSeason.setOnClickListener {
            advanceSeason()
        }
    }

    private fun toggleTraining() {
        optInPuppyTraining = !optInPuppyTraining
        binding.btnPuppyTraining.text = "Puppy Training: " + if (optInPuppyTraining) "ON" else "OFF"
    }

    private fun toggleGrooming() {
        optInGrooming = !optInGrooming
        binding.btnGrooming.text = "Grooming: " + if (optInGrooming) "ON" else "OFF"
    }

    private fun filterDogsByReputation(): MutableList<Dog> {
        val currentReputation = Reputation.entries.find {
            it.displayName == player.getReputationTitle()
        } ?: Reputation.NoviceBreeder
        return Constant.dogList.filter { it.unlockedAt.ordinal <= currentReputation.ordinal }.toMutableList()
    }

    private fun updateUI() {
        dogs = filterDogsByReputation()
        configureParents()
        binding.tvMoney.text = "Money: $${player.money}"
        binding.tvReputation.text = "Reputation: ${player.reputation} - ${player.getReputationTitle()}"
        binding.tvSeason.text = "Season: ${Constant.gameTime.seasonName} | Year ${Constant.gameTime.year}"
        State(this@MainActivity).savePuppies(puppies)
        State(this@MainActivity).savePlayer(player)
    }

    fun generateUniqueName(): String {
        val usedNames = puppies.map { it.name }
        val letters = ('A'..'Z')
        var name: String
        do {
            name = "Pup-" + (1..3).map { letters.random() }.joinToString("")
        } while (name in usedNames) // regenerate if already used
        return name
    }

    private fun makePuppy(mom: Dog, dad: Dog, momCurrentFertility: Double): List<Dog> {
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

        val baseLitterSize = Random.nextInt(5, 11)
        // Adjust litter size based on mother's season-adjusted fertility
        val litterSize = when {
            momCurrentFertility >= 0.95 -> {
                baseLitterSize + Random.nextInt(0, 2) // upper bound is exclusive → 0 or 1
            }
            momCurrentFertility < 0.90 -> {
                maxOf(2, baseLitterSize - Random.nextInt(0, 3)) // 0,1,2
            }
            else -> baseLitterSize
        }
        val tempPuppies = (1..litterSize).map {
            val name = generateUniqueName()
            val sex = Sex.entries.random()
            val birthday = LocalDate.now()
//          Genetic inheritance
            val B = breedGenotype(mom.B, dad.B)
            val E = breedGenotype(mom.E, dad.E)
            val D = breedGenotype(mom.D, dad.D)
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

            fun getBaseCoatColor(): String {
                val base = when {
                    E == "ee" -> "Yellow"
                    B == "bb" -> "Brown"
                    else -> "Black"
                }

                // Apply dilution only if dd
                return when {
                    D == "dd" && base == "Yellow" -> "Champagne"
                    D == "dd" && base == "Brown" -> "Lilac"
                    D == "dd" && base == "Black" -> "Blue"
                    else -> base
                } + " coat"
            }

            val hasDilute = player.calculateDilutePattern(
                parent1HasDilute = mom.hasDilute,
                parent2HasDilute = dad.hasDilute,
                coatColor = getBaseCoatColor()
            )

            val puppy = Dog(
                name = name,
                sex = sex,
                birthday = birthday,
                B = B,
                E = E,
                D = D,
                tail = tail,
                pra = pra,
                eic = eic,
                hnpk = hnpk,
                cnm = cnm,
                sd2 = sd2,
                temperament = temperament,
                trainability = trainability,
                sociability = sociability,
                hasDilute = hasDilute,
                mother = mom.name,
                father = dad.name
            )

            puppy
        }
        // Apply survivability rate - some puppies may not survive
        val survivedPuppies = tempPuppies.filter {
            Random.nextDouble() <= mom.survivabilityRate
        }.toMutableList()

        // Ensure at least one puppy survives if any were born
        if (tempPuppies.isNotEmpty() && survivedPuppies.isEmpty()) {
            survivedPuppies.add(tempPuppies.first())
        }
        survivedPuppies.forEachIndexed { index, puppy ->
            val name = if (index == 0 && isFirstLitter) {
                "Genesis"
            } else {
                generateUniqueName()
            }
            puppy.name = name
            registerDog(puppy)
        }

        return survivedPuppies
    }

    private fun breedGenotype(mom: String, dad: String): String {
        val alleles = listOf(
            mom.random(),
            dad.random()
        ).sortedBy { it.isLowerCase() }
        return alleles.joinToString("")
    }

    private fun advanceSeason() {
        // 1) Quarterly expenses
        val numDogs = dogs.size
        val numPuppies = puppies.size
        var totalExpense = numDogs * Constant.FOOD_COST_PER_DOG
        if (optInPuppyTraining) {
            totalExpense += numPuppies * Constant.PUPPY_TRAINING_COST_PER_PUPPY
        }
        if (optInGrooming) {
            totalExpense += numDogs * Constant.GROOMING_COST_PER_DOG
        }
        if (totalExpense > 0) {
            val canSpend = player.spendCheck(totalExpense)
            if (canSpend) {
                player.spend(totalExpense)

            } else {
                toast("Not enough money!")
                return
            }
        }
        // 2 Advance time
        Constant.gameTime.advance()

        // 3) Age progression and discovery for all dogs
        dogs.forEach { it.advanceSeason() }
        puppies.forEach { it.advanceSeason() }

        // 4) Random events
        val events = mutableListOf<String>()
        val rng = Random

        // Spontaneous loss
        if (rng.nextDouble() < 0.03 && dogs.isNotEmpty()) {
            val lost = dogs.random()
            events.add("Loss: ${lost.name} passed away due to illness/old age.")
            dogs.remove(lost)
            // pedigree history is preserved
        }

        // Unexpected costs/gains/reputation (20% chance)
        if (rng.nextDouble() < 0.20) {
            val roll = rng.nextDouble()
            when {
                roll < 0.33 -> {
                    val cost = rng.nextInt(80, 201)
                    player.spend(cost)
                    events.add("Unexpected cost: vacation dog sitter needed this season (-$$cost).")
                }
                roll < 0.66 -> {
                    val gain = rng.nextInt(100, 251)
                    player.earn(gain)
                    events.add("Side income: you pet-sat this season (+$$gain).")
                }
                else -> {
                    val rep = rng.nextInt(10, 31)
                    player.reputation += rep
                    events.add("Reputation boost: sold to an influencer who promoted you (+$rep rep).")
                }
            }
        }

        // 5) Seasonal events
        when (Constant.gameTime.seasonName) {
            "Spring" -> if (rng.nextDouble() < 0.5) {
                val reward = rng.nextInt(150, 401)
                val rep = rng.nextInt(20, 41)
                player.earn(reward)
                player.reputation += rep
                events.add("Spring Dog Show: You placed well! +$$reward, +$rep reputation.")
            }
            "Summer" -> if (rng.nextDouble() < 0.4) {
                val reward = rng.nextInt(100, 301)
                val rep = rng.nextInt(10, 26)
                player.earn(reward)
                player.reputation += rep
                events.add("Summer Breeding Challenge: Solid results! +$$reward, +$rep reputation.")
            }
            "Fall" -> if (rng.nextDouble() < 0.35) {
                val reward = rng.nextInt(100, 251)
                val rep = rng.nextInt(10, 21)
                player.earn(reward)
                player.reputation += rep
                events.add("Fall Working Trials: Good showing! +$$reward, +$rep reputation.")
            }
            "Winter" -> if (rng.nextDouble() < 0.30) {
                val reward = rng.nextInt(80, 181)
                val rep = rng.nextInt(8, 19)
                player.earn(reward)
                player.reputation += rep
                events.add("Winter Indoor Show: Minor prize! +$$reward, +$rep reputation.")
            }
        }

        // 6) Update displays
        updateUI()

        // 7) Compose summary popup
        val summaryLines = mutableListOf(
            "Season advanced to ${Constant.gameTime.seasonName}, Year ${Constant.gameTime.year}",
            "Expenses this season: $$totalExpense"
        )
        if (events.isNotEmpty()) {
            summaryLines.add("\nEvents:")
            summaryLines.addAll(events)
        }

        val sheetSeasonAdvance = SheetSeasonAdvance()
        sheetSeasonAdvance.putExtras(
            Extra(
                SheetSeasonAdvance.NOTE,
                summaryLines.joinToString("\n")
            )
        )
        sheetSeasonAdvance.show(supportFragmentManager)
    }
}