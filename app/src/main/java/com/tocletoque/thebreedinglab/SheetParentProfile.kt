package com.tocletoque.thebreedinglab

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tocletoque.thebreedinglab.databinding.SheetParentProfileBinding
import com.tocletoque.thebreedinglab.model.Dog

class SheetParentProfile: BottomSheetDialogFragment(R.layout.sheet_parent_profile) {

    var father: Dog? = null
    var mother: Dog? = null

    private lateinit var binding: SheetParentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SheetParentProfileBinding.bind(view)
        binding.fatherProfile.text = father?.getDetail()
        binding.motherProfile.text = mother?.getDetail()
    }
}