package com.example.topsedtest.customs.textViews

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class DigitalTextView(context: Context, attrs: AttributeSet?) :
    AppCompatTextView(context, attrs) {
    init {
        val fontName = "fonts/digital.ttf"
        val typeface = Typeface.createFromAsset(context.assets, fontName)
        setTypeface(typeface)
    }
}