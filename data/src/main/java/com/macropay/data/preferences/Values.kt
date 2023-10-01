package com.macropay.data.preferences

import android.content.Context

object Values {
    var context: Context? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }
    var imei: String = "inst"
        get() {
            return field
        }
        set(value) {
            field = value
        }
    var hasImei: Boolean = true
        get() {
            return field
        }
        set(value) {
            field = value
        }
}