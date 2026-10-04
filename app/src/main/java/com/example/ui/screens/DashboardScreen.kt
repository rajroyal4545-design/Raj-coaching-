package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.CoachingViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: CoachingViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val profile by viewModel.coachingProfile.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val currency = profile.currencySymbol.ifBlank { "₹" }
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Greeting, 3D Book & Graduation Cap PNG illustration, and Settings Gear
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello,",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${profile.coachingName.ifBlank { "Apex Coaching Classes" }} 🌱",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (profile.teacherName.isNotBlank() && profile.teacherName != "Director / Faculty") {
                            "${profile.teacherName} • Keep learning, keep growing ☀️"
                        } else {
                            "Keep learning, keep growing ☀️"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3D Books + Mortarboard + Sprout Vector Illustration
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hero_books),
                        contentDescription = "Books and Graduation Cap",
                        modifier = Modifier.size(76.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Today's Goal & Month Collection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D35)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Today's Goal
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🎯", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Today's Goal",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${metrics.todayPresent} / ${metrics.totalStudents} Present",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(38.dp)
                            .background(Color(0xFF334155))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Right Column: Month Collection
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Month Collection",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "$currency ${String.format(Locale.US, "%,.0f", metrics.currentMonthCollection)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 3. Two Large Action Pill Cards (Add Student & Today's Attendance)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Student Button Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clickable(onClick = onNavigateToStudents),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                                )
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Add\nStudent",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Today's Attendance Button Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clickable(onClick = onNavigateToAttendance),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                                )
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Today's\nAttendance",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. 4x2 Colorful Stats & Navigation Grid (8 Vibrant Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Total Students, Today's Absent, Today's Leave, Pending Fee
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 1: Total Students (Blue with red badge dot)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                        icon = {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                // Red indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                            }
                        },
                        value = "${metrics.totalStudents}",
                        label = "Total Students",
                        onClick = onNavigateToStudents
                    )

                    // Card 2: Today's Absent (Purple/Violet)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = "${metrics.todayAbsent}",
                        label = "Today's Absent",
                        onClick = onNavigateToAttendance
                    )

                    // Card 3: Today's Leave (Amber/Orange)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = "${metrics.todayLeave}",
                        label = "Today's Leave",
                        onClick = onNavigateToAttendance
                    )

                    // Card 4: Pending Fee (Emerald Green)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("₹", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        },
                        value = String.format(Locale.US, "%,.0f", metrics.pendingFeeAmount),
                        label = "Pending Fee",
                        onClick = onNavigateToFees
                    )
                }

                // Row 2: New Collection, Monthly Collection, Settings, About
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 5: New Collection (Coral Red)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
                        icon = {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "+",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                        },
                        value = String.format(Locale.US, "%,.0f", metrics.currentMonthCollection),
                        label = "New Collection",
                        onClick = onNavigateToFees
                    )

                    // Card 6: Monthly Collection (Vibrant Blue/Cyan)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = "$currency ${String.format(Locale.US, "%,.0f", metrics.currentMonthCollection)}",
                        label = "Monthly Collection",
                        onClick = onNavigateToFees
                    )

                    // Card 7: Student Reports (Cyan/Teal)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2))),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = "Present/Due",
                        label = "Reports >",
                        onClick = onNavigateToReports
                    )

                    // Card 8: Settings (Indigo/Slate)
                    DashboardGridCard(
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = "",
                        label = "Settings >",
                        onClick = onNavigateToSettings
                    )
                }
            }
        }

        // 5. Inspirational Quote Banner with Glowing Seedling Sprout PNG illustration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D35)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 22.sp)

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFF334155))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "\"Small steps every day\nlead to big results.\" 💛",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_seedling_sprout),
                        contentDescription = "Sprout",
                        modifier = Modifier.size(46.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false },
            onOpenInstagram = {
                openWebLink(context, "https://www.instagram.com/royal_ai_hub4u?stkn=MXBuMDhpMmVibjk2bw==")
            },
            onOpenBlog = {
                openWebLink(context, "https://royalaihub.blogspot.com/?m=1")
            }
        )
    }
}

@Composable
fun DashboardGridCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    icon: @Composable () -> Unit,
    value: String = "",
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(102.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                icon()
                Spacer(modifier = Modifier.height(6.dp))
                if (value.isNotBlank()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        fontSize = if (value.length > 5) 12.sp else 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

private fun openWebLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Link cannot be opened: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
