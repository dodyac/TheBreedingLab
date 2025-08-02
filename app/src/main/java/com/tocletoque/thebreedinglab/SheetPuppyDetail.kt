package com.tocletoque.thebreedinglab

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tocletoque.thebreedinglab.databinding.SheetPuppyDetailBinding
import com.tocletoque.thebreedinglab.model.Dog

class SheetPuppyDetail: BottomSheetDialogFragment(R.layout.sheet_puppy_detail) {

    var puppy: Dog? = null

    private lateinit var binding: SheetPuppyDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SheetPuppyDetailBinding.bind(view)
        binding.puppy.text = puppy?.getDetail()
    }
}