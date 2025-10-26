package com.niaouh.moodtracker.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.niaouh.moodtracker.R
import com.niaouh.moodtracker.data.CircleMoodBO
import com.niaouh.moodtracker.data.CircleMoodBO.NONE
import com.niaouh.moodtracker.data.CircleStateBO
import com.niaouh.moodtracker.data.CircleStateBO.*
import com.niaouh.moodtracker.utils.ResUtil
import kotlin.properties.Delegates

class MoodCircle : FrameLayout {

    var state: CircleStateBO by Delegates.observable(DEFAULT) { _, _, state ->
        changeState(state)
    }
    var mood: CircleMoodBO by Delegates.observable(NONE) { _, _, mood ->
        changeBackground(mood)
    }

    private val stateImageView: ImageView
    private val circleImageView: ImageView
    private val moodImageView: ImageView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_mood_circle, this)
        circleImageView = findViewById(R.id.circleImageView)
        stateImageView = findViewById(R.id.stateImageView)
        moodImageView = findViewById(R.id.moodImageView)
        setupStatePadding()
    }

    private fun setupStatePadding() {
        viewTreeObserver.addOnGlobalLayoutListener {
            stateImageView.layoutParams = stateImageView.layoutParams.apply {
                width = (circleImageView.width / 3) * 2
                height = (circleImageView.height / 3) * 2
            }
        }
    }

    private fun changeBackground(mood: CircleMoodBO) {
        circleImageView.setImageDrawable(ResUtil.getDrawable(context, mood.backgroundId))
        moodImageView.setImageDrawable(ResUtil.getDrawable(context, mood.moodDrawable))
    }

    private fun changeState(state: CircleStateBO) {
        when (state) {
            EDIT -> ResUtil.getDrawable(context, R.drawable.ic_edit)
            ADD -> ResUtil.getDrawable(context, R.drawable.ic_plus)
            DEFAULT -> ResUtil.getDrawable(context, R.drawable.ic_mood_none)
            CHOOSE_MOOD -> null
        }.also { stateDrawable ->
            stateImageView.setImageDrawable(stateDrawable)
            if (stateDrawable != null) moodImageView.visibility = View.GONE
            else moodImageView.visibility = View.VISIBLE
        }
    }
}