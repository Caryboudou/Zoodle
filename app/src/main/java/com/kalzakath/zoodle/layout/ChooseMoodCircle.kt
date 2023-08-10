package com.kalzakath.zoodle.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.kalzakath.zoodle.R
import com.kalzakath.zoodle.data.CircleMoodBO
import com.kalzakath.zoodle.data.CircleStateBO
import com.kalzakath.zoodle.data.MoodDO
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.utils.AnimUtils
import com.kalzakath.zoodle.utils.ResUtil
import kotlin.properties.Delegates

const val ANIM_DURATION = 300

class ChooseMoodCircle : FrameLayout {

    var selectedMood: CircleMoodBO by Delegates.observable(CircleMoodBO.NONE) { _, _, value ->
        mediocreCircle.mood = value
        onSelectedMoodChange.invoke(value)
    }

    private val veryGoodCircle: MoodCircle
    private val goodCircle: MoodCircle
    private val mediocreCircle: MoodCircle
    private val badCircle: MoodCircle
    private val veryBadCircle: MoodCircle
    private var expanded: Boolean = false
    private var isInit: Boolean = false

    private var onSelectedMoodChange: (CircleMoodBO) -> Unit = { }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_choose_mood_circle, this)
        veryGoodCircle = findViewById(R.id.veryGoodCircle)
        goodCircle = findViewById(R.id.goodCircle)
        mediocreCircle = findViewById(R.id.mediocreCircle)
        badCircle = findViewById(R.id.badCircle)
        veryBadCircle = findViewById(R.id.veryBadCircle)
        setupMoodColors()
        setupInitialState()
    }

    private fun setupInitialState() {
        mediocreCircle.state = CircleStateBO.EDIT
        veryGoodCircle.state = CircleStateBO.CHOOSE_MOOD
        goodCircle.state = CircleStateBO.CHOOSE_MOOD
        badCircle.state = CircleStateBO.CHOOSE_MOOD
        veryBadCircle.state = CircleStateBO.CHOOSE_MOOD
        mediocreCircle.setOnClickListener { expand() }
        veryGoodCircle.setOnClickListener { selectMood(CircleMoodBO.VERY_GOOD) }
        goodCircle.setOnClickListener { selectMood(CircleMoodBO.GOOD) }
        badCircle.setOnClickListener { selectMood(CircleMoodBO.BAD) }
        veryBadCircle.setOnClickListener { selectMood(CircleMoodBO.VERY_BAD) }
    }

    private fun setupMoodColors() {
        veryGoodCircle.mood = CircleMoodBO.VERY_GOOD
        goodCircle.mood = CircleMoodBO.GOOD
        badCircle.mood = CircleMoodBO.BAD
        veryBadCircle.mood = CircleMoodBO.VERY_BAD
        if (isInit) {
            mediocreCircle.mood = CircleMoodBO.MEDIOCRE
        }
        else {
            mediocreCircle.mood = CircleMoodBO.NONE
        }
    }

    private fun expand() {
        if (!expanded) {
            mediocreCircle.state = CircleStateBO.CHOOSE_MOOD
            mediocreCircle.mood = CircleMoodBO.MEDIOCRE
            mediocreCircle.setOnClickListener { selectMood(CircleMoodBO.MEDIOCRE) }
            circleExpandAnimation()
            expanded = true
        }
    }

    private fun selectMood(fatigue: CircleMoodBO) {
        if (expanded) {
            mediocreCircle.state = CircleStateBO.EDIT
            selectedMood= fatigue
            mediocreCircle.setOnClickListener { expand() }
            circleCollapseAnimation()
            mediocreCircle.bringToFront()
            expanded = false
            isInit = true
        }
    }

    private fun circleExpandAnimation() {
        val distance =
            mediocreCircle.width + ResUtil.getDimenDp(resources, R.dimen.choose_mood_circle_margin)
        AnimUtils.animateMove(ANIM_DURATION, -(distance * 2), veryBadCircle)
        AnimUtils.animateMove(ANIM_DURATION, -distance, badCircle)
        AnimUtils.animateMove(ANIM_DURATION, distance, goodCircle)
        AnimUtils.animateMove(ANIM_DURATION, distance * 2, veryGoodCircle)
    }

    private fun circleCollapseAnimation() {
        AnimUtils.animateMove(ANIM_DURATION, 0, veryGoodCircle)
        AnimUtils.animateMove(ANIM_DURATION, 0, goodCircle)
        AnimUtils.animateMove(ANIM_DURATION, 0, badCircle)
        AnimUtils.animateMove(ANIM_DURATION, 0, veryBadCircle)
    }

    fun setOnSelectedMoodAction(action: (CircleMoodBO) -> Unit) {
        onSelectedMoodChange = action
    }

    fun toInt() : Int {
        return selectedMood.toInt()
    }

    fun setSelected(mood: MoodEntryModel) {
        isInit = true
        mediocreCircle.mood = CircleMoodBO.MEDIOCRE
        selectedMood = CircleMoodBO.from(mood.mood)
    }

    fun reset() {
        isInit = false
        mediocreCircle.mood = CircleMoodBO.NONE
        selectedMood = CircleMoodBO.NONE
    }
}