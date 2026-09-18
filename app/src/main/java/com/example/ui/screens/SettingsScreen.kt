package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.PresentGreen
import com.example.ui.viewmodel.CoachingViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: CoachingViewModel) {
    val context = LocalContext.current
    val profile by viewModel.coachingProfile.collectAsState()

    var coachingName by remember(profile) { mutableStateOf(profile.coachingName) }
    var teacherName by remember(profile) { mutableStateOf(profile.teacherName) }
    var mobileNumber by remember(profile) { mutableStateOf(profile.mobileNumber) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var currencySymbol by remember(profile) { mutableStateOf(profile.currencySymbol) }

    var isSaved by remember { mutableStateOf(false) }

    val cloudAuth by viewModel.cloudAuthState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    var showCloudConfigDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Profile Form Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Coaching Institute Details / संस्थान विवरण",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = coachingName,
                    onValueChange = { coachingName = it },
                    label = { Text("Coaching Name / कोचिंग का नाम") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Teacher / Director Name / शिक्षक का नाम") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Contact Mobile / मोबाइल नंबर") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Coaching Address / पता") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currencySymbol,
                    onValueChange = { currencySymbol = it },
                    label = { Text("Currency Symbol (₹, Rs., $)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val updated = profile.copy(
                            coachingName = coachingName.trim(),
                            teacherName = teacherName.trim(),
                            mobileNumber = mobileNumber.trim(),
                            address = address.trim(),
                            currencySymbol = currencySymbol.trim().ifBlank { "₹" }
                        )
                        viewModel.updateCoachingProfile(updated)
                        isSaved = true
                        Toast.makeText(context, "Settings saved successfully! / सेटिंग्स सहेजी गईं", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(if (isSaved) Icons.Default.Check else Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSaved) "Saved! / सहेजा गया" else "Save Settings / सेटिंग्स सहेजें", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Multi-Device Cloud Sync Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (cloudAuth.isLoggedIn) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (cloudAuth.isLoggedIn) PresentGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Multi-Device Cloud Login",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "किसी भी डिवाइस से लॉगिन करें",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { showCloudConfigDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cloud Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (cloudAuth.isLoggedIn) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (cloudAuth.isLoggedIn) Icons.Default.Check else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (cloudAuth.isLoggedIn) PresentGreen else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (cloudAuth.isLoggedIn) "Logged in as: ${cloudAuth.email}" else "Status: Local / Offline Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (cloudAuth.isLoggedIn) Color(0xFF166534) else Color(0xFF334155)
                            )
                            Text(
                                text = if (cloudAuth.isLoggedIn) "Last synced: ${cloudAuth.lastSyncTime}" else "Login to sync data across all your phones & tablets",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (cloudAuth.isLoggedIn) Color(0xFF15803D) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                if (cloudAuth.isSyncing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = cloudAuth.syncMessage ?: "Syncing with cloud...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Action buttons based on auth state
                if (!cloudAuth.isLoggedIn) {
                    Button(
                        onClick = { showAuthDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Login / Register (लॉग इन या नया खाता)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.uploadToCloud { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload / भेजें", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.downloadFromCloud { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download / लोड", fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.logoutCloud()
                            Toast.makeText(context, "Logged out / लॉग आउट हो गए", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AbsentRed)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout from this device / इस डिवाइस से लॉग आउट करें")
                    }
                }
            }
        }

        // Direct Device-to-Device Backup & Transfer Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Direct Device Transfer & Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "फ़ोन से फ़ोन में डेटा बैकअप और ट्रांसफर करें",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportBackupJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("CoachingBackup", json)
                                clipboard.setPrimaryClip(clip)

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Coaching Data Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Backup File"))
                                Toast.makeText(context, "Backup copied & ready to share!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Data / बैकअप भेजें", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Data / बैकअप लोड करें", fontSize = 12.sp)
                    }
                }
            }
        }

        // Offline Data Safety Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PresentGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "100% Offline & Safe Data",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PresentGreen
                    )
                    Text(
                        text = "All student, attendance, and fee records are securely saved on your device in local SQLite database. No data is lost on app restart.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Dialogs
    if (showAuthDialog) {
        CloudAuthDialog(
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false },
            onOpenConfig = {
                showAuthDialog = false
                showCloudConfigDialog = true
            }
        )
    }

    if (showCloudConfigDialog) {
        CloudConfigDialog(
            viewModel = viewModel,
            onDismiss = { showCloudConfigDialog = false }
        )
    }

    if (showImportDialog) {
        ImportBackupDialog(
            viewModel = viewModel,
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
fun CloudAuthDialog(
    viewModel: CoachingViewModel,
    onDismiss: () -> Unit,
    onOpenConfig: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authState by viewModel.cloudAuthState.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (selectedTab == 0) "Login to Coaching Account" else "Create Coaching Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = { Text("Sign In / लॉग इन") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = { Text("Register / खाता बनाएँ") }
                    )
                }

                if (authState.firebaseApiKey.isBlank()) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "⚠️ Firebase Config Required",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Firebase Project ID & Web API Key are needed for cloud authentication.",
                                color = Color(0xFFB45309),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = onOpenConfig) {
                                Text("Configure Firebase Now / अभी सेट करें", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address / ईमेल") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password / पासवर्ड") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = AbsentRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isLoading) {
                        Text("Cancel / रद्द करें")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            if (selectedTab == 0) {
                                viewModel.loginCloud(email.trim(), password.trim()) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else {
                                viewModel.registerCloud(email.trim(), password.trim()) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (selectedTab == 0) "Sign In" else "Create Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CloudConfigDialog(
    viewModel: CoachingViewModel,
    onDismiss: () -> Unit
) {
    val authState by viewModel.cloudAuthState.collectAsState()
    var projectId by remember(authState) { mutableStateOf(authState.firebaseProjectId) }
    var apiKey by remember(authState) { mutableStateOf(authState.firebaseApiKey) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Firebase Cloud Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "अपने Firebase Console (https://console.firebase.google.com) से Project ID और Web API Key दर्ज करें। इससे सभी डिवाइसों में डेटा सिंक होगा।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = projectId,
                    onValueChange = { projectId = it },
                    label = { Text("Firebase Project ID") },
                    placeholder = { Text("e.g. my-coaching-app") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Web API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveFirebaseConfig(projectId.trim(), apiKey.trim())
                            Toast.makeText(context, "Firebase configuration saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Config", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ImportBackupDialog(
    viewModel: CoachingViewModel,
    onDismiss: () -> Unit
) {
    var backupJsonText by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Import / Restore Backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "दूसरे फ़ोन से भेजी गई बैकअप फ़ाइल का टेक्स्ट यहाँ पेस्ट करें। यह आपके सभी छात्र, हाजिरी और फ़ीस डेटा को इस फ़ोन में तुरंत रीस्टोर कर देगा।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = backupJsonText,
                    onValueChange = { backupJsonText = it },
                    label = { Text("Paste Backup JSON Text Here") },
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = AbsentRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isImporting) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (backupJsonText.isBlank()) {
                                errorMessage = "Please paste the backup text."
                                return@Button
                            }
                            isImporting = true
                            errorMessage = null
                            viewModel.importBackupJson(backupJsonText.trim()) { success, msg ->
                                isImporting = false
                                if (success) {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isImporting,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Restore Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
