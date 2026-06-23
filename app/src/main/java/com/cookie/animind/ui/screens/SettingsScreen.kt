package com.cookie.animind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookie.animind.ui.AnimeViewModel

import com.cookie.animind.ui.Strings

import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit // Kept for signature compatibility optionally
) {
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle(initialValue = "")
    var localApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var keySaveState by remember { mutableStateOf(0) } // 0 idle, 1 loading, 2 success, 3 error
    val scope = rememberCoroutineScope()
    
    val responseMode by viewModel.responseMode.collectAsStateWithLifecycle(initialValue = "Curta")
    val useAi by viewModel.useAi.collectAsStateWithLifecycle(initialValue = true)
    val language by viewModel.language.collectAsStateWithLifecycle(initialValue = if (java.util.Locale.getDefault().language == "pt") "Português" else "English")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp))
            
            Text(Strings.get("settings_title", language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // AI Toggle
            SettingCard(
                title = Strings.get("use_ai", language),
                description = Strings.get("use_ai_desc", language),
                control = {
                    Switch(
                        checked = useAi,
                        onCheckedChange = { viewModel.updateUseAi(it) }
                    )
                }
            )

            // API Key
            SettingCard(
                title = Strings.get("api_key", language),
                description = Strings.get("api_key_desc", language),
                control = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = localApiKey,
                            onValueChange = { 
                                localApiKey = it 
                                keySaveState = 0
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text(Strings.get("api_key_hint", language)) },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                    Icon(
                                        imageVector = if (apiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (keySaveState) {
                                    1 -> if (language == "Português") "Verificando..." else "Verifying..."
                                    2 -> if (language == "Português") "Salvo com sucesso!" else "Saved successfully!"
                                    3 -> if (language == "Português") "Chave inválida." else "Invalid key."
                                    else -> ""
                                },
                                color = if (keySaveState == 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    if (localApiKey.isBlank() || localApiKey.length < 10) {
                                        keySaveState = 3
                                    } else {
                                        scope.launch {
                                            keySaveState = 1
                                            delay(500) // Simulate validation delay
                                            viewModel.updateApiKey(localApiKey)
                                            keySaveState = 2
                                        }
                                    }
                                },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Text(if (language == "Português") "Salvar" else "Save")
                            }
                        }
                    }
                }
            )

            // Response Mode
            SettingCard(
                title = Strings.get("response_mode", language),
                description = Strings.get("response_mode_desc", language),
                control = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Curta" to "short", "Detalhada" to "detailed", "Recomendação Direta" to "direct_recommendation").forEach { (mode, key) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = responseMode == mode,
                                    onClick = { viewModel.updateResponseMode(mode) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(Strings.get(key, language), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            )

            // Language
            SettingCard(
                title = Strings.get("language", language),
                description = Strings.get("language_desc", language),
                control = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Português", "English").forEach { lang ->
                            FilterChip(
                                selected = language == lang,
                                onClick = { viewModel.updateLanguage(lang) },
                                label = { Text(lang) },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    control: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            control()
        }
    }
}
