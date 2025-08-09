package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseAdapter
import com.acxdev.commonFunction.common.base.BaseSheet
import com.acxdev.commonFunction.utils.ext.view.setVStack
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.tocletoque.thebreedinglab.databinding.ItemPuppySaleBinding
import com.tocletoque.thebreedinglab.databinding.SheetPuppyPriceMenuBinding
import com.tocletoque.thebreedinglab.dp
import com.tocletoque.thebreedinglab.model.Dog
import java.time.LocalDate

class SheetPuppyPriceMenu: BaseSheet<SheetPuppyPriceMenuBinding>() {

    companion object {
        const val PUPPIES = "puppies"
    }
    private var onSheetListener: OnSheetListener? = null

    private val puppies by lazy {
        val jsonString = arguments?.getString(PUPPIES) ?: "[]"

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
                LocalDate.parse(json.asString) // for "2025-08-09"
            })
            .create()

        gson.fromJson<List<Dog>>(jsonString, object : TypeToken<List<Dog>>() {}.type)
    }

    private val adapterPuppyPrice by lazy {
        AdapterPuppyPrice(object : OnClick {
            override fun onSellClick(item: Dog, position: Int) {
                onSheetListener?.onSellPuppy(item)
                dismiss()
            }
        })
    }

    override fun SheetPuppyPriceMenuBinding.setViews() {
        val totalPrice = puppies.sumOf { it.calculatePuppyPrice() }
        adapterPuppyPrice.setAdapterList(puppies)
        rvPuppies.apply {
            setVStack(adapterPuppyPrice)
            setMaxHeight(300.dp)
        }
        tvTotalLitterValue.text = "Total Litter Value: $${totalPrice}"
    }

    override fun SheetPuppyPriceMenuBinding.doAction() {
        binding.btnSellAll.setOnClickListener {
            onSheetListener?.onSellAll()
            dismiss()
        }
    }

    override fun onDestroyView() {
        onSheetListener = null
        super.onDestroyView()
    }

    fun setOnSheetListener(listener: OnSheetListener) {
        this.onSheetListener = listener
    }

    inner class AdapterPuppyPrice(
        private val onClick: OnClick
    ): BaseAdapter<ItemPuppySaleBinding, Dog>() {

        override fun ItemPuppySaleBinding.setViews(
            item: Dog,
            position: Int
        ) {
            tvName.text = item.name
            tvDetail.text = "${item.sex} | Wellness: ${item.wellnessScore}"
            tvPrice.text = "Price: $${item.calculatePuppyPrice()}"
            btnSell.setOnClickListener {
                onClick.onSellClick(item, position)
            }
        }
    }

    interface OnClick {
        fun onSellClick(item: Dog, position: Int)
    }

    interface OnSheetListener {
        fun onSellPuppy(puppy: Dog)
        fun onSellAll()
    }
}