package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.graphics.Color
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
    STUDENTS("Student", "छात्र", Icons.Filled.School, Icons.Outlined.School),
    ATTENDANCE("Attendance", "हाजिरी", Icons.Filled.PersonAddAlt1, Icons.Outlined.PersonAddAlt1),
    FEES("Fee", "फीस", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    REPORTS("Reports", "रिपोर्ट", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachingApp(viewModel: CoachingViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showSettings by remember { mutableStateOf(false) }
    val profile by viewModel.coachingProfile.collectAsState()

    if (showSettings) {
        BackHandler { showSettings = false }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Settings & More / सेटिंग्स",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showSettings = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    } else {
        Scaffold(
            topBar = {
                if (currentTab != AppTab.HOME) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = when (currentTab) {
                                    AppTab.HOME -> profile.coachingName.ifBlank { "Coaching Attendance Manager" }
                                    AppTab.STUDENTS -> "Students Management / छात्र"
                                    AppTab.ATTENDANCE -> "Daily Attendance / उपस्थिति"
                                    AppTab.FEES -> "Fee Management / फीस प्रबंधन"
                                    AppTab.REPORTS -> "Student Reports / छात्र रिपोर्ट"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0F172A),
                    tonalElevation = 8.dp
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
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color(0xFF38BDF8),
                                indicatorColor = Color(0xFF1E293B),
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            )
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
                        onNavigateToReports = { currentTab = AppTab.REPORTS },
                        onNavigateToSettings = { showSettings = true }
                    )
                    AppTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                    AppTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                    AppTab.FEES -> FeesScreen(viewModel = viewModel)
                    AppTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
