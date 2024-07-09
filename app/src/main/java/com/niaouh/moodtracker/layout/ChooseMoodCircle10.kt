package com.niaouh.moodtracker.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.niaouh.moodtracker.R
import com.niaouh.moodtracker.Settings
import com.niaouh.moodtracker.data.CircleMoodBO
import com.niaouh.moodtracker.data.CircleStateBO
import com.niaouh.moodtracker.model.MoodEntryModel
import com.niaouh.moodtracker.utils.AnimUtils
import com.niaouh.moodtracker.utils.ResUtil
import kotlin.properties.Delegates

class ChooseMoodCircle10 : FrameLayout {

    private var selectedMood: CircleMoodBO by Delegates.observable(CircleMoodBO.NONE) { _, _, value ->
        circle5.mood = value
        circle5.state = CircleStateBO.EDIT
        circle5.setOnClickListener { expand() }
        if (isMax5 and expanded) circleCollapseAnimation()
        if (!isMax5 and expanded) circleCollapseAnimation10()
        circle5.bringToFront()
        expanded = false
        val newText = value.toText(isMax5)
        tvValue.text = newText
    }

    private val circle9: MoodCircle
    private val circle8: MoodCircle
    private val circle7: MoodCircle
    private val circle6: MoodCircle
    private val circle5: MoodCircle
    private val circle4: MoodCircle
    private val circle3: MoodCircle
    private val circle2: MoodCircle
    private val circle1: MoodCircle
    private val tvValue: TextView
    private val main: FrameLayout
    private var expanded: Boolean = false
    private var isMax5: Boolean = false
    private var height: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        isMax5 = Settings.moodMax == 5
        LayoutInflater.from(context).inflate(R.layout.view_choose_mood_circle_10, this)
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
        main = findViewById(R.id.view_choose_mood_10_main)
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
        circle9.setOnClickListener { selectMood(CircleMoodBO.from(9)) }
        circle8.setOnClickListener { selectMood(CircleMoodBO.from(8)) }
        circle7.setOnClickListener { selectMood(CircleMoodBO.from(7)) }
        circle6.setOnClickListener { selectMood(CircleMoodBO.from(6)) }
        circle4.setOnClickListener { selectMood(CircleMoodBO.from(4)) }
        circle3.setOnClickListener { selectMood(CircleMoodBO.from(3)) }
        circle2.setOnClickListener { selectMood(CircleMoodBO.from(2)) }
        circle1.setOnClickListener { selectMood(CircleMoodBO.from(1)) }
    }

    private fun setupMoodColors() {
        circle9.mood = CircleMoodBO.from(9)
        circle8.mood = CircleMoodBO.from(8)
        circle7.mood = CircleMoodBO.from(7)
        circle6.mood = CircleMoodBO.from(6)
        circle4.mood = CircleMoodBO.from(4)
        circle3.mood = CircleMoodBO.from(3)
        circle2.mood = CircleMoodBO.from(2)
        circle1.mood = CircleMoodBO.from(1)

        circle5.mood = CircleMoodBO.NONE
    }

    private fun expand() {
        if (!expanded) {
            circle5.state = CircleStateBO.CHOOSE_MOOD
            circle5.mood = CircleMoodBO.from(5)
            circle5.setOnClickListener { selectMood(CircleMoodBO.from(5)) }
            if (isMax5) circleExpandAnimation()
            else circleExpandAnimation10()
            expanded = true
        }
    }

    private fun selectMood(Mood: CircleMoodBO) {
        if (expanded) {
            selectedMood = Mood
        }
    }

    private fun circleExpandAnimation10() {
        val distanceX =
            circle5.width + ResUtil.getDimenDp(resources, R.dimen.choose_mood_circle_margin)
        val distanceY =
            (circle5.height + ResUtil.getDimenDp(resources, R.dimen.choose_mood_circle_margin))/2
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
            circle5.width + ResUtil.getDimenDp(resources, R.dimen.choose_mood_circle_margin)
        val distanceY =
            (circle5.height + ResUtil.getDimenDp(resources, R.dimen.choose_mood_circle_margin))/2
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
            circle5.width + ResUtil.getDimenDp(resources, R.dimen.choose_mood_circle_margin)
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
        return selectedMood.toInt()
    }

    fun setSelected(mood: MoodEntryModel) {
        selectedMood = CircleMoodBO.from(mood.mood)
    }

    fun reset() {
        selectedMood = if(expanded)
            selectedMood
        else CircleMoodBO.NONE
    }
}