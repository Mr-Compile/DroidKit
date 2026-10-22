package io.github.droidkit.feature.nfc

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Wifi
import io.github.droidkit.core.ui.components.DroidKitAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.droidkit.core.ui.components.DroidKitFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import io.github.droidkit.core.ui.components.DroidKitOutlinedButton
import io.github.droidkit.core.ui.components.DroidKitTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.droidkit.core.ui.InfoRow
import io.github.droidkit.core.ui.SectionCard
import io.github.droidkit.core.ui.components.DroidKitTopAppBar

private enum class FormKind(val label: String, val icon: ImageVector) {
    TEXT("Text", Icons.AutoMirrored.Outlined.ShortText),
    LINK("Link", Icons.Outlined.Link),
    PHONE("Phone", Icons.Outlined.Call),
    SMS("SMS", Icons.Outlined.Sms),
    EMAIL("Email", Icons.Outlined.Mail),
    CONTACT("Contact", Icons.Outlined.ContactPage),
    WIFI("Wi-Fi", Icons.Outlined.Wifi),
    GEO("Location", Icons.Outlined.Place),
    APP("App", Icons.Outlined.Android),
    MIME("MIME", Icons.Outlined.Description),
    RAW("Raw", Icons.Outlined.Code),
}

private val PACKAGE_RE = Regex("^[A-Za-z][\\w]*(\\.[\\w]+)+$")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NfcWriteScreen(
    onBack: () -> Unit,
    viewModel: NfcViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current as? Activity
    val armed by viewModel.armed.collectAsStateWithLifecycle()
    val actionResult by viewModel.actionResult.collectAsStateWithLifecycle()
    val tag by viewModel.tag.collectAsStateWithLifecycle()

    var kind by remember { mutableStateOf(FormKind.TEXT) }
    var specs by remember { mutableStateOf(listOf<NdefRecordSpec>()) }
    var confirmWrite by remember { mutableStateOf(false) }

    // Per-kind field state
    var text by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsBody by remember { mutableStateOf("") }
    var emailTo by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf(WifiSecurity.WPA2_PSK) }
    var wifiKey by remember { mutableStateOf("") }
    var wifiKeyVisible by remember { mutableStateOf(false) }
    var geoLat by remember { mutableStateOf("") }
    var geoLng by remember { mutableStateOf("") }
    var appPkg by remember { mutableStateOf("") }
    var mimeType by remember { mutableStateOf("") }
    var mimePayload by remember { mutableStateOf("") }
    var rawTnf by remember { mutableStateOf(1.toShort()) }
    var rawType by remember { mutableStateOf("") }
    var rawPayload by remember { mutableStateOf("") }

    // Reader mode stays active while this screen is resumed — armed writes
    // fire on the next presented tag.
    LifecycleResumeEffect(activity) {
        viewModel.refreshAdapterState()
        activity?.let { viewModel.startScan(it) }
        onPauseOrDispose { activity?.let { viewModel.stopScan(it) } }
    }

    fun validationError(): String? = when (kind) {
        FormKind.TEXT -> if (text.isBlank()) "Enter text" else null
        FormKind.LINK -> if (uri.isBlank()) "Enter a URL/URI" else null
        FormKind.PHONE -> if (phone.isBlank()) "Enter a phone number" else null
        FormKind.SMS -> if (smsNumber.isBlank()) "Enter a phone number" else null
        FormKind.EMAIL -> if (emailTo.isBlank()) "Enter a recipient" else null
        FormKind.CONTACT -> if (contactName.isBlank()) "Enter a name" else null
        FormKind.WIFI -> when {
            wifiSsid.isBlank() -> "Enter the SSID"
            wifiSecurity != WifiSecurity.OPEN && wifiKey.length < 8 ->
                "Key must be at least 8 characters"
            else -> null
        }
        FormKind.GEO -> when {
            geoLat.toDoubleOrNull() == null -> "Invalid latitude"
            geoLng.toDoubleOrNull() == null -> "Invalid longitude"
            else -> null
        }
        FormKind.APP -> if (!PACKAGE_RE.matches(appPkg.trim())) {
            "Enter a package name (e.g. com.example.app)"
        } else null
        FormKind.MIME -> if ('/' !in mimeType.trim()) {
            "Enter a MIME type (e.g. text/plain)"
        } else null
        FormKind.RAW -> when {
            NfcTagParser.hexToBytes(rawType) == null -> "Invalid type hex"
            NfcTagParser.hexToBytes(rawPayload) == null -> "Invalid payload hex"
            else -> null
        }
    }

    fun currentSpec(): NdefRecordSpec = when (kind) {
        FormKind.TEXT -> NdefRecordSpec.Text(text)
        FormKind.LINK -> NdefRecordSpec.Uri(uri.trim())
        FormKind.PHONE -> NdefRecordSpec.Phone(phone.trim())
        FormKind.SMS -> NdefRecordSpec.Sms(smsNumber.trim(), smsBody)
        FormKind.EMAIL -> NdefRecordSpec.Email(emailTo.trim(), emailSubject, emailBody)
        FormKind.CONTACT -> NdefRecordSpec.Contact(
            contactName.trim(), contactPhone.trim(), contactEmail.trim(), contactOrg.trim(),
        )
        FormKind.WIFI -> NdefRecordSpec.Wifi(wifiSsid.trim(), wifiSecurity, wifiKey)
        FormKind.GEO -> NdefRecordSpec.Geo(geoLat.trim(), geoLng.trim())
        FormKind.APP -> NdefRecordSpec.App(appPkg.trim())
        FormKind.MIME -> NdefRecordSpec.Mime(mimeType.trim(), mimePayload)
        FormKind.RAW -> NdefRecordSpec.Raw(rawTnf, rawType.trim(), rawPayload.trim())
    }

    val error = validationError()
    val message = runCatching { ndefMessageOf(specs) }.getOrNull()
    val sizeBytes = message?.toByteArray()?.size ?: 0
    val capacity = tag?.ndefMaxSize

    Scaffold(topBar = { DroidKitTopAppBar("NFC Write", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionCard(title = "Add record") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormKind.entries.forEach { k ->
                            DroidKitFilterChip(
                                selected = kind == k,
                                onClick = { kind = k },
                                label = { Text(k.label) },
                                leadingIcon = {
                                    Icon(k.icon, contentDescription = null)
                                },
                            )
                        }
                    }

                    when (kind) {
                        FormKind.TEXT -> Field(
                            text, { text = it }, "Text content",
                        )
                        FormKind.LINK -> Column {
                            Field(uri, { uri = it }, "https://example.com", KeyboardType.Uri)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LINK_PRESETS.take(6).forEach { (label, prefix) ->
                                    DroidKitFilterChip(
                                        selected = false,
                                        onClick = { if (!uri.startsWith(prefix)) uri = prefix },
                                        label = { Text(label) },
                                    )
                                }
                            }
                        }
                        FormKind.PHONE -> Field(
                            phone, { phone = it }, "+1 555 0100", KeyboardType.Phone,
                        )
                        FormKind.SMS -> Column {
                            Field(smsNumber, { smsNumber = it }, "+1 555 0100", KeyboardType.Phone)
                            Field(smsBody, { smsBody = it }, "Message (optional)")
                        }
                        FormKind.EMAIL -> Column {
                            Field(emailTo, { emailTo = it }, "name@example.com", KeyboardType.Email)
                            Field(emailSubject, { emailSubject = it }, "Subject (optional)")
                            Field(emailBody, { emailBody = it }, "Body (optional)")
                        }
                        FormKind.CONTACT -> Column {
                            Field(contactName, { contactName = it }, "Full name")
                            Field(contactPhone, { contactPhone = it }, "Phone", KeyboardType.Phone)
                            Field(contactEmail, { contactEmail = it }, "Email", KeyboardType.Email)
                            Field(contactOrg, { contactOrg = it }, "Organization (optional)")
                        }
                        FormKind.WIFI -> Column {
                            Field(wifiSsid, { wifiSsid = it }, "Network name (SSID)")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WifiSecurity.entries.forEach { sec ->
                                    DroidKitFilterChip(
                                        selected = wifiSecurity == sec,
                                        onClick = { wifiSecurity = sec },
                                        label = { Text(sec.label) },
                                    )
                                }
                            }
                            if (wifiSecurity != WifiSecurity.OPEN) {
                                DroidKitTextField(
                                    value = wifiKey,
                                    onValueChange = { wifiKey = it },
                                    label = { Text("Password") },
                                    singleLine = true,
                                    visualTransformation = if (wifiKeyVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { wifiKeyVisible = !wifiKeyVisible }) {
                                            Icon(
                                                if (wifiKeyVisible) {
                                                    Icons.Outlined.VisibilityOff
                                                } else {
                                                    Icons.Outlined.Visibility
                                                },
                                                contentDescription = "Toggle password visibility",
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                )
                            }
                            Text(
                                "Tags are readable by anyone who scans them — avoid " +
                                    "writing credentials you would not share.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        FormKind.GEO -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DroidKitTextField(
                                value = geoLat,
                                onValueChange = { geoLat = it },
                                label = { Text("Latitude") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                ),
                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                            )
                            DroidKitTextField(
                                value = geoLng,
                                onValueChange = { geoLng = it },
                                label = { Text("Longitude") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                ),
                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                            )
                        }
                        FormKind.APP -> Column {
                            Field(appPkg, { appPkg = it }, "com.example.app")
                            Text(
                                "App records open the app on tap (or its Play Store " +
                                    "page). Modern Android prefers Link records — use " +
                                    "App only for legacy tag readers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        FormKind.MIME -> Column {
                            Field(mimeType, { mimeType = it }, "text/plain")
                            Field(mimePayload, { mimePayload = it }, "Payload (UTF-8 text)")
                        }
                        FormKind.RAW -> Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    0.toShort() to "Empty",
                                    1.toShort() to "WellKnown",
                                    2.toShort() to "MIME",
                                    3.toShort() to "AbsURI",
                                    4.toShort() to "External",
                                    6.toShort() to "Unknown",
                                ).forEach { (tnf, label) ->
                                    DroidKitFilterChip(
                                        selected = rawTnf == tnf,
                                        onClick = { rawTnf = tnf },
                                        label = { Text(label) },
                                    )
                                }
                            }
                            Field(rawType, { rawType = it }, "Type hex (e.g. 55 for 'U')")
                            Field(rawPayload, { rawPayload = it }, "Payload hex (e.g. 00 FF A3)")
                        }
                    }

                    error?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    DroidKitOutlinedButton(
                        onClick = { specs = specs + currentSpec() },
                        enabled = error == null,
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text("Add to message") }
                }
            }

            item {
                SectionCard(title = "Message (${specs.size} record${if (specs.size == 1) "" else "s"})") {
                    if (specs.isEmpty()) {
                        Text(
                            "No records yet — pick a type above and add one. " +
                                "The write replaces all NDEF content on the presented tag.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        specs.forEachIndexed { i, spec ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "#${i + 1} · ${spec.summary()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = {
                                    specs = specs.toMutableList().also { it.removeAt(i) }
                                }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Preview") {
                    InfoRow("Records", specs.size.toString(), copyable = false)
                    InfoRow("Message size", "$sizeBytes bytes", copyable = false)
                    if (capacity != null) {
                        InfoRow(
                            "Last tag capacity",
                            "$capacity bytes — " +
                                if (sizeBytes <= capacity) "fits" else "does not fit",
                            copyable = false,
                        )
                    }
                    DroidKitOutlinedButton(
                        onClick = { confirmWrite = true },
                        enabled = specs.isNotEmpty() && armed == null && message != null,
                        modifier = Modifier.padding(top = 8.dp),
                    ) { Text("Write to tag") }
                }
            }

            if (armed != null) {
                item {
                    SectionCard(title = "Armed") {
                        Text(
                            "Present the target tag to the back of the device now.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        DroidKitOutlinedButton(
                            onClick = { viewModel.disarm() },
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("Cancel") }
                    }
                }
            }

            actionResult?.let { r ->
                item {
                    SectionCard(title = "Result") {
                        Text(
                            when (r) {
                                is NfcViewModel.ActionResult.Success -> r.text
                                is NfcViewModel.ActionResult.Failed -> "Failed: ${r.text}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (r is NfcViewModel.ActionResult.Failed) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }

    if (confirmWrite) {
        DroidKitAlertDialog(
            onDismissRequest = { confirmWrite = false },
            title = { Text("Write to tag?") },
            text = {
                Text(
                    "This writes a $sizeBytes-byte NDEF message (${specs.size} records) " +
                        "to the next presented tag, replacing its current content. " +
                        "The tag must be writable and have enough capacity.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmWrite = false
                    message?.let { viewModel.armWrite(it, sizeBytes) }
                }) { Text("Arm write") }
            },
            dismissButton = {
                TextButton(onClick = { confirmWrite = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    DroidKitTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    )
}
