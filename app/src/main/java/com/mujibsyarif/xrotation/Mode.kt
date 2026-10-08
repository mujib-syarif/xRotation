package com.mujibsyarif.xrotation

import android.content.pm.ActivityInfo

/**
 * Empat mode orientasi. [glyphRotation] adalah putaran ikon ponsel di UI,
 * [degrees] sudut yang ditampilkan, [orientation] nilai ActivityInfo untuk overlay.
 */
enum class Mode(
    val id: Int,
    val title: String,
    val sub: String,
    val degrees: Int,
    val glyphRotation: Float,
    val orientation: Int,
) {
    PORTRAIT(0, "Portrait", "Atas", 0, 0f, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    PORTRAIT_REVERSE(1, "Portrait", "Bawah", 180, 180f, ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT),
    LANDSCAPE_LEFT(2, "Lanskap", "Kiri", 90, -90f, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    LANDSCAPE_RIGHT(3, "Lanskap", "Kanan", 270, 90f, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

    val fullName: String
        get() = when (this) {
            PORTRAIT -> "Portrait"
            PORTRAIT_REVERSE -> "Portrait terbalik"
            LANDSCAPE_LEFT -> "Lanskap kiri"
            LANDSCAPE_RIGHT -> "Lanskap kanan"
        }

    companion object {
        fun fromId(id: Int): Mode = entries.firstOrNull { it.id == id } ?: PORTRAIT
    }
}
