package io.lamart.livedata.example

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

val Context.viewModelFactory: ViewModelFactory
    get() = applicationContext.let { it as SampleApplication }.viewModelFactory

inline fun <reified T : ViewModel> Fragment.viewModel(): Lazy<T> = lazy {
    ViewModelProviders
        .of(this, requireContext().viewModelFactory)
        .get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.viewModel(): Lazy<T> = lazy {
    ViewModelProviders
        .of(this, viewModelFactory)
        .get(T::class.java)
}