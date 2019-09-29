package com.teckudos.demoapplication.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.teckudos.demoapplication.R
import com.teckudos.demoapplication.viewmodel.PlacesApiStatus

@BindingAdapter("placesApiStatus")
fun bindStatus(statusImageView: ImageView, status: PlacesApiStatus?) {
    when (status) {
        PlacesApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_loading)
        }
        PlacesApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        PlacesApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}