package com.mujibsyarif.xrotation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import java.util.Locale

private val Bg = Color(0xFF0C0C0D)
private val Ink = Color(0xFFF5F5F7)
private val GlassFill = Color.White.copy(alpha = 0.07f)
private val Edge = Color.White.copy(alpha = 0.16f)
private val Dim = Color.White.copy(alpha = 0.55f)

class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent { XRotationApp() }
    }
}

@Composable
fun XRotationApp() {
    val ctx = LocalContext.current

    var enabled by remember { mutableStateOf(Prefs.isEnabled(ctx)) }
    var mode by remember { mutableStateOf(Prefs.mode(ctx)) }
    var count by remember { mutableStateOf(Prefs.todayCount(ctx)) }
    var history by remember { mutableStateOf(Prefs.history(ctx)) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var needsOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (Prefs.isEnabled(ctx)) {
            if (Settings.canDrawOverlays(ctx)) RotationService.start(ctx) else Prefs.setEnabled(ctx, false)
        }
        while (true) {
            enabled = Prefs.isEnabled(ctx)
            mode = Prefs.mode(ctx)
            count = Prefs.todayCount(ctx)
            history = Prefs.history(ctx)
            now = System.currentTimeMillis()
            if (Settings.canDrawOverlays(ctx)) needsOverlay = false
            delay(1500)
        }
    }

    fun toggle() {
        if (!enabled) {
            if (!Settings.canDrawOverlays(ctx)) {
                needsOverlay = true
                ctx.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ctx.packageName)),
                )
                return
            }
            Prefs.setEnabled(ctx, true)
            RotationService.start(ctx)
            enabled = true
            needsOverlay = false
        } else {
            Prefs.setEnabled(ctx, false)
            RotationService.stop(ctx)
            enabled = false
        }
    }

    fun pick(m: Mode) {
        if (!enabled) return
        Prefs.setMode(ctx, m)
        mode = m
        count = Prefs.todayCount(ctx)
        history = Prefs.history(ctx)
        RotationService.start(ctx)
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Bg, onBackground = Ink)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Bg)
                .drawBehind {
                    drawCircle(
                        Color.White.copy(alpha = 0.10f),
                        radius = size.width * 0.45f,
                        center = Offset(size.width * 0.95f, 0f),
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.06f),
                        radius = size.width * 0.40f,
                        center = Offset(0f, size.height * 0.55f),
                    )
                },
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Label("xRotation", 26, Ink, FontWeight.Medium)
                        Label(if (enabled) "Layanan aktif" else "Layanan mati", 13, Dim)
                    }
                    XSwitch(enabled, ::toggle)
                }

                Spacer(Modifier.height(18.dp))

                if (needsOverlay) {
                    GlassCard(Modifier.fillMaxWidth(), 18.dp) {
                        Box(Modifier.padding(14.dp)) {
                            Label(
                                "Izinkan \"Tampil di atas aplikasi lain\" untuk xRotation, lalu nyalakan layanan lagi.",
                                13,
                                Ink,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                GlassCard(Modifier.fillMaxWidth(), 26.dp) {
                    Row(
                        Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Box(
                            Modifier
                                .size(104.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            PhoneGlyph(mode)
                        }
                        Column {
                            Label("Orientasi saat ini", 12, Dim)
                            Label(mode.fullName, 20, Ink, FontWeight.Medium)
                            Label(mode.degrees.toString() + "°", 30, Ink, FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModeTile(Mode.PORTRAIT, mode, enabled, Modifier.weight(1f)) { pick(Mode.PORTRAIT) }
                    ModeTile(Mode.PORTRAIT_REVERSE, mode, enabled, Modifier.weight(1f)) { pick(Mode.PORTRAIT_REVERSE) }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModeTile(Mode.LANDSCAPE_LEFT, mode, enabled, Modifier.weight(1f)) { pick(Mode.LANDSCAPE_LEFT) }
                    ModeTile(Mode.LANDSCAPE_RIGHT, mode, enabled, Modifier.weight(1f)) { pick(Mode.LANDSCAPE_RIGHT) }
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Rotasi hari ini", count.toString(), Modifier.weight(1f))
                    StatCard("Lama aktif", activeFor(ctx, enabled, now), Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))

                HistoryCard(history)

                Spacer(Modifier.height(28.dp))

                Text(
                    "©mujib_syarif",
                    modifier = Modifier.fillMaxWidth(),
                    color = Dim,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun activeFor(ctx: Context, enabled: Boolean, now: Long): String {
    val start = Prefs.startedAt(ctx)
    if (!enabled || start <= 0L) return "--:--"
    val minutes = ((now - start) / 60000L).coerceAtLeast(0L)
    return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60)
}

@Composable
private fun Label(
    text: String,
    size: Int,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
) {
    Text(text, color = color, fontSize = size.sp, fontWeight = weight)
}

@Composable
private fun GlassCard(
    modifier: Modifier,
    radius: androidx.compose.ui.unit.Dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .clip(shape)
            .background(GlassFill)
            .border(0.5.dp, Edge, shape),
        content = content,
    )
}

@Composable
private fun XSwitch(on: Boolean, onClick: () -> Unit) {
    val x by animateDpAsState(if (on) 25.dp else 3.dp, label = "knob")
    Box(
        Modifier
            .size(52.dp, 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (on) Ink else Color.White.copy(alpha = 0.2f))
            .clickable(role = Role.Switch, onClick = onClick),
    ) {
        Box(
            Modifier
                .offset(x = x, y = 3.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (on) Bg else Ink),
        )
    }
}

@Composable
private fun PhoneGlyph(mode: Mode) {
    val rot by animateFloatAsState(mode.glyphRotation, tween(450), label = "rot")
    Box(
        Modifier
            .size(40.dp, 72.dp)
            .rotate(rot)
            .border(2.dp, Ink, RoundedCornerShape(10.dp)),
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
                .size(14.dp, 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Ink),
        )
    }
}

@Composable
private fun ModeTile(
    m: Mode,
    current: Mode,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val selected = m == current
    val shape = RoundedCornerShape(20.dp)
    val fg = if (selected) Bg else Ink
    Column(
        modifier
            .defaultMinSize(minHeight = 96.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(if (selected) Ink else GlassFill)
            .border(0.5.dp, if (selected) Ink else Edge, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(12.dp, 22.dp)
                    .rotate(m.glyphRotation)
                    .border(1.5.dp, fg, RoundedCornerShape(4.dp)),
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                        .size(4.dp, 1.5.dp)
                        .background(fg),
                )
            }
        }
        Column {
            Label(m.title, 15, fg, FontWeight.Medium)
            Label(m.sub, 12, fg.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    GlassCard(modifier, 20.dp) {
        Column(Modifier.padding(14.dp)) {
            Label(label, 12, Dim)
            Spacer(Modifier.height(4.dp))
            Label(value, 26, Ink, FontWeight.Medium)
        }
    }
}

@Composable
private fun HistoryCard(history: List<Int>) {
    val labels = remember { Prefs.historyLabels() }
    val max = maxOf(1, history.maxOrNull() ?: 1)
    GlassCard(Modifier.fillMaxWidth(), 20.dp) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Label("7 hari terakhir", 12, Dim)
                Label("rotasi", 12, Dim)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                history.forEachIndexed { i, v ->
                    Column(Modifier.width(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.height(56.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(
                                Modifier
                                    .width(22.dp)
                                    .height((v.toFloat() / max * 52f + 4f).dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (i == history.lastIndex) Ink else Color.White.copy(alpha = 0.25f)),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Label(labels.getOrElse(i) { "" }, 11, Dim)
                    }
                }
            }
        }
    }
}
