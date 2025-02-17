package com.niki.app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.niki.app.databinding.LayoutBarBinding

class BarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val layoutInflater = LayoutInflater.from(context)
    val binding = LayoutBarBinding.inflate(layoutInflater, this, true)

    init {
        post {
            binding.lifecycleOwner = this.findViewTreeLifecycleOwner()
        }
    }

    fun getTextView() = binding.textView
    fun getButton() = binding.backButton
}