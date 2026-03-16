package com.jude.minimallauncher.ui

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.jude.minimallauncher.data.AppPrefs

object PinDialog {
    fun show(context: Context, message: String, onResult: (Boolean) -> Unit) {
        val input = EditText(context)
        input.hint = "PIN"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD

        AlertDialog.Builder(context)
            .setTitle("Override")
            .setMessage(message)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val pin = input.text.toString().trim()
                val ok = pin == (AppPrefs.getPin(context) ?: "0000")
                onResult(ok)
            }
            .setNegativeButton("Cancel") { _, _ -> onResult(false) }
            .show()
    }
}
