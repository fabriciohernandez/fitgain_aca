package com.XD.fitgain.adapters

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.XD.fitgain.R
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_log_in.view.*
import android.os.Handler

class AlertDialogUtility {

    companion object {
        //Se crea un alert dialog personalizado en donde se puede variar la animacion y
        //el texto presente
        fun alertDialog(context: Context, alertText: String, animNumber: Int) {
            val layoutBuilder = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
            val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(layoutBuilder)
            val alertDialog: AlertDialog = builder.show()
            layoutBuilder.tv_alert.text = alertText
            if (animNumber == 1) {
                layoutBuilder.lottie_anim.setAnimation("26380-happy-watermelon.json")
            } else {
                layoutBuilder.lottie_anim.setAnimation("22499-stay-healthy-eat-healty.json")
            }
            layoutBuilder.lottie_anim.playAnimation()
            layoutBuilder.btn_ok.setOnClickListener {
                alertDialog.dismiss()
            }
        }
    }
}