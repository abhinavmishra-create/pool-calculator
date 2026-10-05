package com.example.poolcalculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PoolViewModel : ViewModel() {

    /* ───────────────── PAGE 1 : DIMENSIONS ───────────────── */

    var projectName by mutableStateOf("")
    var dimUnit by mutableStateOf(LengthUnit.FEET); private set

    var lengthText  by mutableStateOf("30")
    var widthText   by mutableStateOf("15")
    var shallowText by mutableStateOf("4.5")
    var deepText    by mutableStateOf("4.5")

    /** FREEBOARD — a real length. 0.5 ft  ⇄  0.1524 m  */
    var freeboardText by mutableStateOf("0.5")

    /** Switching Feet ⇄ Meter converts every length, including freeboard. */
    fun setDimUnit(newUnit: LengthUnit) {
        if (newUnit == dimUnit) return
        val f = if (newUnit == LengthUnit.METER) 1.0 / FT_PER_M else FT_PER_M
        lengthText    = conv(lengthText, f)
        widthText     = conv(widthText, f)
        shallowText   = conv(shallowText, f)
        deepText      = conv(deepText, f)
        freeboardText = conv(freeboardText, f, 4)   // 0.5 ft -> 0.1524 m
        dimUnit = newUnit
    }

    private fun conv(t: String, f: Double, dec: Int = 3) =
        if (t.isBlank()) t else fmt(t.toD() * f, dec)

    private fun toFt(v: Double) = if (dimUnit == LengthUnit.FEET) v else v * FT_PER_M

    val lengthFt    get() = toFt(lengthText.toD())
    val widthFt     get() = toFt(widthText.toD())
    val shallowFt   get() = toFt(shallowText.toD())
    val deepFt      get() = toFt(deepText.toD())
    val freeboardFt get() = toFt(freeboardText.toD())
    val avgDepthFt  get() = (shallowFt + deepFt) / 2.0

    /** e.g. "0.50 Feet" or "0.1524 Meter" — used in the on-screen formula text */
    val freeboardLabel: String
        get() = "${fmt(freeboardText.toD(), if (dimUnit == LengthUnit.FEET) 2 else 4)} ${dimUnit.label}"


    /* ───────────────── SETTINGS (editable constants) ───────────────── */

    var litresPerCuFtText by mutableStateOf("28.32")  // 1 cu.ft = 28.32 litres
    var filterDivisorText by mutableStateOf("4")      // volume ÷ 4
    var gstPercentText    by mutableStateOf("18")     // GST %

    val litresPerCuFt get() = litresPerCuFtText.toD().let { if (it == 0.0) 28.32 else it }
    val filterDivisor get() = filterDivisorText.toD().let { if (it == 0.0) 4.0 else it }
    val gstPercent    get() = gstPercentText.toD()

    fun resetSettings() {
        litresPerCuFtText = "28.32"
        filterDivisorText = "4"
        gstPercentText = "18"
        freeboardText = if (dimUnit == LengthUnit.FEET) "0.5" else fmt(0.5 / FT_PER_M, 4)
    }


    /* ───────────────── PAGE 2 : RESULTS ───────────────── */

    var resultAreaUnit by mutableStateOf(AreaUnit.SQ_FEET)

    val baseAreaFt2 get() = lengthFt * widthFt
    val wallAreaFt2 get() = 2.0 * (lengthFt + widthFt) * (avgDepthFt + freeboardFt)
    val totalAreaFt2 get() = baseAreaFt2 + wallAreaFt2

    fun area(ft2: Double, u: AreaUnit) = if (u == AreaUnit.SQ_FEET) ft2 else ft2 / SQFT_PER_SQM
    val totalArea get() = area(totalAreaFt2, resultAreaUnit)

    /** ✅ CORRECTED:  Volume = L × W × D × 28.32  */
    val volumeCuFt   get() = lengthFt * widthFt * avgDepthFt
    val volumeCuM    get() = volumeCuFt / 35.3146667
    val volumeLitres get() = volumeCuFt * litresPerCuFt
    val filterLph    get() = volumeLitres / filterDivisor


    /* ───────────────── PAGE 3 : COST ───────────────── */

    var costUnit by mutableStateOf(AreaUnit.SQ_FEET); private set
    var rateMode by mutableStateOf(RateMode.SINGLE); private set

    var rateText      by mutableStateOf("750")   // single rate
    var floorRateText by mutableStateOf("750")   // split: floor
    var wallRateText  by mutableStateOf("750")   // split: walls
    var poolCostText  by mutableStateOf("")      // editable only in SINGLE mode

    var taxPercentText by mutableStateOf("100")  // % of pool cost that is taxable
    var taxableAmtText by mutableStateOf("")     // same portion, in ₹

    val floorArea   get() = area(baseAreaFt2, costUnit)
    val wallArea    get() = area(wallAreaFt2, costUnit)
    val areaForCost get() = area(totalAreaFt2, costUnit)

    val splitCost get() = floorRateText.toD() * floorArea + wallRateText.toD() * wallArea

    val poolCost: Double
        get() = if (rateMode == RateMode.SINGLE) poolCostText.toD() else splitCost

    fun prepareCostPage() {
        costUnit = resultAreaUnit
        if (rateText.isBlank()) rateText = "750"
        poolCostText = fmt(rateText.toD() * areaForCost)
        recalcTax()
    }

    fun setRateMode(m: RateMode) {
        if (m == rateMode) return
        rateMode = m
        if (m == RateMode.SINGLE) poolCostText = fmt(rateText.toD() * areaForCost)
        recalcTax()
    }

    /** Sq.Ft ⇄ Sq.M — rates convert so the total cost stays identical. */
    fun setCostUnit(newUnit: AreaUnit) {
        if (newUnit == costUnit) return
        val f = if (newUnit == AreaUnit.SQ_METER) SQFT_PER_SQM else 1.0 / SQFT_PER_SQM
        rateText      = fmt(rateText.toD() * f)
        floorRateText = fmt(floorRateText.toD() * f)
        wallRateText  = fmt(wallRateText.toD() * f)
        costUnit = newUnit
        poolCostText = fmt(rateText.toD() * areaForCost)
        recalcTax()
    }

    fun onRateChanged(t: String) {
        rateText = t
        poolCostText = fmt(t.toD() * areaForCost)
        recalcTax()
    }

    fun onPoolCostChanged(t: String) {
        poolCostText = t
        rateText = if (areaForCost > 0) fmt(t.toD() / areaForCost) else "0"
        recalcTax()
    }

    fun onFloorRateChanged(t: String) { floorRateText = t; recalcTax() }
    fun onWallRateChanged(t: String)  { wallRateText = t;  recalcTax() }

    fun onTaxPercentChanged(t: String) { taxPercentText = t; recalcTax() }

    fun onTaxableAmtChanged(t: String) {
        taxableAmtText = t
        taxPercentText = if (poolCost > 0) fmt(t.toD() / poolCost * 100.0) else "0"
    }

    private fun recalcTax() {
        taxableAmtText = fmt(poolCost * taxPercentText.toD() / 100.0)
    }

    val taxableAmt get() = taxableAmtText.toD()
    val gstAmount  get() = taxableAmt * gstPercent / 100.0
    val grandTotal get() = poolCost + gstAmount


    /* ───────────────── Report rows (shared by PDF + WhatsApp) ───────────────── */

    fun reportRows(): List<Pair<String, String>> {
        val u = dimUnit.label
        val a = costUnit.label
        return listOf(
            "Length"                to "${fmt(lengthText.toD())} $u",
            "Width"                 to "${fmt(widthText.toD())} $u",
            "Shallow depth"         to "${fmt(shallowText.toD())} $u",
            "Deep end depth"        to "${fmt(deepText.toD())} $u",
            "Average depth"         to "${fmt(if (dimUnit == LengthUnit.FEET) avgDepthFt else avgDepthFt / FT_PER_M)} $u",
            "Freeboard allowance"   to freeboardLabel,
            "—"                     to "",
            "Floor area"            to "${money(floorArea)} $a",
            "Wall area (4 walls)"   to "${money(wallArea)} $a",
            "TOTAL SURFACE AREA"    to "${money(areaForCost)} $a",
            "Total water volume"    to "${money(volumeLitres)} L  (${money(volumeCuM)} m³)",
            "Filter required"       to "${money(filterLph)} L/hr",
            "—"                     to "",
            if (rateMode == RateMode.SINGLE) "Rate per $a" else "Floor rate per $a"
                                    to "Rs ${money(if (rateMode == RateMode.SINGLE) rateText.toD() else floorRateText.toD())}",
        ) + (if (rateMode == RateMode.SPLIT)
                listOf("Wall rate per $a" to "Rs ${money(wallRateText.toD())}") else emptyList()
        ) + listOf(
            "Cost of pool"          to "Rs ${money(poolCost)}",
            "Taxable portion"       to "${fmt(taxPercentText.toD(), 0)}%  (Rs ${money(taxableAmt)})",
            "GST @ ${fmt(gstPercent, 0)}%" to "Rs ${money(gstAmount)}",
            "TOTAL COST"            to "Rs ${money(grandTotal)}"
        )
    }

    fun shareText(): String = buildString {
        appendLine("🏊 *SWIMMING POOL ESTIMATE*")
        if (projectName.isNotBlank()) appendLine("Project: $projectName")
        appendLine("————————————————")
        reportRows().forEach { (k, v) ->
            if (k == "—") appendLine("————————————————") else appendLine("$k : $v")
        }
    }

    fun toEstimate() = Estimate(
        id = System.currentTimeMillis(),
        name = projectName.ifBlank { "Pool Estimate" },
        dateMillis = System.currentTimeMillis(),
        unitLabel = dimUnit.label,
        length = lengthText.toD(), width = widthText.toD(),
        shallow = shallowText.toD(), deep = deepText.toD(),
        totalAreaFt2 = totalAreaFt2,
        volumeLitres = volumeLitres,
        filterLph = filterLph,
        poolCost = poolCost,
        gst = gstAmount,
        total = grandTotal
    )
}
