package com.XD.fitgain.Adapters

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.XD.fitgain.R
import kotlinx.android.synthetic.main.dialog_layout.view.*

class LoadingDialog {
    val activity:Activity= TODO()
    var loadingDialog:AlertDialog

    constructor(myActivity: Activity){
        activity = myActivity
    }

    fun startLoading(){
        val layoutBuilder = LayoutInflater.from(activity).inflate(R.layout.dialog_layout, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity).setView(layoutBuilder)

        layoutBuilder.lottie_anim.setAnimation("loading.json")
        layoutBuilder.lottie_anim.playAnimation()
        loadingDialog = builder.create()
        loadingDialog.show()
    }
}