package com.niaouh.moodtracker.trackerpopup

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.NumberPicker
import com.niaouh.moodtracker.MainActivity
import com.niaouh.moodtracker.R
import java.util.logging.Logger

class TrendViewSelectMoyDialog(private val context: Context,
                               private val finish: (Int) -> Unit) : Dialog(context) {
    private var dialog = Dialog(context)
    private var moy: Int = 0
    private val log = Logger.getLogger(MainActivity::class.java.name + "TrendViewSelectMoyDialog")

    fun showPopup(currentMoy: Int = 7) {
        moy = currentMoy
        log.info("showpopup $currentMoy")
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_trend_view_selec_moy, null, false)
        dialog.setContentView(view)
        dialog.create()
        log.info("create $currentMoy")
        initButton()
        log.info("button initalized $currentMoy")
        dialog.show()
    }

    private fun initButton() {
        val bConfirm: Button = dialog.findViewById(R.id.bConfirm)
        val bCancel: Button = dialog.findViewById(R.id.bCancel)

        log.info("button initaliting")
        val npSelectMoy: NumberPicker = dialog.findViewById(R.id.npSelectMoy)
        npSelectMoy.minValue = 2
        npSelectMoy.maxValue = 20
        npSelectMoy.value = moy

        bConfirm.setOnClickListener {
            finish(npSelectMoy.value)
            dialog.dismiss()
        }

        bCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}