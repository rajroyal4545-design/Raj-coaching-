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

data class CloudAuthState(
    val isLoggedIn: Boolean = false,
    val email: String = "",
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

    private val prefs: SharedPreferences = context.getSharedPreferences("coaching_cloud_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadInitialState())
    val authState: StateFlow<CloudAuthState> = _authState.asStateFlow()

    private fun loadInitialState(): CloudAuthState {
        val savedApiKey = prefs.getString("firebase_api_key", "") ?: ""
        val savedProjectId = prefs.getString("firebase_project_id", "") ?: ""

        val firebaseApiKey = savedApiKey.ifBlank {
            runCatching { context.getString(com.example.R.string.google_api_key) }
                .getOrDefault("")
        }

        val firebaseProjectId = savedProjectId.ifBlank {
            runCatching { context.getString(com.example.R.string.project_id) }
                .getOrDefault("")
        }

        return CloudAuthState(
            isLoggedIn = prefs.getBoolean("is_logged_in", false),
            email = prefs.getString("user_email", "") ?: "",
            userId = prefs.getString("user_id", "") ?: "",
            lastSyncTime = prefs.getString("last_sync_time", "Never synced") ?: "Never synced",
            firebaseApiKey = firebaseApiKey,
            firebaseProjectId = firebaseProjectId
        )
    }

    fun saveFirebaseConfig(projectId: String, apiKey: String) {
        prefs.edit()
            .putString("firebase_project_id", projectId.trim())
            .putString("firebase_api_key", apiKey.trim())
            .apply()
        _authState.value = _authState.value.copy(
            firebaseProjectId = projectId.trim(),
            firebaseApiKey = apiKey.trim()
        )
    }

    suspend fun registerWithEmailPassword(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = _authState.value.firebaseApiKey
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Firebase API Key is required. Please set your Firebase Web API Key in Cloud Settings.")
            )
        }
        try {
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val body = JSONObject().apply {
                put("email", email.trim())
                put("password", pass.trim())
                put("returnSecureToken", true)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val response = BufferedReader(InputStreamReader(stream)).readText()

            val json = JSONObject(response)
            if (responseCode in 200..299) {
                val idToken = json.optString("idToken")
                val localId = json.optString("localId")
                val refreshToken = json.optString("refreshToken")

                saveLoginSession(email.trim(), localId, idToken, refreshToken)
                Result.success("Account created successfully!")
            } else {
                val errorMsg = json.optJSONObject("error")?.optString("message") ?: "Registration failed ($responseCode)"
                Result.failure(Exception(formatFirebaseError(errorMsg)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error during registration"))
        }
    }

    suspend fun loginWithEmailPassword(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = _authState.value.firebaseApiKey
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Firebase API Key is required. Please set your Firebase Web API Key in Cloud Settings.")
            )
        }
        try {
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val body = JSONObject().apply {
                put("email", email.trim())
                put("password", pass.trim())
                put("returnSecureToken", true)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val response = BufferedReader(InputStreamReader(stream)).readText()

            val json = JSONObject(response)
            if (responseCode in 200..299) {
                val idToken = json.optString("idToken")
                val localId = json.optString("localId")
                val refreshToken = json.optString("refreshToken")

                saveLoginSession(email.trim(), localId, idToken, refreshToken)
                Result.success("Login successful!")
            } else {
                val errorMsg = json.optJSONObject("error")?.optString("message") ?: "Login failed ($responseCode)"
                Result.failure(Exception(formatFirebaseError(errorMsg)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error during login"))
        }
    }

    private fun saveLoginSession(email: String, userId: String, idToken: String, refreshToken: String) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_id", userId)
            .putString("id_token", idToken)
            .putString("refresh_token", refreshToken)
            .apply()

        _authState.value = _authState.value.copy(
            isLoggedIn = true,
            email = email,
            userId = userId
        )
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_email")
            .remove("user_id")
            .remove("id_token")
            .remove("refresh_token")
            .apply()

        _authState.value = _authState.value.copy(
            isLoggedIn = false,
            email = "",
            userId = ""
        )
    }

    /**
     * Uploads the entire local coaching database to Firebase Realtime Database
     */
    suspend fun uploadToCloud(payload: CloudBackupPayload): Result<String> = withContext(Dispatchers.IO) {
        val projectId = _authState.value.firebaseProjectId
        val userId = _authState.value.userId
        val idToken = prefs.getString("id_token", "") ?: ""

        if (projectId.isBlank() || userId.isBlank()) {
            return@withContext Result.failure(Exception("Not logged in or Firebase Project ID not configured."))
        }

        try {
            _authState.value = _authState.value.copy(isSyncing = true, syncMessage = "Uploading data to Cloud...")

            val jsonPayload = serializePayload(payload)
            val urlString = "https://$projectId-default-rtdb.firebaseio.com/users/$userId/coaching_data.json" +
                (if (idToken.isNotBlank()) "?auth=$idToken" else "")
            val url = URL(urlString)

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 20000
                readTimeout = 20000
            }

            OutputStreamWriter(conn.outputStream).use { it.write(jsonPayload.toString()) }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                prefs.edit().putString("last_sync_time", nowStr).apply()
                _authState.value = _authState.value.copy(
                    isSyncing = false,
                    lastSyncTime = nowStr,
                    syncMessage = null
                )
                Result.success("Cloud Sync successful! ($nowStr)")
            } else {
                val err = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).readText()
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                Result.failure(Exception("Cloud upload failed ($responseCode): $err"))
            }
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
            Result.failure(Exception("Upload error: ${e.localizedMessage}"))
        }
    }

    /**
     * Downloads coaching data from Firebase Realtime Database
     */
    suspend fun downloadFromCloud(): Result<CloudBackupPayload> = withContext(Dispatchers.IO) {
        val projectId = _authState.value.firebaseProjectId
        val userId = _authState.value.userId
        val idToken = prefs.getString("id_token", "") ?: ""

        if (projectId.isBlank() || userId.isBlank()) {
            return@withContext Result.failure(Exception("Not logged in or Firebase Project ID not configured."))
        }

        try {
            _authState.value = _authState.value.copy(isSyncing = true, syncMessage = "Downloading data from Cloud...")

            val urlString = "https://$projectId-default-rtdb.firebaseio.com/users/$userId/coaching_data.json" +
                (if (idToken.isNotBlank()) "?auth=$idToken" else "")
            val url = URL(urlString)

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 20000
                readTimeout = 20000
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                if (response.isBlank() || response == "null") {
                    _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                    return@withContext Result.failure(Exception("No data found in Cloud for this account."))
                }
                val json = JSONObject(response)
                val payload = deserializePayload(json)
                val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                prefs.edit().putString("last_sync_time", nowStr).apply()
                _authState.value = _authState.value.copy(
                    isSyncing = false,
                    lastSyncTime = nowStr,
                    syncMessage = null
                )
                Result.success(payload)
            } else {
                val err = BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).readText()
                _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
                Result.failure(Exception("Download failed ($responseCode): $err"))
            }
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(isSyncing = false, syncMessage = null)
            Result.failure(Exception("Download error: ${e.localizedMessage}"))
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
            err.contains("EMAIL_EXISTS") -> "This email is already registered. Please sign in instead."
            err.contains("EMAIL_NOT_FOUND") -> "No account found with this email. Please register first."
            err.contains("INVALID_PASSWORD") || err.contains("INVALID_LOGIN_CREDENTIALS") -> "Invalid email or password."
            err.contains("WEAK_PASSWORD") -> "Password should be at least 6 characters long."
            err.contains("INVALID_EMAIL") -> "Please enter a valid email address."
            else -> err
        }
    }
}
