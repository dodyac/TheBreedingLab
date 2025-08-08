package com.tocletoque.thebreedinglab.ui

import com.acxdev.commonFunction.common.base.BaseAdapter
import com.tocletoque.thebreedinglab.databinding.ItemPuppyBinding
import com.tocletoque.thebreedinglab.model.Dog

class PuppyAdapter(
    private val onClick: OnClick
): BaseAdapter<ItemPuppyBinding, Dog>() {

    override fun ItemPuppyBinding.setViews(
        item: Dog,
        position: Int
    ) {
        tvName.text = "${item.name} - ${item.sex}, Wellness: ${item.wellnessScore}"
        root.setOnClickListener {
            onClick.onItemClick(item, position)
        }
    }

    interface OnClick {
        fun onItemClick(item: Dog, position: Int)
    }
}