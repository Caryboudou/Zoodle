package com.niaouh.moodtracker.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.niaouh.moodtracker.R
import com.niaouh.moodtracker.Settings
import com.niaouh.moodtracker.data.CircleFatigueBO
import com.niaouh.moodtracker.data.CircleStateBO
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.utils.AnimUtils
import com.niaouh.moodtracker.utils.ResUtil
import kotlin.properties.Delegates

class ChooseFatigueCircle10 : FrameLayout {

    private var selectedFatigue: CircleFatigueBO by Delegates.observable(CircleFatigueBO.NONE) { _, _, value ->
        circle5.fatigue = value
        circle5.state = CircleStateBO.EDIT
        circle5.setOnClickListener { expand() }
        if (isMax5 and expanded) circleCollapseAnimation()
        if (!isMax5 and expanded) circleCollapseAnimation10()
        circle5.bringToFront()
        expanded = false
        val newText = value.toText(isMax5)
        tvValue.text = newText
    }

    private val circle9: FatigueCircle
    private val circle8: FatigueCircle
    private val circle7: FatigueCircle
    private val circle6: FatigueCircle
    private val circle5: FatigueCircle
    private val circle4: FatigueCircle
    private val circle3: FatigueCircle
    private val circle2: FatigueCircle
    private val circle1: FatigueCircle
    private val tvValue: TextView
    private val main: FrameLayout
    private var expanded: Boolean = false
    private var isMax5: Boolean = false
    private var height: Int = 0

    constructor(context: Context) : super(context) { this.isMax5}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        isMax5 = Settings.moodMax == 5
        LayoutInflater.from(context).inflate(R.layout.view_choose_fatigue_circle_10, this)
        circle9 = findViewById(R.id.Circle9)
        circle8 = findViewById(R.id.Circle8)
        circle7 = findViewById(R.id.Circle7)
        circle6 = findViewById(R.id.Circle6)
        circle5 = findViewById(R.id.Circle5)
        circle4 = findViewById(R.id.Circle4)
        circle3 = findViewById(R.id.Circle3)
        circle2 = findViewById(R.id.Circle2)
        circle1 = findViewById(R.id.Circle1)
        tvValue = findViewById(R.id.tvValue)
        main = findViewById(R.id.view_choose_fatigue_10_main)
        setupMoodColors()
        setupInitialState()
        if (isMax5) {
            circle2.visibility = GONE
            circle4.visibility = GONE
            circle6.visibility = GONE
            circle8.visibility = GONE
        }
        else {
            circle2.visibility = VISIBLE
            circle4.visibility = VISIBLE
            circle6.visibility = VISIBLE
            circle8.visibility = VISIBLE
            val param = main.layoutParams
            param.height *= 2
            main.layoutParams = param
        }
    }

    private fun setupInitialState() {
        circle5.state = CircleStateBO.EDIT
        circle9.state = CircleStateBO.CHOOSE_MOOD
        circle8.state = CircleStateBO.CHOOSE_MOOD
        circle7.state = CircleStateBO.CHOOSE_MOOD
        circle6.state = CircleStateBO.CHOOSE_MOOD
        circle4.state = CircleStateBO.CHOOSE_MOOD
        circle3.state = CircleStateBO.CHOOSE_MOOD
        circle2.state = CircleStateBO.CHOOSE_MOOD
        circle1.state = CircleStateBO.CHOOSE_MOOD
        circle5.setOnClickListener { expand() }
        circle9.setOnClickListener { selectFatigue(CircleFatigueBO.from(9)) }
        circle8.setOnClickListener { selectFatigue(CircleFatigueBO.from(8)) }
        circle7.setOnClickListener { selectFatigue(CircleFatigueBO.from(7)) }
        circle6.setOnClickListener { selectFatigue(CircleFatigueBO.from(6)) }
        circle4.setOnClickListener { selectFatigue(CircleFatigueBO.from(4)) }
        circle3.setOnClickListener { selectFatigue(CircleFatigueBO.from(3)) }
        circle2.setOnClickListener { selectFatigue(CircleFatigueBO.from(2)) }
        circle1.setOnClickListener { selectFatigue(CircleFatigueBO.from(1)) }
    }

    private fun setupMoodColors() {
        circle9.fatigue = CircleFatigueBO.from(9)
        circle8.fatigue = CircleFatigueBO.from(8)
        circle7.fatigue = CircleFatigueBO.from(7)
        circle6.fatigue = CircleFatigueBO.from(6)
        circle4.fatigue = CircleFatigueBO.from(4)
        circle3.fatigue = CircleFatigueBO.from(3)
        circle2.fatigue = CircleFatigueBO.from(2)
        circle1.fatigue = CircleFatigueBO.from(1)

        circle5.fatigue = CircleFatigueBO.NONE
    }

    private fun expand() {
        if (!expanded) {
            circle5.state = CircleStateBO.CHOOSE_MOOD
            circle5.fatigue = CircleFatigueBO.from(5)
            circle5.setOnClickListener { selectFatigue(CircleFatigueBO.from(5)) }
            if (isMax5) circleExpandAnimation()
            else circleExpandAnimation10()
            expanded = true
        }
    }

    private fun selectFatigue(fatigue: CircleFatigueBO) {
        if (expanded) {
            selectedFatigue = fatigue
        }
    }

    private fun circleExpandAnimation10() {
        val distanceX =
            circle5.width + ResUtil.getDimenDp(resources, R.dimen.choose_fatigue_circle_margin)
        val distanceY =
            (circle5.height + ResUtil.getDimenDp(resources, R.dimen.choose_fatigue_circle_margin))/2
        AnimUtils.animateMove10(ANIM_DURATION, -(distanceX * 2), -distanceY, circle1)
        AnimUtils.animateMove10(ANIM_DURATION, -distanceX, -distanceY, circle3)
        AnimUtils.animateMove10(ANIM_DURATION, 0, -distanceY, circle5)
        AnimUtils.animateMove10(ANIM_DURATION, distanceX, -distanceY, circle7)
        AnimUtils.animateMove10(ANIM_DURATION, (distanceX * 2), -distanceY, circle9)
        AnimUtils.animateMove10(ANIM_DURATION, -(distanceX/2 + distanceX), distanceY, circle2)
        AnimUtils.animateMove10(ANIM_DURATION, -distanceX/2, distanceY, circle4)
        AnimUtils.animateMove10(ANIM_DURATION, distanceX/2, distanceY, circle6)
        AnimUtils.animateMove10(ANIM_DURATION, (distanceX/2 + distanceX), distanceY, circle8)
    }

    private fun circleCollapseAnimation10() {
        val distanceX =
            circle5.width + ResUtil.getDimenDp(resources, R.dimen.choose_fatigue_circle_margin)
        val distanceY =
            (circle5.height + ResUtil.getDimenDp(resources, R.dimen.choose_fatigue_circle_margin))/2
        AnimUtils.animateCollapse10(ANIM_DURATION, -(distanceX * 2), -distanceY, circle1)
        AnimUtils.animateCollapse10(ANIM_DURATION, -distanceX, -distanceY, circle3)
        AnimUtils.animateCollapse10(ANIM_DURATION, 0, -distanceY, circle5)
        AnimUtils.animateCollapse10(ANIM_DURATION, distanceX, -distanceY, circle7)
        AnimUtils.animateCollapse10(ANIM_DURATION, (distanceX * 2), -distanceY, circle9)
        AnimUtils.animateCollapse10(ANIM_DURATION, -(distanceX/2 + distanceX), distanceY, circle2)
        AnimUtils.animateCollapse10(ANIM_DURATION, -distanceX/2, distanceY, circle4)
        AnimUtils.animateCollapse10(ANIM_DURATION, distanceX/2, distanceY, circle6)
        AnimUtils.animateCollapse10(ANIM_DURATION, (distanceX/2 + distanceX), distanceY, circle8)
    }

    private fun circleExpandAnimation() {
        val distance =
            circle5.width + ResUtil.getDimenDp(resources, R.dimen.choose_fatigue_circle_margin)
        AnimUtils.animateMove(ANIM_DURATION, -(distance * 2), circle1)
        AnimUtils.animateMove(ANIM_DURATION, -distance, circle3)
        AnimUtils.animateMove(ANIM_DURATION, distance, circle7)
        AnimUtils.animateMove(ANIM_DURATION, distance * 2, circle9)
    }

    private fun circleCollapseAnimation() {
        AnimUtils.animateMove(ANIM_DURATION, 0, circle9)
        AnimUtils.animateMove(ANIM_DURATION, 0, circle7)
        AnimUtils.animateMove(ANIM_DURATION, 0, circle3)
        AnimUtils.animateMove(ANIM_DURATION, 0, circle1)
    }

    fun toInt() : Int {
        return selectedFatigue.toInt()
    }

    fun setSelected(mood: MoodEntryModel) {
        selectedFatigue = CircleFatigueBO.from(mood.fatigue)
    }

    fun reset() {
        selectedFatigue = if(expanded)
            selectedFatigue
        else CircleFatigueBO.NONE
    }
}