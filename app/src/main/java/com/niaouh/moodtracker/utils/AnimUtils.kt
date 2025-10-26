package com.niaouh.moodtracker.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Path
import android.view.View
import androidx.core.animation.doOnEnd


object AnimUtils {

    fun animateMove(duration: Int, move: Int, view: View, doOnEnd: () -> Unit = {}) {
        val buttonAnimator =
            ObjectAnimator.ofFloat(view, "translationX", move.toFloat())
        buttonAnimator.duration = duration.toLong()
        buttonAnimator.doOnEnd {
            doOnEnd.invoke()
        }
        buttonAnimator.start()
    }

    fun animateMove10(duration: Int, moveX: Int, moveY: Int, view: View, doOnEnd: () -> Unit = {}) {
        val path = Path()
        //path.moveTo(view.x, view.y)
        path.lineTo(moveX.toFloat(), moveY.toFloat())
        val buttonAnimator =
            ObjectAnimator.ofFloat(view, "translationX", "translationY", path)
        buttonAnimator.duration = duration.toLong()
        buttonAnimator.doOnEnd {
            doOnEnd.invoke()
        }
        buttonAnimator.start()
    }

    fun animateCollapse10(duration: Int, moveX: Int, moveY: Int, view: View, doOnEnd: () -> Unit = {}) {
        val path = Path()
        path.moveTo(moveX.toFloat(), moveY.toFloat())
        path.lineTo(0f, 0f)
        val buttonAnimator =
            ObjectAnimator.ofFloat(view, "translationX", "translationY", path)
        buttonAnimator.duration = duration.toLong()
        buttonAnimator.doOnEnd {
            doOnEnd.invoke()
        }
        buttonAnimator.start()
    }

    fun animateAlpha(durationToSet: Int, to: Float, vararg views: View) {
        views.forEach { view ->
            view.animate().apply {
                alpha(to)
                duration = durationToSet.toLong()
                setListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            view.alpha = to
                            super.onAnimationEnd(animation)
                        }
                    }
                )
                start()
            }
        }
    }

    fun fadeIn(duration: Int, vararg views: View) {
        fadeIn(duration, {}, *views)
    }

    fun fadeOut(duration: Int, vararg views: View) {
        fadeOut(duration, {}, *views)
    }

    fun fadeIn(duration: Int, doOnEnd: () -> Unit = {}, vararg views: View) {
        for (view in views) {
            if (view.visibility == View.GONE || view.visibility == View.INVISIBLE) {
                view.alpha = 0f
                view.visibility = View.VISIBLE
                view.animate()
                    .alpha(1f)
                    .setDuration(duration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view.visibility = View.VISIBLE
                            doOnEnd.invoke()
                        }
                    })
            }
        }
    }

    fun fadeOut(duration: Int, doOnEnd: () -> Unit = {}, vararg views: View) {
        for (view in views) {
            if (view.visibility == View.VISIBLE) {
                view.alpha = 1f
                view.visibility = View.VISIBLE
                view.animate()
                    .alpha(0f)
                    .setDuration(duration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            view.visibility = View.INVISIBLE
                            doOnEnd.invoke()
                        }
                    })
            }
        }
    }
}