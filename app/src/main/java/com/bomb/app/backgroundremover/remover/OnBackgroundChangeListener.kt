package com.bomb.app.backgroundremover.remover

import android.graphics.Bitmap

interface OnBackgroundChangeListener {
    fun onSuccess(bitmap: Bitmap)

    fun onFailed(exception: Exception)
}