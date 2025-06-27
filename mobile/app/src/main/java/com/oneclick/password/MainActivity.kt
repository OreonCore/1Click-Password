package com.oneclick.password

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import com.oneclick.password.ui.theme._1ClickPasswordTheme
import kotlinx.coroutines.delay
import java.util.Locale


private const val PREFS_NAME = "password_prefs"
private const val KEY_PASSWORD_LENGTH = "password_length"
private const val KEY_DARK_THEME = "dark_theme_enabled"

class MainActivity : ComponentActivity() {
    private var batteryOptimizationIgnored: MutableState<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val lang = getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", "uk")
        val locale = Locale(lang!!)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLength = prefs.getInt(KEY_PASSWORD_LENGTH, 24)

        val themePrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getBoolean(KEY_DARK_THEME, false)

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(savedTheme) }

            _1ClickPasswordTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val view = LocalView.current
                    LaunchedEffect(Unit) {
                        ViewCompat.setOnApplyWindowInsetsListener(view, null)
                    }

                    batteryOptimizationIgnored = remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        batteryOptimizationIgnored?.value = isIgnoringBatteryOptimizations()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            PasswordAnimationBanner()

                            PasswordLengthSetting(
                                initialLength = savedLength,
                                onLengthChange = { newLength ->
                                    prefs.edit().putInt(KEY_PASSWORD_LENGTH, newLength).apply()
                                }
                            )

                            if (batteryOptimizationIgnored?.value == false) {
                                BatteryOptimizationCard(
                                    onDisableOptimizationClick = { openBatteryOptimizationSettings() }
                                )
                            }

                            LanguageSwitchRow(
                                isDarkTheme = isDarkTheme,
                                onLanguageToggle = { toggleLanguage() },
                                onThemeToggle = {
                                    isDarkTheme = !isDarkTheme
                                    themePrefs.edit().putBoolean(KEY_DARK_THEME, isDarkTheme).apply()
                                },
                                tooltipText = "Інструмент створений мною і для мене...",
                                onTooltipClick = {
                                    val url = "https://github.com/OreonCore/1Click-Password"
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 🌀 Оновлюємо стан батареї після повернення з налаштувань
    override fun onResume() {
        super.onResume()
        batteryOptimizationIgnored?.value = isIgnoringBatteryOptimizations()
    }

    private fun toggleLanguage() {
        val currentLocale = resources.configuration.locales[0]
        val newLocale = if (currentLocale.language == "uk") Locale("en") else Locale("uk")
        val config = resources.configuration
        config.setLocale(newLocale)
        resources.updateConfiguration(config, resources.displayMetrics)

        getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("language", newLocale.language)
            .apply()

        val intent = intent
        finish()
        startActivity(intent)
    }

    // 🔧 Відкриваємо налаштування оптимізації батареї саме для цього застосунку
    private fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    // ✅ Перевірка: чи вимкнено оптимізацію
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    @Composable
    fun PasswordAnimationBanner() {
        val length = 8
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val password = remember {
            mutableStateListOf<Char>().apply { repeat(length) { add(chars.random()) } }
        }
        var currentIndex by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                password[currentIndex] = chars.random()
                currentIndex = (currentIndex + 1) % length
                delay(100)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "[" + password.joinToString("") + "]",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun PasswordLengthSetting(initialLength: Int, onLengthChange: (Int) -> Unit) {
        var length by remember { mutableStateOf(initialLength.toFloat()) }

        val context = LocalContext.current  // Отримуємо контекст

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Password icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.choose_password_length, length.toInt()),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Slider(
                    value = length,
                    onValueChange = {
                        length = it
                        onLengthChange(it.toInt())
                    },
                    valueRange = 6f..32f,
                    steps = 25,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val generatedPassword = PasswordGenerator.generatePassword(length.toInt())

                        // Запускаємо сервіс для копіювання пароля
                        val intent = Intent(context, SecureClipboardService::class.java).apply {
                            putExtra("PASSWORD", generatedPassword)
                        }
                        context.startService(intent)

                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(id = R.string.generate_password))
                }
            }
        }
    }

    @Composable
    fun BatteryOptimizationCard(onDisableOptimizationClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery icon",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.battery_optimization_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = stringResource(id = R.string.battery_optimization_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = onDisableOptimizationClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(id = R.string.disable_optimization))
                }
            }
        }
    }

    @Composable
    fun LanguageSwitchRow(
        isDarkTheme: Boolean,
        onLanguageToggle: () -> Unit,
        onThemeToggle: () -> Unit,
        tooltipText: String,
        onTooltipClick: () -> Unit
    ) {
        var showDialog by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            // 🔳 Кнопка перемикання теми (зліва)
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .clickable { onThemeToggle() },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = stringResource(id = R.string.theme_toggle),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 🔳 Кнопка мови (звужена)
            Button(
                onClick = onLanguageToggle,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(stringResource(id = R.string.language_switch))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 🔳 Кнопка підказки (справа)
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .clickable {
                        showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "?",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showDialog) {
            InfoDialogWithLink(
                text = tooltipText,
                onDismissRequest = { showDialog = false },
                onLinkClick = onTooltipClick
            )
        }
    }

    @Composable
    fun InfoDialogWithLink(text: String, onDismissRequest: () -> Unit, onLinkClick: () -> Unit) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.about_program_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.about_program_close))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.about_program_text),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            onLinkClick()
                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(id = R.string.about_program_github_button))
                    }
                }
            }
        }
    }
}