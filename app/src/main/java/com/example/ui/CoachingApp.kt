package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.viewmodel.CoachingViewModel

enum class AppTab(
    val title: String,
    val hindiTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", "होम", Icons.Filled.Home, Icons.Outlined.Home),
    STUDENTS("Students", "छात्र", Icons.Filled.People, Icons.Outlined.People),
    ATTENDANCE("Attendance", "हाजिरी", Icons.Filled.EventNote, Icons.Outlined.EventNote),
    FEES("Fees", "फीस", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    REPORTS("Reports", "रिपोर्ट", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    SETTINGS("Settings", "सेटिंग्स", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingApp(viewModel: CoachingViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    val profile by viewModel.coachingProfile.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.HOME -> profile.coachingName.ifBlank { "Coaching Attendance Manager" }
                            AppTab.STUDENTS -> "Students Management / छात्र"
                            AppTab.ATTENDANCE -> "Daily Attendance / उपस्थिति"
                            AppTab.FEES -> "Fee Management / फीस प्रबंधन"
                            AppTab.REPORTS -> "Monthly Reports / मासिक रिपोर्ट"
                            AppTab.SETTINGS -> "Coaching Settings / सेटिंग्स"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaultsElevation
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToStudents = { currentTab = AppTab.STUDENTS },
                    onNavigateToAttendance = { currentTab = AppTab.ATTENDANCE },
                    onNavigateToFees = { currentTab = AppTab.FEES },
                    onNavigateToSettings = { currentTab = AppTab.SETTINGS }
                )
                AppTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                AppTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                AppTab.FEES -> FeesScreen(viewModel = viewModel)
                AppTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

private val NavigationBarDefaultsElevation = 3.dp
