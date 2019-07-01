package com.ycz.lanhome

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.layout_level_view.view.*

class LevelView : FrameLayout {
    var titleText: String
        get() = titleTextLevelView.text.toString()
        set(value) {
            titleTextLevelView.text = value
        }
    var currentLevel: Int
        get() = seekbarLevelView.progress
        set(value) {
            seekbarLevelView.progress = value
        }

    var maxLevel: Int
        get() = seekbarLevelView.max
        set(value) {
            seekbarLevelView.max = value
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.layout_level_view, this)
        val array = context.obtainStyledAttributes(attrs, R.styleable.LevelView)
        titleText = array.getString(R.styleable.LevelView_titleText) ?: ""
        currentLevel = array.getInt(R.styleable.LevelView_currentLevel, 0)
        maxLevel = array.getInt(R.styleable.LevelView_maxLevel, 0)
        array.recycle()
    }

    fun setOnSeekBarChangeListener(listener: SeekBar.OnSeekBarChangeListener) {
        seekbarLevelView.setOnSeekBarChangeListener(listener)
    }
}