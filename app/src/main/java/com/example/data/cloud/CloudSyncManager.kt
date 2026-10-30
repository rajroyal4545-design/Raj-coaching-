package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AttendanceRecord
import com.example.data.model.CoachingProfile
import com.example.data.model.FeePayment
import com.example.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Activity
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class CloudAuthState(
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val displayName: String = "",
    val userId: String = "",
    val lastSyncTime: String = "",
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val firebaseApiKey: String = "",
    val firebaseProjectId: String = ""
)

data class CloudBackupPayload(
    val profile: CoachingProfile,
    val students: List<Student>,
    val attendance: List<AttendanceRecord>,
    val payments: List<FeePayment>,
    val exportedAt: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)

class CloudSyncManager(private val context: Context) {

    companion object {
        private const val FIREBASE_API_KEY = "AIzaSyCSTtTdZ3k1jzVYvBL8Bii3Rg3sYpTUo7A"
        private const val FIREBASE_PROJECT_ID = "gen-lang-client-0496874569"
    }

    private fun getFirestoreDb(): FirebaseFirestore {
        val databaseId = try {
            context.getString(com.example.R.string.firestore_database_id)
        } catch (e: Exception) {
            "ai-studio-android-coaching-e2b23df1-a131-473d-8224-7423cd153511"
        }
        val app = try { FirebaseApp.getInstance() } catch (e: Exception) { FirebaseApp.initializeApp(context)!! }
        return FirebaseFirestore.getInstance(app, databaseId)
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("coaching_cloud_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadInitialState())
    val authState: StateFlow<CloudAuthState> = _authState.asStateFlow()

    private fun loadInitialState(): CloudAuthState {
        val fbUser: FirebaseUser? = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }
        val isFirebaseLoggedIn = fbUser != null
        val storedLoggedIn = prefs.getBoolean("is_logged_in", false)
        val isLoggedIn = isFirebaseLoggedIn || storedLoggedIn
        val email = fbUser?.email ?: prefs.getString("user_email", "") ?: ""
        val displayName = fbUser?.displayName ?: prefs.getString("user_display_name", "") ?: ""
        val userId = fbUser?.uid ?: prefs.getString("user_id", "") ?: ""

        return CloudAuthState(
            isLoggedIn = isLoggedIn,
            email = email,
            displayName = displayName,
            userId = userId,
            lastSyncTime = prefs.getString("last_sync_time", "Never synced") ?: "Never synced",
            firebaseApiKey = FIREBASE_API_KEY,
            firebaseProjectId = FIREBASE_PROJECT_ID
        )
    }

    fun saveFirebaseConfig(projectId: String, apiKey: String) {
        // Kept for compatibility; credentials are fixed directly in code
    }

    /**
     * Google Sign-In via Jetpack CredentialManager (GetSignInWithGoogleOption)
     * Authenticates with Google identity provider and links with Firebase Authentication.
     */
    fun signInWithGoogle(
        activity: Activity,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onAuthError: (String) -> Unit,
        onAuthCancelled: () -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val credentialManager = CredentialManager.create(activity)
                val fallbackClientId = if (com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank() && !com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID.contains("REPLACE")) {
                    com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID
                } else {
                    com.example.BuildConfig.WEB_CLIENT_ID
                }
                val serverClientId = try {
                    val resId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
                    if (resId != 0) {
                        val clientStr = activity.getString(resId)
                        if (clientStr.isNotBlank()) clientStr else fallbackClientId
                    } else {
                        fallbackClientId
                    }
                } catch (e: Exception) {
                    fallbackClientId
                }

                val googleIdOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = serverClientId
                ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val auth = try {
                        FirebaseAuth.getInstance()
                    } catch (e: Exception) {
                        val app = try { FirebaseApp.getInstance() } catch (_: Exception) { FirebaseApp.initializeApp(activity.applicationContext)!! }
                        FirebaseAuth.getInstance(app)
                    }

                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        user.getIdToken(true).addOnCompleteListener { tokenTask ->
                            val token = tokenTask.result?.token ?: idToken
                            saveLoginSession(
                                email = user.email ?: "",
                                userId = user.uid,
                                idToken = token,
                                refreshToken = "",
                                displayName = user.displayName ?: ""
                            )
                            _authState.value = _authState.value.copy(
                                isSyncing = false,
                                syncMessage = null
                            )
                            onAuthSuccess(user)
                        }
                    } else {
                        _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                        onAuthError("Firebase authentication failed: User is null")
                    }
                } else {
                    _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                    onAuthError("Unexpected credential format received")
                }
            } catch (e: NoCredentialException) {
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                Log.w("CloudSyncManager", "No Google account found on device: ${e.message}")
                onAuthError("No Google account found on device. Please add a Google account in phone Settings.")
            } catch (e: GetCredentialCancellationException) {
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                Log.i("CloudSyncManager", "User cancelled Google Sign-In prompt")
                onAuthCancelled()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                Log.e("CloudSyncManager", "Google Sign-In failed: ${e.message}", e)
                onAuthError(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    private fun saveLoginSession(
        email: String,
        userId: String,
        idToken: String,
        refreshToken: String,
        displayName: String = ""
    ) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_display_name", displayName)
            .putString("user_id", userId)
            .putString("id_token", idToken)
            .putString("refresh_token", refreshToken)
            .apply()

        _authState.value = _authState.value.copy(
            isLoggedIn = true,
            email = email,
            displayName = displayName,
            userId = userId
        )
    }

    fun logout(activity: Activity? = null) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            // Ignore if Firebase instance is not initialized
        }

        if (activity != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val credentialManager = CredentialManager.create(activity)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    // Ignore clear failure
                }
            }
        }

        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_email")
            .remove("user_display_name")
            .remove("user_id")
            .remove("id_token")
            .remove("refresh_token")
            .apply()

        _authState.value = _authState.value.copy(
            isLoggedIn = false,
            email = "",
            displayName = "",
            userId = ""
        )
    }

    /**
     * Client-Side Code Check and Force-Refreshing ID Token (Matching decompilation verification pattern):
     * 1. Query FirebaseAuth instance. If currentUser is null, upload is blocked and login dialog is shown.
     * 2. Force-refresh token via user.getIdToken(true) to verify session is active and not revoked.
     * 3. Start upload process only once token is validated with Firebase Auth backend.
     */
    fun checkAuthAndUpload(
        payload: CloudBackupPayload,
        showLoginRequiredDialog: () -> Unit,
        handleSessionExpired: () -> Unit,
        startUploadProcess: (validToken: String) -> Unit
    ) {
        val user: FirebaseUser? = try {
            FirebaseAuth.getInstance().currentUser
        } catch (e: Exception) {
            null
        }

        if (user != null) {
            // User is signed in, force-refresh ID token to ensure active session
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token ?: ""
                    // Token is validated with Firebase Auth backend; proceed to upload
                    startUploadProcess(token)
                } else {
                    // Token is invalid or expired; redirect to login
                    logout()
                    handleSessionExpired()
                }
            }
        } else {
            // User is not signed in; redirect to login screen
            showLoginRequiredDialog()
        }
    }

    /**
     * Uploads the entire local coaching database to Cloud Firestore with verified authentication.
     */
    suspend fun uploadToCloud(payload: CloudBackupPayload): Result<String> = withContext(Dispatchers.IO) {
        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        val user: FirebaseUser? = auth?.currentUser

        // 1. Client-Side Code Check: Query FirebaseAuth instance
        if (user == null && !prefs.getBoolean("is_logged_in", false)) {
            return@withContext Result.failure(Exception("Authentication required: currentUser is null. Please login first."))
        }

        // 2. Force-Refreshing the ID Token before upload
        val validatedToken: String = if (user != null) {
            val tokenResult = suspendCancellableCoroutine<Result<String>> { cont ->
                user.getIdToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result?.token ?: ""
                        if (cont.isActive) cont.resume(Result.success(token))
                    } else {
                        val err = task.exception?.message ?: "Session invalid or expired"
                        if (cont.isActive) cont.resume(Result.failure(Exception(err)))
                    }
                }
            }
            if (tokenResult.isFailure) {
                logout()
                return@withContext Result.failure(Exception("Session expired or token invalid. Please login again."))
            }
            tokenResult.getOrNull() ?: ""
        } else {
            prefs.getString("id_token", "") ?: ""
        }

        val userId = user?.uid ?: prefs.getString("user_id", "") ?: ""
        if (userId.isBlank()) {
            return@withContext Result.failure(Exception("Authentication required: Not signed in with valid credentials."))
        }

        startUploadProcess(payload, userId, validatedToken)
    }

    /**
     * Executes the authenticated payload upload to Cloud Firestore
     */
    suspend fun startUploadProcess(
        payload: CloudBackupPayload,
        userId: String,
        idToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            _authState.value = _authState.value.copy(isSyncing = true, syncMessage = "Uploading data to Cloud...")

            val jsonPayload = serializePayload(payload)
            val db = getFirestoreDb()
            val backupDoc = db.collection("users").document(userId).collection("coaching_data").document("backup")

            val docData = hashMapOf<String, Any>(
                "payload" to jsonPayload.toString(),
                "exportedAt" to payload.exportedAt,
                "studentCount" to payload.students.size,
                "attendanceCount" to payload.attendance.size,
                "paymentCount" to payload.payments.size,
                "userId" to userId,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            backupDoc.set(docData).await()

            val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            prefs.edit()
                .putString("last_sync_time", nowStr)
                .putString("id_token", idToken)
                .apply()
            _authState.value = _authState.value.copy(
                isSyncing = false,
                lastSyncTime = nowStr,
                syncMessage = null
            )
            Result.success("Cloud Sync successful! ($nowStr)")
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
            Result.failure(Exception("Cloud upload failed: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Downloads coaching data from Cloud Firestore
     */
    suspend fun downloadFromCloud(): Result<CloudBackupPayload> = withContext(Dispatchers.IO) {
        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        val user = auth?.currentUser
        val userId = user?.uid ?: _authState.value.userId

        if (userId.isBlank()) {
            return@withContext Result.failure(Exception("Not logged in. Please sign in first."))
        }

        try {
            _authState.value = _authState.value.copy(isSyncing = true, syncMessage = "Downloading data from Cloud...")

            val db = getFirestoreDb()
            val backupDoc = db.collection("users").document(userId).collection("coaching_data").document("backup")
            val snapshot = backupDoc.get().await()

            if (!snapshot.exists()) {
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                return@withContext Result.failure(Exception("No data found in Cloud for this account."))
            }

            val payloadStr = snapshot.getString("payload")
            if (payloadStr.isNullOrBlank()) {
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                return@withContext Result.failure(Exception("Corrupted or empty backup in Cloud."))
            }

            val json = JSONObject(payloadStr)
            val payload = deserializePayload(json)
            val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            prefs.edit().putString("last_sync_time", nowStr).apply()
            _authState.value = _authState.value.copy(
                isSyncing = false,
                lastSyncTime = nowStr,
                syncMessage = null
            )
            Result.success(payload)
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
            Result.failure(Exception("Download failed: ${e.localizedMessage ?: e.message}"))
        }
    }

    /**
     * Serializes coaching data into a formatted JSON string for local file export or sharing.
     */
    fun exportBackupJson(payload: CloudBackupPayload): String {
        return serializePayload(payload).toString(2)
    }

    /**
     * Deserializes coaching data from a JSON string.
     */
    fun importBackupJson(jsonString: String): Result<CloudBackupPayload> {
        return try {
            val json = JSONObject(jsonString)
            Result.success(deserializePayload(json))
        } catch (e: Exception) {
            Result.failure(Exception("Invalid backup format: ${e.localizedMessage}"))
        }
    }

    private fun serializePayload(payload: CloudBackupPayload): JSONObject {
        val root = JSONObject()
        root.put("exportedAt", payload.exportedAt)

        // Profile
        val prof = JSONObject().apply {
            put("id", payload.profile.id)
            put("coachingName", payload.profile.coachingName)
            put("teacherName", payload.profile.teacherName)
            put("mobileNumber", payload.profile.mobileNumber)
            put("address", payload.profile.address)
            put("currencySymbol", payload.profile.currencySymbol)
        }
        root.put("profile", prof)

        // Students
        val studentsArr = JSONArray()
        payload.students.forEach { s ->
            val sJson = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("guardianName", s.guardianName)
                put("mobileNumber", s.mobileNumber)
                put("address", s.address)
                put("studentClass", s.studentClass)
                put("batch", s.batch)
                put("rollNumber", s.rollNumber)
                put("joiningDate", s.joiningDate)
                put("monthlyFee", s.monthlyFee)
                put("isActive", s.isActive)
                put("avatarColorHex", s.avatarColorHex)
            }
            studentsArr.put(sJson)
        }
        root.put("students", studentsArr)

        // Attendance
        val attendanceArr = JSONArray()
        payload.attendance.forEach { a ->
            val aJson = JSONObject().apply {
                put("id", a.id)
                put("studentId", a.studentId)
                put("date", a.date)
                put("status", a.status)
                put("note", a.note)
            }
            attendanceArr.put(aJson)
        }
        root.put("attendance", attendanceArr)

        // Payments
        val paymentsArr = JSONArray()
        payload.payments.forEach { p ->
            val pJson = JSONObject().apply {
                put("id", p.id)
                put("studentId", p.studentId)
                put("amountPaid", p.amountPaid)
                put("paymentDate", p.paymentDate)
                put("forMonthYear", p.forMonthYear)
                put("paymentMode", p.paymentMode)
                put("remarks", p.remarks)
            }
            paymentsArr.put(pJson)
        }
        root.put("payments", paymentsArr)

        return root
    }

    private fun deserializePayload(json: JSONObject): CloudBackupPayload {
        // Profile
        val profJson = json.optJSONObject("profile")
        val profile = if (profJson != null) {
            CoachingProfile(
                id = profJson.optInt("id", 1),
                coachingName = profJson.optString("coachingName", "My Coaching Center"),
                teacherName = profJson.optString("teacherName", "Teacher"),
                mobileNumber = profJson.optString("mobileNumber", ""),
                address = profJson.optString("address", ""),
                currencySymbol = profJson.optString("currencySymbol", "₹")
            )
        } else {
            CoachingProfile()
        }

        // Students
        val students = mutableListOf<Student>()
        val studentsArr = json.optJSONArray("students")
        if (studentsArr != null) {
            for (i in 0 until studentsArr.length()) {
                val sObj = studentsArr.optJSONObject(i) ?: continue
                students.add(
                    Student(
                        id = sObj.optLong("id", 0L),
                        name = sObj.optString("name", ""),
                        guardianName = sObj.optString("guardianName", ""),
                        mobileNumber = sObj.optString("mobileNumber", ""),
                        address = sObj.optString("address", ""),
                        studentClass = sObj.optString("studentClass", ""),
                        batch = sObj.optString("batch", ""),
                        rollNumber = sObj.optString("rollNumber", ""),
                        joiningDate = sObj.optString("joiningDate", ""),
                        monthlyFee = sObj.optDouble("monthlyFee", 1000.0),
                        isActive = sObj.optBoolean("isActive", true),
                        avatarColorHex = sObj.optString("avatarColorHex", "#2563EB")
                    )
                )
            }
        }

        // Attendance
        val attendance = mutableListOf<AttendanceRecord>()
        val attendanceArr = json.optJSONArray("attendance")
        if (attendanceArr != null) {
            for (i in 0 until attendanceArr.length()) {
                val aObj = attendanceArr.optJSONObject(i) ?: continue
                attendance.add(
                    AttendanceRecord(
                        id = aObj.optLong("id", 0L),
                        studentId = aObj.optLong("studentId", 0L),
                        date = aObj.optString("date", ""),
                        status = aObj.optString("status", "PRESENT"),
                        note = aObj.optString("note", "")
                    )
                )
            }
        }

        // Payments
        val payments = mutableListOf<FeePayment>()
        val paymentsArr = json.optJSONArray("payments")
        if (paymentsArr != null) {
            for (i in 0 until paymentsArr.length()) {
                val pObj = paymentsArr.optJSONObject(i) ?: continue
                payments.add(
                    FeePayment(
                        id = pObj.optLong("id", 0L),
                        studentId = pObj.optLong("studentId", 0L),
                        amountPaid = pObj.optDouble("amountPaid", 0.0),
                        paymentDate = pObj.optString("paymentDate", ""),
                        forMonthYear = pObj.optString("forMonthYear", ""),
                        paymentMode = pObj.optString("paymentMode", "Cash"),
                        remarks = pObj.optString("remarks", "")
                    )
                )
            }
        }

        return CloudBackupPayload(
            profile = profile,
            students = students,
            attendance = attendance,
            payments = payments,
            exportedAt = json.optString("exportedAt", "")
        )
    }

    private fun formatFirebaseError(err: String): String {
        return when {
            err.contains("EMAIL_EXISTS") -> "यह ईमेल पहले से पंजीकृत है। कृपया लॉग इन करें। (This email is already registered. Please sign in instead.)"
            err.contains("EMAIL_NOT_FOUND") -> "इस ईमेल से कोई खाता नहीं मिला। कृपया सही ईमेल दर्ज करें या खाता बनाएँ। (No account found with this email.)"
            err.contains("INVALID_PASSWORD") || err.contains("INVALID_LOGIN_CREDENTIALS") -> "गलत ईमेल या पासवर्ड। (Invalid email or password.)"
            err.contains("WEAK_PASSWORD") -> "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए। (Password should be at least 6 characters long.)"
            err.contains("INVALID_EMAIL") -> "कृपया एक मान्य ईमेल पता दर्ज करें। (Please enter a valid email address.)"
            err.contains("TOO_MANY_ATTEMPTS_TRY_LATER") -> "बहुत अधिक प्रयास। कृपया कुछ समय बाद पुनः प्रयास करें। (Too many attempts. Please try again later.)"
            else -> err
        }
    }
}
