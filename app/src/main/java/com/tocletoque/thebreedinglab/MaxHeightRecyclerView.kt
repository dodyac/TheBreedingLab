package com.tocletoque.thebreedinglab

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class MaxHeightRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    private var mMaxHeight = -1

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var heightSpec = heightSpec
        val mode = MeasureSpec.getMode(heightSpec)
        val height = MeasureSpec.getSize(heightSpec)
        if (mMaxHeight >= 0 && (mode == MeasureSpec.UNSPECIFIED || height > mMaxHeight)) {
            heightSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    /**
     * Sets the maximum height for this recycler view.
     */
    fun setMaxHeight(maxHeight: Int) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight
            requestLayout()
        }
    }
}