package com.example.myapplication.features.tutorials

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.ui.theme.CityFlowBKKTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class TutorialContentSection {
    Usage,
    Fare,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(
    section: TutorialContentSection,
    onBackClick: () -> Unit,
    viewModel: TutorialsViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("คู่มือการใช้งาน", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "เริ่มต้นเดินทางกับ CityFlowBKK",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            when (section) {
                TutorialContentSection.Usage -> {
                    TutorialSectionBlock(
                        title = "วิธีการใช้งาน",
                        description = "เลือกดูคู่มือแยกตามระบบรถไฟฟ้า โดยตอนนี้ข้อมูลเดิมถูกจัดไว้ในหมวด BTS และเตรียมหมวด MRT สำหรับเพิ่มข้อมูลใหม่",
                    ) {
                        TransitSystemBlock(
                            title = "BTS",
                            description = "คู่มือการใช้งาน BTS SkyTrain พร้อมขั้นตอนซื้อตั๋ว แผนที่ กฎระเบียบ บัตรโดยสาร เวลาให้บริการ และข้อมูลช่วยเหลือ",
                        ) {
                            BtsOverviewCard()
                            TicketingGuideCard()
                            OfficialLinksCard()
                            RouteMapCard()
                            BtsRulesCard()
                            TravelCardsCard()
                            OperatingHoursCard()
                            FacilitiesCard()
                            ContactSupportCard()
                            SafetyTipsCard()
                        }
                        TransitSystemBlock(
                            title = "MRT",
                            description = "คู่มือ MRT จาก CityFlowBKK MRT Manual พร้อมข้อมูลเส้นทาง ค่าโดยสาร ตั๋ว วิธีใช้งาน กฎระเบียบ และลิงก์อ้างอิง",
                        ) {
                            MrtOverviewCard()
                            MrtStepByStepCard()
                            MrtLearningChannelsCard()
                            MrtRouteMapCard()
                            MrtRulesCard()
                            MrtTicketsCard()
                            MrtOperatingHoursCard()
                            MrtFacilitiesCard()
                            MrtContactSupportCard()
                            MrtSafetyTipsCard()
                        }
                    }
                }

                TutorialContentSection.Fare -> {
                    TutorialSectionBlock(
                        title = "ราคาการซื้อตั๋ว",
                        description = "ตรวจสอบข้อมูลค่าโดยสารแยกตามระบบรถไฟฟ้า",
                    ) {
                        TransitSystemBlock(
                            title = "BTS",
                            description = "คำนวณค่าโดยสาร BTS และดูตารางค่าโดยสารจากข้อมูลเดิม",
                        ) {
                            FareCalculatorCard(viewModel)
                            FareInformationCard(
                                expanded = viewModel.isFareInformationExpanded,
                                onClick = viewModel::onFareInformationClick,
                            )
                        }
                        TransitSystemBlock(
                            title = "MRT",
                            description = "ข้อมูลค่าโดยสาร MRT แยกจากกล่อง BTS",
                        ) {
                            MrtFareCalculatorCard(viewModel)
                            MrtFareSystemCard()
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TutorialSectionBlock(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun TransitSystemBlock(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun EmptyTransitSystemCard(
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExpandableTutorialCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { expanded = expanded.not() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = if (expanded) "⌃" else "⌄",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content,
                )
            }
        }
    }
}
@Composable
private fun FareCalculatorCard(viewModel: TutorialsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ค้นหาค่าโดยสาร (BTS)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (viewModel.fromStation != null || viewModel.toStation != null) {
                    TextButton(onClick = {
                        viewModel.fromStation = null
                        viewModel.toStation = null
                    }) {
                        Text("ล้างข้อมูล", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    StationDropdown(
                        label = "จากสถานี",
                        selectedStation = viewModel.fromStation,
                        onStationSelected = { viewModel.fromStation = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    StationDropdown(
                        label = "ไปสถานี",
                        selectedStation = viewModel.toStation,
                        onStationSelected = { viewModel.toStation = it }
                    )
                }
                
                IconButton(
                    onClick = {
                        val temp = viewModel.fromStation
                        viewModel.fromStation = viewModel.toStation
                        viewModel.toStation = temp
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("⇅", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            val fare = viewModel.getCalculatedFare()
            AnimatedVisibility(
                visible = fare != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (fare != null) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(text = "ค่าโดยสาร: ", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "$fare",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " บาท",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MrtFareCalculatorCard(viewModel: TutorialsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ค้นหาค่าโดยสาร (MRT)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (viewModel.mrtFromStation != null || viewModel.mrtToStation != null) {
                    TextButton(onClick = {
                        viewModel.mrtFromStation = null
                        viewModel.mrtToStation = null
                    }) {
                        Text("ล้างข้อมูล", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    StationDropdown(
                        label = "จากสถานี",
                        selectedStation = viewModel.mrtFromStation,
                        onStationSelected = { viewModel.mrtFromStation = it },
                        stationCodes = MrtFareData.stationCodes,
                        stationNames = MrtFareData.stationNames,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    StationDropdown(
                        label = "ไปสถานี",
                        selectedStation = viewModel.mrtToStation,
                        onStationSelected = { viewModel.mrtToStation = it },
                        stationCodes = MrtFareData.stationCodes,
                        stationNames = MrtFareData.stationNames,
                    )
                }

                IconButton(
                    onClick = {
                        val temp = viewModel.mrtFromStation
                        viewModel.mrtFromStation = viewModel.mrtToStation
                        viewModel.mrtToStation = temp
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("⇅", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            val fare = viewModel.getCalculatedMrtFare()
            AnimatedVisibility(
                visible = fare != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (fare != null) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(text = "ค่าโดยสาร: ", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "$fare",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " บาท",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }
                        Text(
                            text = "รองรับการคำนวณเบื้องต้นสำหรับ MRT สายสีน้ำเงินและสายสีม่วง",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDropdown(
    label: String,
    selectedStation: String?,
    onStationSelected: (String) -> Unit,
    stationCodes: List<String> = FareMatrixData.stationCodes,
    stationNames: Map<String, String> = FareMatrixData.stationNames,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val displayText = if (selectedStation != null) {
        val name = stationNames[selectedStation] ?: ""
        "$selectedStation - $name"
    } else {
        "กรุณาเลือกสถานี"
    }

    val filteredStations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            stationCodes
        } else {
            stationCodes.filter { code ->
                code.contains(searchQuery, ignoreCase = true) || 
                (stationNames[code]?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayText, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedStation == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(text = "▼", style = MaterialTheme.typography.bodySmall)
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { 
                    expanded = false
                    searchQuery = ""
                },
                modifier = Modifier.fillMaxWidth(0.85f).heightIn(max = 450.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("ค้นหารหัสหรือชื่อสถานี", fontSize = 14.sp) },
                    leadingIcon = { Text("🔍", fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Text("✕", fontSize = 18.sp)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                if (filteredStations.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("ไม่พบสถานีที่ค้นหา", style = MaterialTheme.typography.bodySmall) },
                        onClick = { },
                        enabled = false
                    )
                }

                filteredStations.forEach { code ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = stationNames[code] ?: "", style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        onClick = {
                            onStationSelected(code)
                            expanded = false
                            searchQuery = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FareInformationCard(
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("฿", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ข้อมูลค่าโดยสารรวม",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "ตรวจสอบราคาเริ่มต้นของแต่ละสาย",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                
                Text(
                    text = "BTS SkyTrain (Effective 1 Nov 2025)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                FareRow(label = "ช่วงค่าโดยสาร:", value = "${FareMatrixData.minFare} - ${FareMatrixData.maxFare} บาท")
                FareRow(label = "จำนวนสถานี:", value = "${FareMatrixData.stationCodes.size} สถานี")

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ตารางข้อมูลค่าโดยสาร",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                FareMatrixTable()

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ภาพตารางค่าโดยสารจาก PDF",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                FareMatrixPdfImage()
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                
                Text(
                    text = "สายอื่นๆ",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                FareRow(label = "MRT (สายสีน้ำเงิน/ม่วง):", value = "17 - 45 บาท", color = Color(0xFF2196F3))
                FareRow(label = "Airport Rail Link:", value = "15 - 45 บาท", color = Color(0xFF9C27B0))
                FareRow(label = "สายสีแดง (SRT):", value = "12 - 42 บาท", color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun FareMatrixPdfImage() {
    val context = LocalContext.current
    var showZoomDialog by remember { mutableStateOf(false) }
    // Move PDF rendering to a background thread to prevent ANR (App Not Responding)
    val bitmapState = produceState<Bitmap?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            renderPdfPageBitmap(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 300.dp)
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(enabled = bitmapState.value != null, role = Role.Button) { showZoomDialog = true },
        contentAlignment = Alignment.Center
    ) {
        bitmapState.value?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "BTS fare matrix",
                modifier = Modifier.width(800.dp),
            )
        } ?: CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
    Text(
        text = "แตะรูปเพื่อซูม",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    bitmapState.value?.let { bitmap ->
        if (showZoomDialog) {
            ZoomableBitmapImageDialog(
                imageBitmap = bitmap.asImageBitmap(),
                title = "ภาพตารางค่าโดยสารจาก PDF",
                onDismiss = { showZoomDialog = false },
            )
        }
    }
}

@Composable
private fun FareMatrixTable() {
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .horizontalScroll(horizontalScrollState),
        ) {
            FareMatrixTableRow(
                rowLabel = "From/To",
                fares = FareMatrixData.stationCodes,
                isHeader = true,
            )
            FareMatrixData.rows.take(10).forEach { row ->
                val stationName = FareMatrixData.stationNames[row.stationCode] ?: ""
                val displayLabel = "${row.stationCode}\n$stationName"
                FareMatrixTableRow(
                    rowLabel = displayLabel,
                    fares = row.fares.map { it.toString() },
                    isHeader = false,
                )
            }
            if (FareMatrixData.rows.size > 10) {
                Text(
                    text = "... และเส้นทางอื่นๆ ในระบบ ...",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun FareMatrixTableRow(
    rowLabel: String,
    fares: List<String>,
    isHeader: Boolean,
) {
    val backgroundColor = if (isHeader) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val textColor = if (isHeader) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.background(backgroundColor),
    ) {
        FareMatrixCell(
            text = rowLabel,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            width = 70.dp,
        )
        fares.take(15).forEach { fare ->
            FareMatrixCell(
                text = fare,
                color = textColor,
                fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
                width = 40.dp,
            )
        }
    }
}

@Composable
private fun FareMatrixCell(
    text: String,
    color: Color,
    fontWeight: FontWeight,
    width: Dp,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(32.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = fontWeight,
            maxLines = 1
        )
    }
}

private fun renderPdfPageBitmap(context: android.content.Context): Bitmap {
    val pdfFile = File(context.cacheDir, "fare_matrix_eff_1nov25_sjc.pdf")
    if (!pdfFile.exists()) {
        try {
            context.resources.openRawResource(R.raw.fare_matrix_eff_1nov25_sjc).use { input ->
                pdfFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("TutorialsScreen", "Error copying PDF: ${e.message}")
            return createEmptyBitmap()
        }
    }

    return try {
        ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                if (renderer.pageCount > 0) {
                    renderer.openPage(0).use { page ->
                        val scale = 2
                        val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                } else {
                    createEmptyBitmap()
                }
            }
        }
    } catch (e: Exception) {
        Log.e("TutorialsScreen", "Error rendering PDF: ${e.message}")
        createEmptyBitmap()
    }
}

private fun createEmptyBitmap(): Bitmap {
    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}

@Composable
private fun TicketingGuideCard() {
    ExpandableTutorialCard(
        title = "วิธีการซื้อตั๋ว",
        description = "ทำตามขั้นตอนบนหน้าจอ เลือกสถานีปลายทาง ชำระเงิน แล้วรับบัตรโดยสารก่อนเข้าประตูอัตโนมัติ",
    ) {
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_language,
                title = "1. เลือกภาษาและเริ่มทำรายการ",
                description = "แตะปุ่มภาษาได้หากต้องการเปลี่ยนภาษา จากนั้นเริ่มเลือกปลายทางบนหน้าจอของตู้จำหน่ายบัตร",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_choose_station,
                title = "2. เลือกสถานีปลายทาง",
                description = "แตะสถานีปลายทางบนแผนที่สายรถไฟฟ้า ระบบจะคำนวณค่าโดยสารให้อัตโนมัติ",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_select_destination,
                title = "3. ตรวจสอบสถานีที่เลือก",
                description = "ดูชื่อสถานีที่หน้าจอให้ถูกต้องก่อนทำขั้นตอนต่อไป หากเลือกผิดให้ย้อนกลับหรือเลือกใหม่",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_card_count,
                title = "4. เลือกจำนวนบัตร",
                description = "เลือกจำนวนบัตรโดยสารที่ต้องการซื้อ ระบบจะแสดงยอดเงินรวมที่ต้องชำระ",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_insert_coins,
                title = "5. ชำระเงินด้วยเหรียญหรือเงินสด",
                description = "ใส่เหรียญตามจำนวนที่แสดงบนหน้าจอ แล้วรอให้เครื่องประมวลผลการชำระเงิน",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_qr_payment,
                title = "6. หรือชำระด้วย QR Payment",
                description = "หากตู้รองรับ QR ให้สแกน QR Code ด้วยแอปธนาคารหรือช่องทางชำระเงินที่รองรับภายในเวลาที่กำหนด",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_step_take_card,
                title = "7. รับบัตรโดยสารและเงินทอน",
                description = "รับบัตรโดยสารจากช่องรับบัตร และอย่าลืมรับเงินทอนหากมี",
            )
            TutorialImageStep(
                imageRes = R.drawable.ticket_gate_use,
                title = "8. ใช้บัตรแตะเข้าและสอดออก",
                description = "ตอนเข้าให้แตะบัตรที่ประตูอัตโนมัติ ตอนออกให้สอดบัตรคืนที่ช่องรับบัตรของประตู",
            )
    }
}

@Composable
private fun OfficialLinksCard() {
    val context = LocalContext.current

    ExpandableTutorialCard(
        title = "ช่องทางเรียนรู้และอัปเดตข่าว",
        description = "ใช้สำหรับติดตามประกาศรายวันและดูวิดีโอสอนการใช้งาน BTS เพิ่มเติม",
    ) {
        LinkButton(
            label = "Facebook BTS SkyTrain (อัปเดตข่าวรายวัน)",
            url = "https://www.facebook.com/BTSSkyTrain/",
            context = context,
        )
        YouTubeVideoPlayer(videoId = "8H0mP1eV6ok")
    }
}

@Composable
private fun LinkButton(
    label: String,
    url: String,
    context: android.content.Context,
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
    ) {
        Text(text = label)
    }
}

@Composable
private fun BtsOverviewCard() {
    ExpandableTutorialCard(
        title = "ข้อมูลพื้นฐาน",
        description = "ภาพรวมระบบ BTS SkyTrain สายหลัก จุดเชื่อมต่อ และบทบาทในการเดินทางในกรุงเทพฯ",
    ) {
        BulletItem(text = "BTS SkyTrain เป็นระบบรถไฟฟ้ายกระดับหลักในกรุงเทพฯ เปิดให้บริการครั้งแรกเมื่อวันที่ 5 ธันวาคม 2542")
        BulletItem(text = "เส้นทางหลักประกอบด้วยสายสุขุมวิท สายสีลม และสายสีทอง")
        BulletItem(text = "จุดเชื่อมต่อสำคัญคือสถานีสยาม สำหรับเปลี่ยนระหว่างสายสุขุมวิทและสายสีลม")
        BulletItem(text = "เชื่อมต่อระบบอื่นได้หลายจุด เช่น อโศก-สุขุมวิท, ศาลาแดง-สีลม, หมอชิต-สวนจตุจักร และพญาไท-Airport Rail Link")
        BulletItem(text = "เหมาะสำหรับเดินทางในย่านธุรกิจ แหล่งท่องเที่ยว ศูนย์การค้า และย่านที่พักอาศัยตามแนวรถไฟฟ้า")
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeVideoPlayer(
    videoId: String,
    title: String = "วิดีโอสอนใช้งาน BTS",
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(12.dp)),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webChromeClient = WebChromeClient()
                    loadDataWithBaseURL(
                        "https://www.youtube.com",
                        """
                            <html>
                              <body style="margin:0;padding:0;background:#000;">
                                <iframe
                                  width="100%"
                                  height="100%"
                                  src="https://www.youtube.com/embed/$videoId"
                                  title="$title"
                                  frameborder="0"
                                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                  allowfullscreen>
                                </iframe>
                              </body>
                            </html>
                        """.trimIndent(),
                        "text/html",
                        "utf-8",
                        null,
                    )
                }
            },
            onRelease = { webView ->
                webView.destroy()
            }
        )
        Text(
            text = "เล่นวิดีโอได้ในแอป โดยไม่ต้องออกไปเปิด YouTube",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MrtOverviewCard() {
    ExpandableTutorialCard(
        title = "ข้อมูลพื้นฐาน",
        description = "ภาพรวมระบบรถไฟฟ้ามหานคร ผู้ให้บริการ เวลาให้บริการ และสถานีเชื่อมต่อหลัก",
    ) {
        BulletItem(text = "MRT เป็นระบบขนส่งมวลชนความเร็วสูงในกรุงเทพฯ และปริมณฑล เริ่มให้บริการสายสีน้ำเงินเมื่อวันที่ 3 กรกฎาคม 2547")
        BulletItem(text = "สายสีน้ำเงินและสายสีม่วงให้บริการโดย BEM")
        BulletItem(text = "สายสีเหลืองและสายสีชมพูเป็นระบบ Monorail ให้บริการโดยกลุ่มผู้ให้บริการแยกต่างหาก")
        BulletItem(text = "เวลาให้บริการโดยทั่วไป 06:00 - 24:00 น. ทุกวัน")
        BulletItem(text = "ช่วงเร่งด่วนสายสีน้ำเงินมีความถี่ประมาณ 3.5 - 4 นาที ส่วนสายอื่นประมาณ 5 - 6 นาที")
        Text(
            text = "สถานีเชื่อมต่อหลัก",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        BulletItem(text = "สุขุมวิท (BL22) เชื่อม BTS อโศก (E4)")
        BulletItem(text = "สวนจตุจักร (BL13) เชื่อม BTS หมอชิต (N8)")
        BulletItem(text = "เตาปูน (BL10 / PP16) เชื่อมสายสีน้ำเงินและสายสีม่วง")
        BulletItem(text = "สีลม (BL26) เชื่อม BTS ศาลาแดง (S2)")
        BulletItem(text = "บางซื่อ (BL11) เชื่อมรถไฟชานเมืองสายสีแดงและรถไฟทางไกล")
        BulletItem(text = "ลาดพร้าว (BL15 / YL01) เชื่อมสายสีน้ำเงินและสายสีเหลือง")
    }
}

@Composable
private fun MrtRouteMapCard() {
    ExpandableTutorialCard(
        title = "แผนที่เส้นทาง",
        description = "ข้อมูลสายสีน้ำเงิน สีม่วง สีเหลือง และสีชมพู พร้อมรูปแผนที่ประกอบ",
    ) {
        TutorialImageStep(
            imageRes = R.drawable.mrt_pink_yellow_map,
            title = "แผนที่เส้นทาง MRT / Monorail",
            description = "รูปประกอบเส้นทางสายสีชมพูและสายสีเหลือง พร้อมจุดเชื่อมต่อสำคัญ เช่น บางซื่อ ลาดพร้าว และศรีนครินทร์",
            contentScale = ContentScale.Fit,
        )
        MrtLineGroup(
            title = "สายสีน้ำเงิน (Blue Line)",
            color = Color(0xFF1565C0),
            route = "ท่าพระ (BL01) ↔ หลักสอง (BL38)",
            places = "บางซื่อ, สวนจตุจักร, ลาดพร้าว, สุขุมวิท, เพชรบุรี, สีลม, หัวลำโพง, วัดมังกร, สนามไชย",
        )
        MrtLineGroup(
            title = "สายสีม่วง (Purple Line)",
            color = Color(0xFF7B1FA2),
            route = "คลองบางไผ่ (PP01) ↔ เตาปูน (PP16)",
            places = "Central Westgate, IKEA บางใหญ่, ศูนย์ราชการจังหวัดนนทบุรี",
        )
        MrtLineGroup(
            title = "สายสีเหลือง (Yellow Line)",
            color = Color(0xFFF9A825),
            route = "ลาดพร้าว (YL01) ↔ สำโรง (YL23)",
            places = "ซีคอนสแควร์, ตลาดนัดรถไฟศรีนครินทร์, สวนหลวง ร.9",
        )
        MrtLineGroup(
            title = "สายสีชมพู (Pink Line)",
            color = Color(0xFFD81B60),
            route = "ศูนย์ราชการนนทบุรี (PK01) ↔ มีนบุรี (PK30)",
            places = "ศูนย์ราชการแจ้งวัฒนะ, อิมแพ็ค เมืองทองธานี, มีนบุรี",
        )
    }
}

@Composable
private fun MrtLineGroup(
    title: String,
    color: Color,
    route: String,
    places: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        BulletItem(text = "เส้นทาง: $route")
        BulletItem(text = "จุดสำคัญ: $places")
    }
}

@Composable
private fun MrtFareSystemCard() {
    ExpandableTutorialCard(
        title = "ค่าโดยสาร MRT",
        description = "สรุปค่าโดยสารตามระยะทาง การเปลี่ยนสาย และสิทธิ์เด็กตาม MRT Manual",
    ) {
        FareRow(label = "สายสีน้ำเงิน:", value = "17 - 44 บาท", color = Color(0xFF1565C0))
        FareRow(label = "เชื่อม Blue ↔ Purple:", value = "สูงสุดไม่เกิน 71 บาท", color = Color(0xFF7B1FA2))
        BulletItem(text = "ระบบคิดค่าโดยสารตามระยะทางและจำนวนสถานีที่เดินทางจริง")
        BulletItem(text = "เปลี่ยนสายสีน้ำเงิน ↔ สายสีม่วงที่สถานีเตาปูน ได้รับการยกเว้นค่าแรกเข้า")
        BulletItem(text = "เด็กอายุไม่เกิน 14 ปี และสูงไม่เกิน 120 เซนติเมตร เดินทางฟรี")
    }
}

@Composable
private fun MrtTicketsCard() {
    ExpandableTutorialCard(
        title = "บัตรโดยสาร",
        description = "ประเภทตั๋ว บัตรเติมเงิน บัตรส่วนลด และ EMV Contactless",
    ) {
        BulletItem(text = "เหรียญโดยสารเที่ยวเดียว (Single Journey Token): ซื้อได้ที่ตู้หรือเคาน์เตอร์ ใช้แตะเข้าและหยอดคืนตอนออก")
        BulletItem(text = "บัตรโดยสาร MRT แบบเติมเงิน: ค่าแรกเริ่มออกบัตร 180 บาท แบ่งเป็นมูลค่าเดินทาง 100 บาท มัดจำ 50 บาท และค่าธรรมเนียม 30 บาท")
        BulletItem(text = "บัตรนักเรียน/นักศึกษาอายุไม่เกิน 23 ปี ได้ส่วนลดค่าโดยสาร 10%")
        BulletItem(text = "บัตรผู้สูงอายุชาวไทยอายุ 60 ปีขึ้นไป ได้ส่วนลดสูงสุด 50%")
        BulletItem(text = "บัตรเครดิต/เดบิต Contactless สามารถแตะเข้า-ออกได้โดยไม่ต้องซื้อตั๋วใหม่")
        BulletItem(text = "ข้อควรระวัง: บัตรเติมเงิน MRT ใช้ข้ามไปสายสีเหลือง/ชมพูไม่ได้ แต่ EMV หรือ Rabbit Card ใช้กับระบบ Monorail ได้ตามเงื่อนไข")
    }
}

@Composable
private fun MrtStepByStepCard() {
    ExpandableTutorialCard(
        title = "วิธีการซื้อตั๋ว",
        description = "ขั้นตอนตั้งแต่เข้าสถานี ซื้อตั๋ว ผ่านประตู รอรถไฟ และออกจากสถานี",
    ) {
        BulletItem(text = "เข้าสู่สถานี: สังเกตป้ายทางเข้าและหมายเลขทางออก เดินลงสู่ชั้นจำหน่ายตั๋ว และผ่านจุดตรวจความปลอดภัย")
        BulletItem(text = "ซื้อเหรียญโดยสาร: เลือกภาษา เลือกสถานีปลายทางบนหน้าจอ TVM ชำระเงินสดหรือ QR PromptPay แล้วรับเหรียญและเงินทอน")
        BulletItem(text = "ผ่านประตูกลขาเข้า: แตะเหรียญ บัตร MRT หรือบัตร EMV ที่จุดเซนเซอร์ด้านบน ห้ามหยอดเหรียญในขาเข้า")
        BulletItem(text = "รอรถไฟ: ตรวจป้ายปลายทาง ยืนหลังเส้นสีเหลือง และเปิดทางให้ผู้โดยสารออกก่อน")
        BulletItem(text = "ออกจากสถานี: หากใช้เหรียญให้หยอดคืนในช่องรับด้านหน้าเครื่อง หากใช้บัตรให้แตะด้านบนเหมือนขาเข้า")
    }
}

@Composable
private fun MrtLearningChannelsCard() {
    val context = LocalContext.current

    ExpandableTutorialCard(
        title = "ช่องทางเรียนรู้และอัปเดตข่าว",
        description = "ช่องทางติดตามข้อมูลทางการของ MRTA/BEM และวิดีโอแนะนำการเดินทาง MRT",
    ) {
        LinkButton(
            label = "MRTA",
            url = "https://www.mrta.co.th/",
            context = context,
        )
        LinkButton(
            label = "BEM",
            url = "http://bemplc.co.th/",
            context = context,
        )
        YouTubeVideoPlayer(
            videoId = "EIcj-kJnQrU",
            title = "วิดีโอแนะนำการใช้งาน MRT",
        )
    }
}

@Composable
private fun MrtRulesCard() {
    ExpandableTutorialCard(
        title = "กฎระเบียบการใช้งาน",
        description = "ข้อปฏิบัติและข้อห้ามที่ควรรู้เมื่อใช้บริการ MRT",
    ) {
        BulletItem(text = "ห้ามรับประทานอาหาร ขนม หรือดื่มเครื่องดื่มทุกชนิดในเขตชำระเงินและบนรถไฟ")
        BulletItem(text = "ใช้ตั๋ว เหรียญโดยสาร หรือบัตรที่ถูกต้องในการแตะเข้า-ออกระบบ")
        BulletItem(text = "ไม่ยืนกีดขวางบริเวณประตูรถไฟ และควรให้ผู้โดยสารออกก่อนขึ้น")
        BulletItem(text = "ไม่เข้าเขตหวงห้ามหรือพื้นที่รางโดยเด็ดขาด")
        BulletItem(text = "ปฏิบัติตามประกาศ ป้ายเตือน และคำแนะนำของพนักงานสถานี")
    }
}

@Composable
private fun MrtOperatingHoursCard() {
    ExpandableTutorialCard(
        title = "เวลาให้บริการ",
        description = "เวลาให้บริการและความถี่โดยประมาณของระบบ MRT",
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
    ) {
        BulletItem(text = "เวลาให้บริการโดยทั่วไป 06:00 - 24:00 น. ทุกวัน")
        BulletItem(text = "ช่วงเวลาเร่งด่วน สายสีน้ำเงินมีความถี่ประมาณ 3.5 - 4 นาที")
        BulletItem(text = "สายอื่น ๆ โดยทั่วไปมีความถี่ประมาณ 5 - 6 นาทีในช่วงเร่งด่วน")
        BulletItem(text = "ควรตรวจสอบเวลารถไฟขบวนแรก/ขบวนสุดท้ายจากช่องทางทางการก่อนเดินทางในช่วงเช้าตรู่หรือดึก")
    }
}

@Composable
private fun MrtFacilitiesCard() {
    ExpandableTutorialCard(
        title = "สิ่งอำนวยความสะดวก",
        description = "สิ่งอำนวยความสะดวกในสถานี MRT สำหรับผู้โดยสารทั่วไปและผู้ที่ต้องการความช่วยเหลือ",
    ) {
        BulletItem(text = "MRT มีห้องน้ำสำหรับผู้โดยสารครบทุกสถานี หากหาไม่พบสามารถติดต่อพนักงานประจำเคาน์เตอร์")
        BulletItem(text = "สถานีมีลิฟต์ บันไดเลื่อน และทางลาดในหลายจุดเพื่อรองรับผู้สูงอายุ ผู้พิการ และผู้มีกระเป๋าเดินทาง")
        BulletItem(text = "มีแผนผังสถานี ป้ายทางออก และป้ายเชื่อมต่อระบบขนส่งอื่นเพื่อช่วยวางแผนเส้นทาง")
        BulletItem(text = "บางสถานีมีอาคารจอดแล้วจร เช่น ลาดพร้าว และศูนย์วัฒนธรรมแห่งประเทศไทย")
        BulletItem(text = "สามารถติดต่อพนักงานสถานีเพื่อสอบถามทางออก สิ่งอำนวยความสะดวก หรือขอความช่วยเหลือ")
    }
}

@Composable
private fun MrtContactSupportCard() {
    val context = LocalContext.current

    ExpandableTutorialCard(
        title = "ศูนย์บริการและช่วยเหลือ",
        description = "ช่องทางติดต่อข้อมูล MRT และหน่วยงานที่เกี่ยวข้อง",
    ) {
        ContactRow(label = "MRT Information:", value = "02-624-5200") {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:026245200"))
            context.startActivity(intent)
        }
        LinkButton(
            label = "MRTA",
            url = "https://www.mrta.co.th/",
            context = context,
        )
        LinkButton(
            label = "BEM",
            url = "http://bemplc.co.th/",
            context = context,
        )
    }
}

@Composable
private fun MrtSafetyTipsCard() {
    ExpandableTutorialCard(
        title = "ข้อควรระวังและความปลอดภัย",
        description = "ข้อควรระวังบนชานชาลา ในขบวนรถ และการใช้อุปกรณ์ฉุกเฉิน",
    ) {
        BulletItem(text = "ชานชาลามีปุ่ม Emergency Train Stop สำหรับกดหยุดรถไฟเมื่อมีคนหรือสิ่งของตกลงราง")
        BulletItem(text = "ภายในขบวนมีระบบอินเตอร์คอมใกล้ประตู เพื่อแจ้งเหตุฉุกเฉินกับคนขับ")
        BulletItem(text = "ควรยืนหลังเส้นเหลือง ไม่ขวางทางเข้าออก และระวังช่องว่างระหว่างรถไฟกับชานชาลา")
        BulletItem(text = "จับราวหรือห่วงจับขณะรถเคลื่อนที่ และดูแลเด็กเล็กอย่างใกล้ชิด")
        BulletItem(text = "หากพบวัตถุต้องสงสัยหรือเหตุผิดปกติ ให้แจ้งพนักงานสถานีทันที")
    }
}

@Composable
private fun MrtReferenceLinksCard() {
    val context = LocalContext.current

    ExpandableTutorialCard(
        title = "ลิงก์อ้างอิง MRT",
        description = "ช่องทางข้อมูลทางการสำหรับตรวจสอบเส้นทาง ค่าโดยสาร EMV และข่าว MRT เพิ่มเติม",
    ) {
        LinkButton(
            label = "BEM",
            url = "http://bemplc.co.th/",
            context = context,
        )
        LinkButton(
            label = "MRTA",
            url = "https://www.mrta.co.th/",
            context = context,
        )
        YouTubeVideoPlayer(
            videoId = "EIcj-kJnQrU",
            title = "วิดีโอแนะนำการใช้งาน MRT",
        )
    }
}

@Composable
private fun RouteMapCard() {
    ExpandableTutorialCard(
        title = "แผนที่เส้นทาง",
        description = "ดูภาพรวมเส้นทาง BTS, MRT, Airport Rail Link และสายเชื่อมต่อ เพื่อช่วยวางแผนก่อนเดินทาง",
    ) {
        TutorialImageStep(
            imageRes = R.drawable.bts_yellow_map,
            title = "แผนที่เส้นทางระบบรถไฟฟ้า",
            description = "ใช้ดูสถานีต้นทาง ปลายทาง จุดเปลี่ยนสาย และเส้นทางที่เชื่อมต่อกันในกรุงเทพฯ",
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun TutorialImageStep(
    imageRes: Int,
    title: String,
    description: String,
    contentScale: ContentScale = ContentScale.Crop
) {
    var showZoomDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 190.dp, max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button) { showZoomDialog = true },
        )
        Text(
            text = "แตะรูปเพื่อซูม",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (showZoomDialog) {
        ZoomableResourceImageDialog(
            imageRes = imageRes,
            title = title,
            contentScale = contentScale,
            onDismiss = { showZoomDialog = false },
        )
    }
}

@Composable
private fun ZoomableResourceImageDialog(
    imageRes: Int,
    title: String,
    contentScale: ContentScale = ContentScale.Fit,
    onDismiss: () -> Unit,
) {
    ZoomableImageDialogFrame(title = title, onDismiss = onDismiss) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ZoomableBitmapImageDialog(
    imageBitmap: ImageBitmap,
    title: String,
    onDismiss: () -> Unit,
) {
    ZoomableImageDialogFrame(title = title, onDismiss = onDismiss) {
        Image(
            bitmap = imageBitmap,
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ZoomableImageDialogFrame(
    title: String,
    onDismiss: () -> Unit,
    imageContent: @Composable () -> Unit,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 6f)
        offset = if (scale == 1f) Offset.Zero else offset + panChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    )
                    .transformable(transformableState),
                contentAlignment = Alignment.Center,
            ) {
                imageContent()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) {
                    Text(text = "ปิด", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BtsRulesCard() {
    ExpandableTutorialCard(
        title = "กฎระเบียบการใช้งาน",
        description = "ข้อปฏิบัติและข้อห้ามที่ควรรู้เมื่อใช้บริการรถไฟฟ้า BTS ทั้งบนสถานี บริเวณชานชาลา และภายในขบวนรถ",
    ) {
            AnnouncementRegulationsSection()


            RulesGroup(
                title = "เมื่ออยู่บนสถานี",
                dos = listOf(
                    "อ่านหรือฟังประกาศ และปฏิบัติตามอย่างเคร่งครัด",
                    "รักษาความสะอาด และทิ้งขยะลงในถังที่จัดเตรียมไว้",
                    "หากรู้สึกไม่สบายหรือต้องการความช่วยเหลือ ให้ติดต่อพนักงาน",
                    "หากทรัพย์สินสูญหาย พบของ หรือเก็บของได้ ให้แจ้งพนักงาน",
                    "หากพบพฤติกรรมน่าสงสัยหรือวัตถุต้องสงสัย ให้แจ้งพนักงานทันที",
                ),
                donts = listOf(
                    "ห้ามสูบบุหรี่ หรือนำวัตถุไวไฟและวัตถุอันตรายเข้าระบบ",
                    "ห้ามนำสัตว์ทุกประเภทเข้ามาในระบบรถไฟฟ้า",
                    "ห้ามรับประทานอาหารและเครื่องดื่มในระบบรถไฟฟ้า",
                    "ห้ามทิ้งสิ่งของออกนอกสถานี",
                    "ห้ามนำสัมภาระขนาดใหญ่หรือหนักเกินไปเข้าระบบ",
                    "ห้ามขีดเขียนหรือทำความเสียหายแก่อุปกรณ์",
                    "ห้ามสวมรองเท้าสเก็ต เล่นสเก็ตบอร์ด หรือขี่จักรยานในระบบ",
                    "ห้ามส่งเสียงดังหรือก่อความรำคาญแก่ผู้โดยสารอื่น",
                ),
            )

            RulesGroup(
                title = "เมื่ออยู่บนชานชาลา",
                dos = listOf(
                    "ยืนเข้าแถวรอขบวนรถ และวางสัมภาระหลังเส้นเหลือง",
                    "ดูแลเด็กเล็กขณะรอและขณะเข้าออกขบวนรถ",
                    "หลีกทางให้ผู้โดยสารในขบวนรถออกก่อน",
                    "ระวังช่องว่างระหว่างพื้นชานชาลากับขบวนรถ",
                    "เมื่อได้ยินเสียงสัญญาณปิดประตู ให้รอขบวนถัดไป",
                    "หากสิ่งของตกลงราง ให้แจ้งพนักงานทันที",
                ),
                donts = listOf(
                    "ห้ามวิ่ง เล่น ผลัก หรือหยอกล้อกันบริเวณชานชาลา",
                    "ห้ามลงรางโดยเด็ดขาด เพราะอันตรายจากขบวนรถและไฟฟ้าแรงสูง",
                    "ห้ามเข้าไปในเขตหวงห้ามบริเวณปลายชานชาลา",
                ),
            )

            RulesGroup(
                title = "ขณะโดยสารรถไฟฟ้า",
                dos = listOf(
                    "จับห่วง เสา หรือราวขณะเดินทาง",
                    "เอื้อเฟื้อที่นั่งแก่เด็ก สตรีมีครรภ์ ผู้สูงอายุ และผู้พิการ",
                    "คืนที่นั่งสำรองแด่ภิกษุและสามเณร",
                    "ดูแลสัมภาระและสิ่งของมีค่าขณะเดินทาง",
                    "เมื่อมีเหตุฉุกเฉิน ให้แจ้งพนักงานควบคุมรถไฟฟ้าทันที",
                    "ใช้อุปกรณ์ฉุกเฉินเมื่อมีเหตุจำเป็นเท่านั้น",
                ),
                donts = listOf(
                    "ห้ามยืนพิงประตู หรือยืนกีดขวางบริเวณประตูรถไฟฟ้า",
                    "ห้ามจับบริเวณยางรอยต่อระหว่างขบวนรถไฟฟ้า",
                    "ห้ามวางมือบริเวณประตูรถไฟฟ้า",
                    "ห้ามวางสัมภาระกีดขวางทางเดินในขบวนรถ",
                    "ห้ามรับประทานอาหารและเครื่องดื่มในขบวนรถไฟฟ้า",
                ),
            )
    }
}

@Composable
private fun AnnouncementRegulationsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "ประกาศและระเบียบข้อบังคับในการใช้บริการ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "สรุปจากประกาศกรุงเทพมหานคร และข้อบังคับของบริษัท ระบบขนส่งมวลชนกรุงเทพ จำกัด (มหาชน)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "ประกาศกรุงเทพมหานคร",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
        )
        BulletItem(text = "ระบบขนส่งมวลชนกรุงเทพมหานครเป็นระบบขนส่งมวลชนสาธารณะเพื่อประโยชน์ของประชาชนโดยส่วนรวม")
        BulletItem(text = "ผู้ใช้บริการต้องปฏิบัติตามข้อบังคับของระบบ และกฎหมายที่เกี่ยวข้อง")
        BulletItem(text = "ห้ามพกพาอาวุธ วัตถุอันตราย วัตถุไวไฟ หรือสิ่งที่อาจก่ออันตรายในเขตระบบ")
        BulletItem(text = "ห้ามขีด เขียน พ่นสี หรือทำให้รถไฟฟ้า สถานี บันได และอุปกรณ์ในระบบเสียหาย")
        BulletItem(text = "ห้ามบ้วนหรือถ่มน้ำลาย ทิ้งสิ่งของ สูบบุหรี่ ส่งเสียงดัง ทะเลาะวิวาท หรือรบกวนผู้อื่น")
        BulletItem(text = "ห้ามนำสัตว์เข้าระบบ ยกเว้นกรณีที่กฎหมายหรือระบบอนุญาต เช่น สุนัขนำทางสำหรับคนตาบอด")
        BulletItem(text = "ห้ามจำหน่ายสินค้า บริการ หรือกระทำการใด ๆ โดยไม่ได้รับอนุญาต")

        Text(
            text = "ข้อบังคับสำคัญของบริษัท",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
        )
        BulletItem(text = "ข้อบังคับระบบขนส่งมวลชนกรุงเทพมหานครมีผลใช้บังคับตั้งแต่วันที่ 5 ธันวาคม 2542")
        BulletItem(text = "บริษัทอาจเปิดหรือปิดทางเข้าออกบางจุด หรือระงับบริการบางส่วนได้เมื่อมีเหตุจำเป็นเพื่อความปลอดภัย")
        BulletItem(text = "ผู้โดยสารต้องใช้ตั๋วหรือบัตรโดยสารที่ถูกต้อง และชำระค่าโดยสารตามอัตราที่ประกาศ")
        BulletItem(text = "เด็กที่มีความสูงไม่เกิน 90 เซนติเมตร จะได้รับยกเว้นไม่ต้องชำระค่าโดยสาร และผู้ปกครองกรุณาอุ้มเด็กขณะเดินผ่านเข้า-ออกประตูอัตโนมัติเพื่อความปลอดภัย หากมีความสูงเกินที่กำหนดจะต้องชำระค่าโดยสารตามปกติ สามารถตรวจสอบจากเครื่องวัดความสูงบริเวณเครื่องจำหน่ายตั๋วโดยสาร")
        BulletItem(text = "ผู้โดยสารที่ใช้บัตรโดยสารผ่านประตูอัตโนมัติเข้ามาในระบบเดินทางแล้ว จะสามารถอยู่ในระบบเดินทางได้ไม่เกิน 300 นาที หากเกินเวลาที่กำหนดจะต้องชำระค่าปรับในอัตราค่าโดยสารสูงสุดที่ประกาศเรียกเก็บ")
        BulletItem(text = "หากเดินทางเกินระยะหรือชำระค่าโดยสารไม่ครบ ต้องชำระค่าโดยสารส่วนเกิน เบี้ยปรับ หรือเงินอื่นตามข้อบังคับ")
        BulletItem(text = "ผู้โดยสารควรตรวจสอบตั๋วและเงินทอนทันทีหลังซื้อ บริษัทอาจไม่รับผิดชอบความผิดพลาดที่ไม่ได้แจ้งในขณะออกตั๋ว")
        BulletItem(text = "ห้ามปีน กระโดดข้าม หรือข้ามสิ่งกีดขวางและประตูอัตโนมัติในเขตระบบ")
        BulletItem(text = "ห้ามเข้าเขตหวงห้าม เว้นแต่ได้รับอนุญาตจากบริษัท")
        BulletItem(text = "พนักงานมีสิทธิขอตรวจหลักฐาน และให้ผู้ที่สงสัยว่าฝ่าฝืนข้อบังคับออกจากรถไฟฟ้าหรือเขตระบบได้")
        BulletItem(text = "บริษัทสงวนสิทธิแก้ไข เปลี่ยนแปลง หรือยกเลิกข้อบังคับ โดยจะประกาศแจ้งให้ทราบเป็นคราว ๆ ไป")
    }
}
@Composable
private fun RulesGroup(
    title: String,
    dos: List<String>,
    donts: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "ข้อปฏิบัติ",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
        )
        dos.forEach { BulletItem(text = it) }
        Text(
            text = "ข้อห้าม",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC62828),
        )
        donts.forEach { BulletItem(text = it) }
    }
}
@Composable
private fun TravelCardsCard() {
    ExpandableTutorialCard(
        title = "บัตรโดยสาร",
        description = "บัตร Rabbit ใช้แตะเข้า-ออกประตูอัตโนมัติได้สะดวก เหมาะกับผู้ที่เดินทางด้วย BTS เป็นประจำ และมีหลายประเภทตามสิทธิ์ของผู้โดยสาร",
    ) {
            RabbitCardType(
                imageRes = R.drawable.rabbit_adult,
                title = "Adult Card",
                description = "บัตรสำหรับบุคคลทั่วไป ใช้เติมเงินและแตะเดินทางในระบบ BTS",
            )
            RabbitCardType(
                imageRes = R.drawable.rabbit_student,
                title = "Student Card",
                description = "บัตรสำหรับนักเรียน/นักศึกษา ใช้ตามเงื่อนไขและสิทธิ์ที่กำหนด",
            )
            RabbitCardType(
                imageRes = R.drawable.rabbit_senior,
                title = "Senior Card",
                description = "บัตรสำหรับผู้สูงอายุ ใช้ตามเงื่อนไขและสิทธิ์ส่วนลดที่ระบบกำหนด",
            )
            BulletItem(text = "ควรเติมเงินให้เพียงพอก่อนเดินทาง เพื่อลดเวลารอที่เครื่องจำหน่ายตั๋ว")
            BulletItem(text = "ตรวจสอบยอดเงินคงเหลือได้ที่สถานีหรือจุดบริการที่รองรับ")
    }
}

@Composable
private fun RabbitCardType(
    imageRes: Int,
    title: String,
    description: String,
) {
    var showZoomDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(72.dp)
                .height(104.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(role = Role.Button) { showZoomDialog = true },
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showZoomDialog) {
        ZoomableResourceImageDialog(
            imageRes = imageRes,
            title = title,
            onDismiss = { showZoomDialog = false },
        )
    }
}

@Composable
private fun OperatingHoursCard() {
    ExpandableTutorialCard(
        title = "เวลาให้บริการ",
        description = "เปิดให้บริการโดยประมาณ 06:00 - 24:00 น. และมีตารางเวลารถไฟฟ้าขบวนแรก-ขบวนสุดท้าย",
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
    ) {
            Text(
                text = "เปิดให้บริการโดยประมาณ 06:00 - 24:00 น. ผู้โดยสารควรรอที่ชานชาลาก่อนรถไฟมาถึง และเตรียมบัตรหรือเหรียญให้พร้อมก่อนใช้บริการ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TutorialImageStep(
                imageRes = R.drawable.bts_service_timetable_p1,
                title = "ตารางเวลาให้บริการ BTS",
                description = "แสดงเวลารถไฟฟ้าขบวนแรกและขบวนสุดท้ายของสายสุขุมวิทและสายสีลม",
                contentScale = ContentScale.Fit
            )
            TutorialImageStep(
                imageRes = R.drawable.bts_service_timetable_p2,
                title = "ความถี่ของรถไฟฟ้า BTS",
                description = "แสดงช่วงเวลาระหว่างขบวนในวันจันทร์-ศุกร์ วันเสาร์-อาทิตย์ และวันหยุดนักขัตฤกษ์",
                contentScale = ContentScale.Fit
            )
    }
}

@Composable
private fun ContactSupportCard() {
    val context = LocalContext.current
    
    ExpandableTutorialCard(
        title = "ศูนย์บริการและช่วยเหลือ",
        description = "เบอร์โทรสำหรับติดต่อศูนย์บริการ BTS, MRT และ Airport Rail Link",
    ) {
            ContactRow(label = "BTS Hotline:", value = "02-617-6000") {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:026176000"))
                context.startActivity(intent)
            }
            ContactRow(label = "MRT Information:", value = "02-624-5200") {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:026245200"))
                context.startActivity(intent)
            }
            ContactRow(label = "ARL Call Center:", value = "1690") {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1690"))
                context.startActivity(intent)
            }
    }
}

@Composable
private fun SafetyTipsCard() {
    ExpandableTutorialCard(
        title = "ข้อควรระวังและความปลอดภัย",
        description = "ข้อควรปฏิบัติบนชานชาลา ในขบวนรถ และการใช้อุปกรณ์ฉุกเฉิน",
    ) {
            BulletItem(text = "ยืนรอหลังเส้นสีเหลืองบนชานชาลาเสมอ")
            BulletItem(text = "ระวังช่องว่างระหว่างตัวรถและชานชาลา (Mind the gap)")
            BulletItem(text = "งดรับประทานอาหารและเครื่องดื่มภายในระบบรถไฟฟ้า")
            BulletItem(text = "ปุ่มหยุดรถฉุกเฉินบนชานชาลาใช้เฉพาะกรณีมีคนหรือสิ่งของตกลงราง")
            BulletItem(text = "ปุ่มติดต่อพนักงานขับรถในขบวนใช้แจ้งเหตุเจ็บป่วย เหตุทะเลาะวิวาท หรือเหตุด้านความปลอดภัย")
            BulletItem(text = "อุปกรณ์เปิดประตูฉุกเฉินใช้เฉพาะเมื่อรถจอดสนิทและมีเหตุจำเป็นในการอพยพ")
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FareRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (color != MaterialTheme.colorScheme.onSurface) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun ContactRow(label: String, value: String, onCallClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onCallClick) {
            Text("📞", fontSize = 18.sp)
        }
    }
}

@Composable
private fun FacilitiesCard() {
    ExpandableTutorialCard(
        title = "สิ่งอำนวยความสะดวกในสถานี",
        description = "ข้อมูลสำหรับวางแผนการเดินทาง โดยเฉพาะผู้ใช้รถเข็น ผู้สูงอายุ ผู้โดยสารที่มีกระเป๋าเดินทาง หรือผู้ที่ต้องการความช่วยเหลือ",
    ) {
            BulletItem(text = "สถานีหลักหลายแห่งมีลิฟต์เชื่อมต่อจากระดับถนนไปยังชั้นจำหน่ายตั๋วและชานชาลา")
            BulletItem(text = "หากต้องการใช้ห้องน้ำหรือขอความช่วยเหลือเร่งด่วน สามารถติดต่อเจ้าหน้าที่ที่เคาน์เตอร์บริการลูกค้า")
            BulletItem(text = "สถานีขนาดใหญ่บางแห่งมีร้านค้า ตู้ ATM และจุดบริการแลกเปลี่ยนเงินตรา")
            BulletItem(text = "กรณีของหาย ให้แจ้งรายละเอียด รูปภาพ พิกัด หรือขบวนที่โดยสารกับเจ้าหน้าที่สถานี หรือโทร BTS Hotline")
    }
}

@Preview(showBackground = true)
@Composable
private fun TutorialsScreenPreview() {
    CityFlowBKKTheme {
        TutorialsScreen(
            section = TutorialContentSection.Usage,
            onBackClick = {},
        )
    }
}
