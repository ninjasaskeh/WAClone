package com.example.wacloneapp.util

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.wacloneapp.R
import java.text.DateFormat
import java.util.*

fun populateImage(
    context: Context?,
    uri: String?,
    imageView: ImageView,
    errorDrawable: Int = R.drawable.empty
) {
    if (context != null) {
        val options = RequestOptions().placeholder(progressDrawable(context)).error(errorDrawable)
        Glide.with(context).load(uri).apply(options).into(imageView)
    }
}

fun progressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 15f
        centerRadius = 50f
        start()
    }
}

fun getTime(): String{
    val dateFormat = DateFormat.getDateInstance()
    return dateFormat.format(Date())
}