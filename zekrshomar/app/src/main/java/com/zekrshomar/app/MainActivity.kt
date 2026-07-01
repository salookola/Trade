package com.zekrshomar.app

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ZekrShomarApp() }
    }
}

data class Step(val title: String, val arabic: String, val translation: String, val target: Int)
data class Zekr(
    val id: String,
    val title: String,
    val arabic: String,
    val translation: String,
    val goal: Int,
    val category: String,
    val steps: List<Step> = emptyList(),
    val custom: Boolean = false
)

private val defaults = listOf(
    Zekr("salawat", "صلوات", "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَ آلِ مُحَمَّدٍ", "خدایا بر محمد و خاندان محمد درود فرست.", 100, "اذکار روزانه"),
    Zekr("istighfar", "استغفار", "أَسْتَغْفِرُ اللَّهَ رَبِّي وَ أَتُوبُ إِلَيْهِ", "از خداوند، پروردگارم، آمرزش می‌خواهم و به سوی او بازمی‌گردم.", 100, "اذکار روزانه"),
    Zekr("fatima", "تسبیحات حضرت زهرا (س)", "", "ذکری سه‌مرحله‌ای پس از نماز.", 100, "پس از نماز", listOf(
        Step("الله اکبر", "اللَّهُ أَكْبَرُ", "خداوند بزرگ‌تر است.", 34),
        Step("الحمدلله", "الْحَمْدُ لِلَّهِ", "ستایش مخصوص خداوند است.", 33),
        Step("سبحان الله", "سُبْحَانَ اللَّهِ", "خداوند پاک و منزه است.", 33)
    )),
    Zekr("subhan", "سبحان الله", "سُبْحَانَ اللَّهِ", "خداوند پاک و منزه است.", 33, "تسبیحات"),
    Zekr("hamd", "الحمدلله", "الْحَمْدُ لِلَّهِ", "ستایش مخصوص خداوند است.", 33, "تسبیحات"),
    Zekr("akbar", "الله اکبر", "اللَّهُ أَكْبَرُ", "خداوند بزرگ‌تر است.", 34, "تسبیحات"),
    Zekr("tahlil", "لا اله الا الله", "لَا إِلَهَ إِلَّا اللَّهُ", "هیچ معبودی جز خدا نیست.", 100, "اذکار توحیدی"),
    Zekr("hasb", "حسبنا الله و نعم الوکیل", "حَسْبُنَا اللَّهُ وَ نِعْمَ الْوَكِيلُ", "خدا ما را کافی است و او بهترین کارساز است.", 100, "اذکار توکل"),
    Zekr("yaallah", "یا الله", "یَا اللَّهُ", "ای خدا", 100, "مناجات")
)

@Composable
private fun ZekrShomarApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zekrshomar", Context.MODE_PRIVATE) }
    var customZekrs by remember { mutableStateOf(readCustoms(prefs.getString("customs", "[]") ?: "[]")) }
    var selectedId by remember { mutableStateOf(prefs.getString("selected", "salawat") ?: "salawat") }
    var count by remember { mutableIntStateOf(prefs.getInt("count", 0)) }
    var stepIndex by remember { mutableIntStateOf(prefs.getInt("step", 0)) }
    var goalOverride by remember { mutableIntStateOf(prefs.getInt("goal", 0)) }
    var total by remember { mutableIntStateOf(prefs.getInt("total", 0)) }
    var completed by remember { mutableIntStateOf(prefs.getInt("completed", 0)) }
    var daily by remember { mutableStateOf(readDaily(prefs.getString("daily", "{}") ?: "{}")) }
    var vibration by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
    var sound by remember { mutableStateOf(prefs.getBoolean("sound", false)) }
    var resetAfter by remember { mutableStateOf(prefs.getBoolean("reset_after", false)) }
    var theme by remember { mutableStateOf(prefs.getString("theme", "system") ?: "system") }
    var tab by remember { mutableIntStateOf(0) }
    var showPicker by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDecreaseDialog by remember { mutableStateOf(false) }
    var creatingCustom by remember { mutableStateOf(false) }
    var editor by remember { mutableStateOf<Zekr?>(null) }
    var deleteTarget by remember { mutableStateOf<Zekr?>(null) }
    var clearStage by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }

    val allZekrs = defaults + customZekrs
    val selected = allZekrs.firstOrNull { it.id == selectedId } ?: defaults.first()
    val safeStep = stepIndex.coerceIn(0, (selected.steps.size - 1).coerceAtLeast(0))
    val currentStep = selected.steps.getOrNull(safeStep)
    val target = currentStep?.target ?: goalOverride.takeIf { it > 0 } ?: selected.goal
    val shownArabic = currentStep?.arabic ?: selected.arabic
    val shownTranslation = currentStep?.translation ?: selected.translation
    val today = persianToday()

    fun save() {
        prefs.edit()
            .putString("customs", writeCustoms(customZekrs))
            .putString("selected", selectedId)
            .putInt("count", count)
            .putInt("step", stepIndex)
            .putInt("goal", goalOverride)
            .putInt("total", total)
            .putInt("completed", completed)
            .putString("daily", writeDaily(daily))
            .putBoolean("vibration", vibration)
            .putBoolean("sound", sound)
            .putBoolean("reset_after", resetAfter)
            .putString("theme", theme)
            .apply()
    }

    fun feedback(goalDone: Boolean = false) {
        if (vibration) {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(if (goalDone) 60 else 12)
        }
        if (sound) ToneGenerator(AudioManager.STREAM_NOTIFICATION, 20).startTone(ToneGenerator.TONE_PROP_BEEP2, 18)
    }

    fun increment() {
        val before = count
        count += 1
        total += 1
        daily = daily.toMutableMap().apply { put(today, (this[today] ?: 0) + 1) }
        var goalDone = false
        if (before < target && count >= target) {
            if (selected.steps.isNotEmpty() && safeStep < selected.steps.lastIndex) {
                stepIndex = safeStep + 1
                count = 0
                message = "مرحله بعدی: ${selected.steps[stepIndex].title}"
            } else {
                completed += 1
                goalDone = true
                message = "ذکر شما کامل شد، قبول باشد"
                if (resetAfter) count = 0
            }
        }
        feedback(goalDone)
        save()
    }

    fun decrement() {
        if (count > 0) {
            count -= 1
            total = (total - 1).coerceAtLeast(0)
            daily = daily.toMutableMap().apply { put(today, ((this[today] ?: 0) - 1).coerceAtLeast(0)) }
        } else if (selected.steps.isNotEmpty() && safeStep > 0) {
            stepIndex = safeStep - 1
            count = (selected.steps[stepIndex].target - 1).coerceAtLeast(0)
            total = (total - 1).coerceAtLeast(0)
            daily = daily.toMutableMap().apply { put(today, ((this[today] ?: 0) - 1).coerceAtLeast(0)) }
        }
        save()
    }

    val useDark = when (theme) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val light = lightColorScheme(primary = Color(0xFF0D665B), secondary = Color(0xFF38665E), surface = Color(0xFFFAFCFA))
    val dark = darkColorScheme(primary = Color(0xFF82D8C6), secondary = Color(0xFFB1CCC5), surface = Color(0xFF101513))

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = if (useDark) dark else light) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        listOf("شمارنده", "آمار", "تنظیمات").forEachIndexed { index, title ->
                            NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Text(if (index == 0) "◉" else if (index == 1) "▦" else "⚙") }, label = { Text(title) })
                        }
                    }
                }
            ) { padding ->
                when (tab) {
                    0 -> CounterScreen(
                        modifier = Modifier.padding(padding),
                        selectedTitle = selected.title,
                        arabic = shownArabic,
                        translation = shownTranslation,
                        stage = currentStep?.let { "مرحله ${safeStep + 1} از ${selected.steps.size}: ${it.title}" },
                        count = count,
                        target = target,
                        message = message,
                        vibration = vibration,
                        onVibration = { vibration = it; save() },
                        onSelect = { showPicker = true },
                        onGoal = { if (selected.steps.isEmpty()) showGoalDialog = true },
                        onIncrement = ::increment,
                        onDecrease = { showDecreaseDialog = true },
                        onReset = { showResetDialog = true }
                    )
                    1 -> StatsScreen(
                        modifier = Modifier.padding(padding),
                        today = daily[today] ?: 0,
                        total = total,
                        completed = completed,
                        daily = daily,
                        onClear = { clearStage = 1 }
                    )
                    else -> SettingsScreen(
                        modifier = Modifier.padding(padding),
                        vibration = vibration,
                        sound = sound,
                        theme = theme,
                        resetAfter = resetAfter,
                        onVibration = { vibration = it; save() },
                        onSound = { sound = it; save() },
                        onTheme = { theme = it; save() },
                        onResetAfter = { resetAfter = it; save() }
                    )
                }
            }

            if (showPicker) {
                ZekrPicker(
                    allZekrs = allZekrs,
                    selectedId = selected.id,
                    onDismiss = { showPicker = false },
                    onSelect = {
                        selectedId = it.id; count = 0; stepIndex = 0; goalOverride = 0; message = null; save(); showPicker = false
                    },
                    onCreate = { showPicker = false; creatingCustom = true },
                    onEdit = { showPicker = false; editor = it },
                    onDelete = { deleteTarget = it }
                )
            }
            if (showGoalDialog && selected.steps.isEmpty()) {
                GoalDialog(target, selected.goal, goalOverride > 0, { showGoalDialog = false }) { value -> goalOverride = value ?: 0; save(); showGoalDialog = false }
            }
            if (showResetDialog) ConfirmDialog("بازنشانی شمارنده", "شمارندهٔ ذکر فعلی از ابتدا شروع شود؟ آمار کلی حذف نمی‌شود.", "بازنشانی", { showResetDialog = false }) { count = 0; stepIndex = 0; save(); showResetDialog = false }
            if (showDecreaseDialog) ConfirmDialog("کسر یک ذکر", "یک واحد از شمارش کم شود؟", "کم کن", { showDecreaseDialog = false }) { decrement(); showDecreaseDialog = false }
            if (creatingCustom) ZekrEditor(null, { creatingCustom = false }) { title, arabic, translation, goal ->
                customZekrs = customZekrs + Zekr("custom_${System.currentTimeMillis()}", title, arabic, translation, goal, "ذکر شخصی", custom = true)
                save(); creatingCustom = false
            }
            editor?.let { item -> ZekrEditor(item, { editor = null }) { title, arabic, translation, goal ->
                customZekrs = customZekrs.map { if (it.id == item.id) item.copy(title = title, arabic = arabic, translation = translation, goal = goal) else it }
                save(); editor = null
            } }
            deleteTarget?.let { item -> ConfirmDialog("حذف ذکر شخصی", "«${item.title}» حذف شود؟ این عمل بازگشت‌پذیر نیست.", "حذف", { deleteTarget = null }) {
                customZekrs = customZekrs.filterNot { it.id == item.id }
                if (selectedId == item.id) { selectedId = "salawat"; count = 0; stepIndex = 0 }
                save(); deleteTarget = null; showPicker = false
            } }
            if (clearStage == 1) ConfirmDialog("پاک‌کردن تاریخچه", "آمار و تاریخچهٔ ذخیره‌شده پاک شود؟", "ادامه", { clearStage = 0 }) { clearStage = 2 }
            if (clearStage == 2) ConfirmDialog("تأیید نهایی", "این کار قابل بازگشت نیست. تاریخچه پاک شود؟", "پاک کن", { clearStage = 0 }) { total = 0; completed = 0; daily = emptyMap(); save(); clearStage = 0 }
        }
    }
}

@Composable
private fun CounterScreen(
    modifier: Modifier,
    selectedTitle: String,
    arabic: String,
    translation: String,
    stage: String?,
    count: Int,
    target: Int,
    message: String?,
    vibration: Boolean,
    onVibration: (Boolean) -> Unit,
    onSelect: () -> Unit,
    onGoal: () -> Unit,
    onIncrement: () -> Unit,
    onDecrease: () -> Unit,
    onReset: () -> Unit
) {
    val progress = (count.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("ذکر انتخاب‌شده", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(selectedTitle, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
        Text(arabic, fontSize = 29.sp, lineHeight = 47.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 20.dp))
        Text(translation, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 9.dp))
        stage?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.height(22.dp))
        Text(formatNumber(count), fontSize = 79.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("هدف: ${formatNumber(target)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(MaterialTheme.colorScheme.secondaryContainer)) {
            Box(Modifier.fillMaxWidth(progress).height(8.dp).background(MaterialTheme.colorScheme.primary))
        }
        Text("${formatNumber((progress * 100).toInt())} درصد", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 7.dp))
        message?.let { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.padding(top = 12.dp)) { Text(it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer) } }
        Spacer(Modifier.weight(1f))
        Surface(
            modifier = Modifier.fillMaxWidth().height(105.dp).clip(RoundedCornerShape(28.dp)).pointerInput(Unit) {
                detectTapGestures(onTap = { onIncrement() }, onLongPress = { onDecrease() })
            },
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 5.dp
        ) { Box(contentAlignment = Alignment.Center) { Text("ذکر بگو", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) } }
        Text("برای کم‌کردن یک عدد، دکمه را نگه دارید.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 7.dp))
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onSelect, modifier = Modifier.weight(1f)) { Text("انتخاب ذکر") }
            OutlinedButton(onClick = onGoal, modifier = Modifier.weight(1f)) { Text("تعیین هدف") }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
            Text("لرزش هنگام شمارش", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(8.dp)); Switch(vibration, onVibration)
            TextButton(onClick = onReset) { Text("بازنشانی") }
        }
    }
}

@Composable
private fun StatsScreen(modifier: Modifier, today: Int, total: Int, completed: Int, daily: Map<String, Int>, onClear: () -> Unit) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        Text("آمار", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("امروز", formatNumber(today), Modifier.weight(1f)); StatCard("کل ذکرها", formatNumber(total), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp)); StatCard("هدف‌های کامل‌شده", formatNumber(completed), Modifier.fillMaxWidth())
        Spacer(Modifier.height(22.dp)); Text("روزهای اخیر", fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.weight(1f).padding(top = 8.dp)) {
            items(daily.entries.sortedByDescending { it.key }.take(30)) { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item.key); Text(formatNumber(item.value), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                HorizontalDivider()
            }
        }
        TextButton(onClick = onClear, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("پاک‌کردن کل تاریخچه", color = MaterialTheme.colorScheme.error) }
    }
}

@Composable private fun StatCard(title: String, value: String, modifier: Modifier) { Card(modifier) { Column(Modifier.padding(15.dp)) { Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun SettingsScreen(modifier: Modifier, vibration: Boolean, sound: Boolean, theme: String, resetAfter: Boolean, onVibration: (Boolean) -> Unit, onSound: (Boolean) -> Unit, onTheme: (String) -> Unit, onResetAfter: (Boolean) -> Unit) {
    Column(modifier.fillMaxSize().padding(18.dp).verticalScroll(rememberScrollState())) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SettingSwitch("لرزش هنگام شمارش", "بازخورد کوتاه هنگام لمس دکمه", vibration, onVibration)
        SettingSwitch("صدای ملایم", "صدای کوتاه و اختیاری هنگام لمس", sound, onSound)
        SettingSwitch("صفرشدن پس از هدف", "بعد از تکمیل هدف، شمارنده از صفر شروع شود", resetAfter, onResetAfter)
        Spacer(Modifier.height(18.dp)); Text("نمایش", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("system" to "سیستم", "light" to "روشن", "dark" to "تیره").forEach { (value, title) -> FilterChip(selected = theme == value, onClick = { onTheme(value) }, label = { Text(title) }) }
        }
        Spacer(Modifier.height(28.dp)); Text("ذکرشمار کاملاً آفلاین است. اطلاعات فقط روی همین گوشی ذخیره می‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun SettingSwitch(title: String, sub: String, checked: Boolean, change: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Medium); Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked, change) } }

@Composable
private fun ZekrPicker(allZekrs: List<Zekr>, selectedId: String, onDismiss: () -> Unit, onSelect: (Zekr) -> Unit, onCreate: () -> Unit, onEdit: (Zekr) -> Unit, onDelete: (Zekr) -> Unit) {
    var query by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 24.dp)) {
            Text("انتخاب ذکر", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(query, { query = it }, label = { Text("جست‌وجو") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), singleLine = true)
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("ساخت ذکر شخصی") }
            LazyColumn(Modifier.height(420.dp).padding(top = 10.dp)) {
                items(allZekrs.filter { it.title.contains(query, true) || it.arabic.contains(query, true) }) { item ->
                    Row(Modifier.fillMaxWidth().clickable { onSelect(item) }.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(item.title, fontWeight = if (item.id == selectedId) FontWeight.Bold else FontWeight.Normal); Text("هدف پیشنهادی: ${formatNumber(item.goal)}${if (item.steps.isNotEmpty()) " • سه‌مرحله‌ای" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (item.custom) { TextButton(onClick = { onEdit(item) }) { Text("ویرایش") }; TextButton(onClick = { onDelete(item) }) { Text("حذف") } } else if (item.id == selectedId) Text("انتخاب‌شده", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun GoalDialog(current: Int, suggested: Int, custom: Boolean, dismiss: () -> Unit, confirm: (Int?) -> Unit) {
    var text by remember { mutableStateOf(current.toString()) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("تعیین هدف") }, text = { Column { Text("هدف پیشنهادی: ${formatNumber(suggested)}"); Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 10.dp)) { listOf(10, 33, 100, 313, 1000).forEach { n -> FilterChip(text == n.toString(), { text = n.toString() }, { Text(formatNumber(n)) }) } }; OutlinedTextField(text, { text = it.filter(Char::isDigit) }, label = { Text("عدد دلخواه") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)); if (custom) TextButton(onClick = { confirm(null) }) { Text("بازگشت به هدف پیشنهادی") } } }, confirmButton = { TextButton(onClick = { text.toIntOrNull()?.takeIf { it > 0 }?.let(confirm) }) { Text("ثبت") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}

@Composable
private fun ZekrEditor(initial: Zekr?, dismiss: () -> Unit, save: (String, String, String, Int) -> Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "") }; var arabic by remember { mutableStateOf(initial?.arabic ?: "") }; var translation by remember { mutableStateOf(initial?.translation ?: "") }; var goal by remember { mutableStateOf((initial?.goal ?: 100).toString()) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (initial == null) "ذکر شخصی جدید" else "ویرایش ذکر شخصی") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(title, { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(arabic, { arabic = it }, label = { Text("متن ذکر") }, minLines = 2, modifier = Modifier.fillMaxWidth()); OutlinedTextField(translation, { translation = it }, label = { Text("ترجمه (اختیاری)") }, minLines = 2, modifier = Modifier.fillMaxWidth()); OutlinedTextField(goal, { goal = it.filter(Char::isDigit) }, label = { Text("هدف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { val n = goal.toIntOrNull(); if (title.isNotBlank() && arabic.isNotBlank() && n != null && n > 0) save(title, arabic, translation, n) }) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } })
}

@Composable private fun ConfirmDialog(title: String, text: String, action: String, dismiss: () -> Unit, confirm: () -> Unit) { AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Text(text) }, confirmButton = { TextButton(onClick = confirm) { Text(action) } }, dismissButton = { TextButton(onClick = dismiss) { Text("انصراف") } }) }

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance(Locale("fa", "IR")).format(value)
private fun persianToday(): String { val c = android.icu.util.PersianCalendar.getInstance(); return "%04d/%02d/%02d".format(c.get(android.icu.util.Calendar.YEAR), c.get(android.icu.util.Calendar.MONTH) + 1, c.get(android.icu.util.Calendar.DAY_OF_MONTH)) }
private fun readDaily(raw: String): Map<String, Int> = runCatching { JSONObject(raw).keys().asSequence().associateWith { JSONObject(raw).optInt(it) } }.getOrDefault(emptyMap())
private fun writeDaily(map: Map<String, Int>): String = JSONObject().also { json -> map.forEach { (k, v) -> json.put(k, v) } }.toString()
private fun readCustoms(raw: String): List<Zekr> = runCatching { val a = JSONArray(raw); List(a.length()) { i -> a.getJSONObject(i).let { o -> Zekr(o.getString("id"), o.getString("title"), o.getString("arabic"), o.optString("translation"), o.optInt("goal", 100), "ذکر شخصی", custom = true) } } }.getOrDefault(emptyList())
private fun writeCustoms(items: List<Zekr>): String = JSONArray().also { a -> items.forEach { z -> a.put(JSONObject().put("id", z.id).put("title", z.title).put("arabic", z.arabic).put("translation", z.translation).put("goal", z.goal)) } }.toString()
