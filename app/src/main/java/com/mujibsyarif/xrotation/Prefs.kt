package com.mujibsyarif.xrotation

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Penyimpanan sederhana: status layanan, mode terpilih, dan statistik rotasi harian. */
object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("xrotation", Context.MODE_PRIVATE)

    private val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.US)

    private fun dayKey(offsetDays: Int = 0): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return "d_" + dayFmt.format(cal.time)
    }

    fun isEnabled(c: Context): Boolean = sp(c).getBoolean("enabled", false)

    fun setEnabled(c: Context, value: Boolean) {
        val e = sp(c).edit().putBoolean("enabled", value)
        if (value) e.putLong("startedAt", System.currentTimeMillis())
        e.apply()
    }

    fun mode(c: Context): Mode = Mode.fromId(sp(c).getInt("mode", 0))

    fun setMode(c: Context, mode: Mode) {
        val key = dayKey()
        val sp = sp(c)
        sp.edit()
            .putInt("mode", mode.id)
            .putInt(key, sp.getInt(key, 0) + 1)
            .apply()
    }

    fun startedAt(c: Context): Long = sp(c).getLong("startedAt", 0L)

    fun todayCount(c: Context): Int = sp(c).getInt(dayKey(), 0)

    /** Jumlah rotasi 7 hari terakhir, urutan lama ke hari ini. */
    fun history(c: Context): List<Int> = (-6..0).map { sp(c).getInt(dayKey(it), 0) }

    /** Nama hari singkat untuk 7 hari terakhir, urutan sama dengan [history]. */
    fun historyLabels(): List<String> {
        val fmt = SimpleDateFormat("EEE", Locale("id", "ID"))
        return (-6..0).map {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, it)
            fmt.format(cal.time)
        }
    }
}
