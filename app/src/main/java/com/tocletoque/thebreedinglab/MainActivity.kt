package com.tocletoque.thebreedinglab

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.tocletoque.thebreedinglab.databinding.ActivityMainBinding
import com.tocletoque.thebreedinglab.model.Dog
import com.tocletoque.thebreedinglab.model.Player
import java.time.LocalDate
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var player: Player
    private lateinit var dogs: List<Dog>

    private val fatherAutoComplete by lazy {
        binding.tilFather.editText as? MaterialAutoCompleteTextView
    }

    private val motherAutoComplete by lazy {
        binding.tilMother.editText as? MaterialAutoCompleteTextView
    }

    private val puppyAdapter by lazy {
        PuppyAdapter(object : PuppyAdapter.OnClick {
            override fun onItemClick(item: Dog, position: Int) {
                val sheetPuppyDetail = SheetPuppyDetail()
                sheetPuppyDetail.puppy = item
                sheetPuppyDetail.show(supportFragmentManager, "SheetPuppyDetail")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = Player()
        dogs = Constant.dogList

        val moms = dogs.filter { it.sex == "Female" }.map { it.name }
        val dads = dogs.filter { it.sex == "Male" }.map { it.name }

        motherAutoComplete?.apply {
            val dataAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, moms)
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            setAdapter(dataAdapter)
        }
        fatherAutoComplete?.apply {
            val dataAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, dads)
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            setAdapter(dataAdapter)
        }

        binding.btnBreed.setOnClickListener {
            val momName = motherAutoComplete?.text.toString()
            val dadName = fatherAutoComplete?.text.toString()

            val mom = dogs.find { it.name == momName }
            val dad = dogs.find { it.name == dadName }

            if (mom != null && dad != null) {
                val cost = mom.price + dad.price
                if (player.spend(cost)) {
                    val puppies = makePuppy(mom, dad)
                    val repGain = player.calculateBreedingReputation(puppies)
                    val (gainedTitle, newTitle) = player.addReputation(repGain)
                    player.totalPuppiesBred += puppies.size

                    Toast.makeText(this, "Breeding Success",Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "You Bred $momName and $dadName for $$cost", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Gained $repGain Reputation Points!", Toast.LENGTH_SHORT).show()
                    if (gainedTitle) {
                        Toast.makeText(this, "CONGRATULATIONS! You've reached a new title: $newTitle", Toast.LENGTH_SHORT).show()
                    }
//                    Toast.makeText(this, "Bred ${puppies.size} puppies!", Toast.LENGTH_SHORT).show()
                    updateUI()

                    puppyAdapter.submitList(puppies)
                    binding.puppies.apply {
                        layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                        adapter = puppyAdapter
                        setHasFixedSize(true)
                    }
                } else {
                    Toast.makeText(this, "Not enough money! Breeding costs $$cost", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select both parents!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnParentProfiles.setOnClickListener {
            val momName = motherAutoComplete?.text.toString()
            val dadName = fatherAutoComplete?.text.toString()

            val mom = dogs.find { it.name == momName }
            val dad = dogs.find { it.name == dadName }

            if (mom != null && dad != null) {
                val sheetParentProfile = SheetParentProfile()
                sheetParentProfile.father = dad
                sheetParentProfile.mother = mom
                sheetParentProfile.show(supportFragmentManager, "SheetParentProfile")
            } else {
                Toast.makeText(this, "Please select both parents to view their profiles.",Toast.LENGTH_SHORT).show()
            }
        }

        updateUI()
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.tvMoney).text = "Money: $${player.money}"
        findViewById<TextView>(R.id.tvReputation).text = "Reputation: ${player.reputation} - ${player.getReputationTitle()}"
    }

    private fun makePuppy(mom: Dog, dad: Dog): List<Dog> {
        return (1..Random.nextInt(4, 10)).map {
            val name = "Pup $it"
            val sex = listOf("Male", "Female").random()
            Dog(name, sex, LocalDate.now(),
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