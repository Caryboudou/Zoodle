package com.kalzakath.zoodle.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.kalzakath.zoodle.R
import com.kalzakath.zoodle.data.CircleFatigueBO
import com.kalzakath.zoodle.data.CircleStateBO
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.utils.AnimUtils
import com.kalzakath.zoodle.utils.ResUtil
import kotlin.properties.Delegates

class ChooseFatigueCircle : FrameLayout {

    private var selectedFatigue: CircleFatigueBO by Delegates.observable(CircleFatigueBO.NONE) { _, _, value ->
        mediocreCircle.fatigue = value
        mediocreCircle.state = CircleStateBO.EDIT
        mediocreCircle.setOnClickListener { expand() }
        circleCollapseAnimation()
        mediocreCircle.bringToFront()
        expanded = false
    }

    private val veryGoodCircle: FatigueCircle
    private val goodCircle: FatigueCircle
    private val mediocreCircle: FatigueCircle
    private val badCircle: FatigueCircle
    private val veryBadCircle: FatigueCircle
    private var expanded: Boolean = false


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_choose_fatigue_circle, this)
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
        veryGoodCircle.setOnClickListener { selectFatigue(CircleFatigueBO.VERY_GOOD) }
        goodCircle.setOnClickListener { selectFatigue(CircleFatigueBO.GOOD) }
        badCircle.setOnClickListener { selectFatigue(CircleFatigueBO.BAD) }
        veryBadCircle.setOnClickListener { selectFatigue(CircleFatigueBO.VERY_BAD) }
    }

    private fun setupMoodColors() {
        veryGoodCircle.fatigue = CircleFatigueBO.VERY_GOOD
        goodCircle.fatigue = CircleFatigueBO.GOOD
        badCircle.fatigue = CircleFatigueBO.BAD
        veryBadCircle.fatigue = CircleFatigueBO.VERY_BAD
        mediocreCircle.fatigue = CircleFatigueBO.NONE
    }

    private fun expand() {
        if (!expanded) {
            mediocreCircle.state = CircleStateBO.CHOOSE_MOOD
            mediocreCircle.fatigue = CircleFatigueBO.MEDIOCRE
            mediocreCircle.setOnClickListener { selectFatigue(CircleFatigueBO.MEDIOCRE) }
            circleExpandAnimation()
            expanded = true
        }
    }

    private fun selectFatigue(fatigue: CircleFatigueBO) {
        if (expanded) {
            selectedFatigue = fatigue
        }
    }

    private fun circleExpandAnimation() {
        val distance =
            mediocreCircle.width + ResUtil.getDimenDp(resources, R.dimen.choose_fatigue_circle_margin)
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

    fun toInt() : Int {
        return selectedFatigue.toInt()
    }

    fun setSelected(mood: MoodEntryModel) {
        selectedFatigue = CircleFatigueBO.from(mood.fatigue)
    }

    fun reset() {
        selectedFatigue = CircleFatigueBO.NONE
    }
}