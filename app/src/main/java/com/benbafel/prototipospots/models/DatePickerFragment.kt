package com.benbafel.prototipospots.models

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class DatePickerFragment(val listener: (day:Int,month:Int,year:Int)->Unit): DialogFragment(),
DatePickerDialog.OnDateSetListener{

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_MONTH)
        val month = c.get(Calendar.MONTH)
        val year = c.get(Calendar.YEAR)


        val picker = DatePickerDialog(activity as Context,this,year,month,day)
        picker.datePicker.minDate = c.timeInMillis
        picker.datePicker.maxDate = System.currentTimeMillis() + (31536000000)
        return picker

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener(dayOfMonth,month,year)
    }
}