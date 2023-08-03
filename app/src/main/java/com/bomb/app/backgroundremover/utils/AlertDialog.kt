package com.bomb.app.backgroundremover.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import com.bomb.app.backgroundremover.R
import com.bomb.app.backgroundremover.ads.Adsimpl
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject


@ActivityScoped
class AlertDialog @Inject constructor(@ActivityContext private var context: Context,private var ads:Adsimpl) {

     lateinit var  dialog:AlertDialog


    fun show_alert() {
        val builder = AlertDialog.Builder(context)
        val layoutInflaterAndroid = LayoutInflater.from(context)
        val view: View = layoutInflaterAndroid.inflate(R.layout.progress_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        ads.loadnativad(view.findViewById(R.id.my_template))
    }

}