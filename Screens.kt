package com.example.poolcalculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ══════════════ PAGE 1 : DIMENSIONS ══════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    vm: PoolViewModel,
    onCalculate: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit
) {
    val u = vm.dimUnit.label
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Pool Dimensions") },
            actions = {
                TextButton(onClick = onHistory) { Text("History") }
                TextButton(onClick = onSettings) { Text("Settings") }
            }
        )
    }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = vm.projectName,
                onValueChange = { vm.projectName = it },
                label = { Text("Project / Client name (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            UnitSelector(
                "Measurement unit — values convert automatically",
                LengthUnit.values().toList(), vm.dimUnit, { it.label }
            ) { vm.setDimUnit(it) }

            Line()

            NumberField("Length", vm.lengthText, { vm.lengthText = it }, u)
            NumberField("Width", vm.widthText, { vm.widthText = it }, u)

            SectionTitle("Depth")
            NumberField("Shallow end depth", vm.shallowText, { vm.shallowText = it }, u)
            NumberField("Deep end depth", vm.deepText, { vm.deepText = it }, u)

            SectionTitle("Freeboard (added to wall height)")
            NumberField("Freeboard", vm.freeboardText, { vm.freeboardText = it }, u)
            Text(
                "Default 0.5 Feet = 0.1524 Meter — converts with the unit above.",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "Average depth: ${fmt(vm.avgDepthFt)} ft  /  ${fmt(vm.avgDepthFt / FT_PER_M)} m",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))
            Button(onClick = onCalculate, Modifier.fillMaxWidth().height(52.dp)) {
                Text("CALCULATE", fontSize = 16.sp)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/* ══════════════ PAGE 2 : RESULTS ══════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(vm: PoolViewModel, onBack: () -> Unit, onCost: () -> Unit) {
    val lbl = vm.resultAreaUnit.label
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Results") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
    }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitSelector(
                "Show area in — converts automatically",
                AreaUnit.values().toList(), vm.resultAreaUnit, { it.label }
            ) { vm.resultAreaUnit = it }

            Line()

            ResultCard(
                "TOTAL SURFACE AREA",
                "${money(vm.totalArea)} $lbl",
                "Base area + four walls (depth + ${vm.freeboardLabel})",
                highlight = true
            )
            ResultCard("Base / floor area",
                "${money(vm.area(vm.baseAreaFt2, vm.resultAreaUnit))} $lbl", "L × W")
            ResultCard("Four walls area",
                "${money(vm.area(vm.wallAreaFt2, vm.resultAreaUnit))} $lbl",
                "2 × (L + W) × (Depth + freeboard)")

            Line()

            ResultCard(
                "TOTAL VOLUME OF WATER",
                "${money(vm.volumeLitres)} Litres",
                "L × W × D × ${fmt(vm.litresPerCuFt)}   =   ${money(vm.volumeCuFt)} cu.ft  /  ${money(vm.volumeCuM)} m³"
            )
            ResultCard(
                "FILTER REQUIRED",
                "${money(vm.filterLph)} Litres / hour",
                "Total volume ÷ ${fmt(vm.filterDivisor, 0)}"
            )

            Spacer(Modifier.height(6.dp))
            Button(
                onClick = { vm.prepareCostPage(); onCost() },
                Modifier.fillMaxWidth().height(52.dp)
            ) { Text("COST", fontSize = 16.sp) }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/* ══════════════ PAGE 3 : COST ══════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostScreen(vm: PoolViewModel, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val a = vm.costUnit.label

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Cost Estimate") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
    }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitSelector(
                "Rate unit — rates convert automatically",
                AreaUnit.values().toList(), vm.costUnit, { "Per ${it.label}" }
            ) { vm.setCostUnit(it) }

            UnitSelector(
                "Rate type",
                RateMode.values().toList(), vm.rateMode,
                { if (it == RateMode.SINGLE) "Single rate" else "Floor + Wall" }
            ) { vm.setRateMode(it) }

            Line()
            Text(
                "Floor ${money(vm.floorArea)} + Walls ${money(vm.wallArea)} = ${money(vm.areaForCost)} $a",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (vm.rateMode == RateMode.SINGLE) {
                NumberField("Cost per $a", vm.rateText, vm::onRateChanged, "Rs")
                NumberField("Cost of the pool", vm.poolCostText, vm::onPoolCostChanged, "Rs")
                Text("↑ Linked both ways — edit either field.",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                NumberField("Floor rate per $a", vm.floorRateText, vm::onFloorRateChanged, "Rs")
                NumberField("Wall rate per $a", vm.wallRateText, vm::onWallRateChanged, "Rs")
                ResultCard("COST OF THE POOL", "Rs ${money(vm.poolCost)}",
                    "(floor rate × floor area) + (wall rate × wall area)")
            }

            Line()
            SectionTitle("GST @ ${fmt(vm.gstPercent, 0)}%")

            NumberField("Taxable portion of pool cost", vm.taxPercentText,
                vm::onTaxPercentChanged, "%")
            NumberField("…that portion in Rupees", vm.taxableAmtText,
                vm::onTaxableAmtChanged, "Rs")

            ResultCard(
                "GST @ ${fmt(vm.gstPercent, 0)}% on ${fmt(vm.taxPercentText.toD(), 0)}% " +
                        "(Rs ${money(vm.taxableAmt)}) of pool cost",
                "Rs ${money(vm.gstAmount)}"
            )

            Line()
            ResultCard("TOTAL COST OF THE POOL", "Rs ${money(vm.grandTotal)}",
                "Cost of pool + GST", highlight = true)

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        HistoryStore.add(ctx, vm.toEstimate())
                        Toast.makeText(ctx, "Saved to history", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }

                OutlinedButton(
                    onClick = { Exporter.shareText(ctx, vm.shareText(), whatsAppOnly = true) },
                    modifier = Modifier.weight(1f)
                ) { Text("WhatsApp") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { Exporter.shareText(ctx, vm.shareText(), whatsAppOnly = false) },
                    modifier = Modifier.weight(1f)
                ) { Text("Share text") }

                Button(
                    onClick = {
                        val uri = Exporter.makePdf(
                            ctx, vm.projectName.ifBlank { "Pool Estimate" }, vm.reportRows()
                        )
                        uri?.let { Exporter.sharePdf(ctx, it) }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Export PDF") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ══════════════ SETTINGS ══════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: PoolViewModel, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
    }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle("Freeboard")
            NumberField("Added to wall height", vm.freeboardText,
                { vm.freeboardText = it }, vm.dimUnit.label)
            Text("Stored as a real length: 0.5 Feet ⇄ 0.1524 Meter.",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Line()
            SectionTitle("Volume")
            NumberField("Litres per cubic foot", vm.litresPerCuFtText,
                { vm.litresPerCuFtText = it }, "L")
            Text("Standard = 28.32  (use 28.3168 for exact).",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Line()
            SectionTitle("Filter")
            NumberField("Turnover divisor", vm.filterDivisorText,
                { vm.filterDivisorText = it }, "hrs")
            Text("Filter capacity = total volume ÷ this value.",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Line()
            SectionTitle("Tax")
            NumberField("GST percentage", vm.gstPercentText,
                { vm.gstPercentText = it }, "%")

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { vm.resetSettings() }, Modifier.fillMaxWidth()) {
                Text("Reset to defaults")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ══════════════ HISTORY ══════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var items by remember { mutableStateOf(HistoryStore.load(ctx)) }
    val df = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Saved Estimates") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            actions = {
                if (items.isNotEmpty()) TextButton(onClick = {
                    HistoryStore.clear(ctx); items = emptyList()
                }) { Text("Clear all") }
            }
        )
    }) { pad ->
        if (items.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No saved estimates yet.")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { e ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(e.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(df.format(Date(e.dateMillis)), fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Text("${fmt(e.length)} × ${fmt(e.width)} ${e.unitLabel}, " +
                                    "depth ${fmt(e.shallow)}–${fmt(e.deep)}", fontSize = 13.sp)
                            Text("Area: ${money(e.totalAreaFt2)} Sq.Ft", fontSize = 13.sp)
                            Text("Volume: ${money(e.volumeLitres)} L  •  Filter: ${money(e.filterLph)} L/hr",
                                fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Pool Rs ${money(e.poolCost)} + GST Rs ${money(e.gst)}", fontSize = 13.sp)
                            Text("TOTAL  Rs ${money(e.total)}",
                                fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    Exporter.shareText(ctx, buildString {
                                        appendLine("🏊 ${e.name}")
                                        appendLine("Area: ${money(e.totalAreaFt2)} Sq.Ft")
                                        appendLine("Volume: ${money(e.volumeLitres)} L")
                                        appendLine("Filter: ${money(e.filterLph)} L/hr")
                                        appendLine("Total: Rs ${money(e.total)}")
                                    }, false)
                                }) { Text("Share") }
                                TextButton(onClick = {
                                    HistoryStore.delete(ctx, e.id); items = HistoryStore.load(ctx)
                                }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}
