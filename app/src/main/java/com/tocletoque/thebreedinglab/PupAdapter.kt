package com.tocletoque.thebreedinglab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tocletoque.thebreedinglab.databinding.ItemPuppyBinding
import com.tocletoque.thebreedinglab.model.Dog

class PuppyAdapter(
    private val onClick: OnClick
): ListAdapter<Dog, PuppyAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPuppyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pup = getItem(position)
        holder.bind(pup)
    }

    inner class ViewHolder(private val binding: ItemPuppyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(puppy: Dog) {
            binding.tvName.text = "${puppy.name} - ${puppy.sex}, Wellness: ${puppy.wellnessScore}"
            binding.root.setOnClickListener {
                onClick.onItemClick(puppy, adapterPosition)
            }
        }
    }

    interface OnClick {
        fun onItemClick(item: Dog, position: Int)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Dog>() {
            override fun areItemsTheSame(oldItem: Dog, newItem: Dog): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Dog, newItem: Dog): Boolean {
                return oldItem == newItem
            }
        }
    }
}