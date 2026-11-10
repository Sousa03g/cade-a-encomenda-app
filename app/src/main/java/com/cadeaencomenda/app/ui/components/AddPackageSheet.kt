package com.cadeaencomenda.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cadeaencomenda.app.R
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme

/**
 * Bottom sheet content for adding a new tracking package.
 * Supports clipboard pasting, nickname input, instant validation (minimum 8 characters),
 * and primary tracking action with loading indicator.
 */
@Composable
fun AddPackageSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onTrack: ((code: String, nickname: String?) -> Unit)? = null,
    onAdd: ((code: String, nickname: String) -> Unit)? = null
) {
    var trackingCode by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val trimmedCode = trackingCode.trim()
    val isCodeValid = trimmedCode.length >= 8
    val showCodeError = trackingCode.isNotBlank() && !isCodeValid

    fun handleSubmit() {
        if (!isCodeValid || isLoading) return
        keyboardController?.hide()
        val cleanedCode = trimmedCode.uppercase()
        val cleanedNickname = nickname.trim().ifEmpty { null }
        onTrack?.invoke(cleanedCode, cleanedNickname)
        onAdd?.invoke(cleanedCode, cleanedNickname ?: "")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Sheet Title and Subtitle
        Text(
            text = stringResource(R.string.add_package),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Insira o código de rastreamento para acompanhar as atualizações.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Tracking Code Input Field
        OutlinedTextField(
            value = trackingCode,
            onValueChange = { input ->
                // Automatically convert to uppercase and filter special spacing characters
                trackingCode = input.uppercase().filter { it.isLetterOrDigit() || it == '-' }
            },
            label = { Text(stringResource(R.string.tracking_code)) },
            placeholder = { Text("Ex: NL123456789BR") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trackingCode.isNotEmpty()) {
                        IconButton(onClick = { trackingCode = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar código",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let { pasted ->
                                trackingCode = pasted.trim().uppercase().filter { it.isLetterOrDigit() || it == '-' }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = "Colar da área de transferência",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            isError = showCodeError,
            supportingText = {
                if (showCodeError) {
                    Text(
                        text = "O código deve ter pelo menos 8 caracteres",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(text = "Correios, Jadlog, Sequoia, Loggi, etc.")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Optional Nickname Input Field
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text(stringResource(R.string.nickname_optional)) },
            placeholder = { Text("Ex: Smartphone novo, Presente de Aniversário") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (nickname.isNotEmpty()) {
                    IconButton(onClick = { nickname = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar apelido",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { handleSubmit() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Action Buttons (Submit & Cancel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(text = stringResource(R.string.cancel))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { handleSubmit() },
                enabled = isCodeValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Rastreando...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rastrear Encomenda",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPackageSheetPreview() {
    CadeAEncomendaTheme {
        AddPackageSheet(
            onDismiss = {},
            onTrack = { _, _ -> }
        )
    }
}
