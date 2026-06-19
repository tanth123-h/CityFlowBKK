package com.example.cityflowbkk.features.tutorials

data class MrtStation(
    val code: String,
    val name: String,
    val line: MrtLine,
)

enum class MrtLine {
    Blue,
    Purple,
}

object MrtFareData {
    private val blueLineStations = listOf(
        MrtStation("BL01", "ท่าพระ", MrtLine.Blue),
        MrtStation("BL02", "จรัญฯ 13", MrtLine.Blue),
        MrtStation("BL03", "ไฟฉาย", MrtLine.Blue),
        MrtStation("BL04", "บางขุนนนท์", MrtLine.Blue),
        MrtStation("BL05", "บางยี่ขัน", MrtLine.Blue),
        MrtStation("BL06", "สิรินธร", MrtLine.Blue),
        MrtStation("BL07", "บางพลัด", MrtLine.Blue),
        MrtStation("BL08", "บางอ้อ", MrtLine.Blue),
        MrtStation("BL09", "บางโพ", MrtLine.Blue),
        MrtStation("BL10", "เตาปูน", MrtLine.Blue),
        MrtStation("BL11", "บางซื่อ", MrtLine.Blue),
        MrtStation("BL12", "กำแพงเพชร", MrtLine.Blue),
        MrtStation("BL13", "สวนจตุจักร", MrtLine.Blue),
        MrtStation("BL14", "พหลโยธิน", MrtLine.Blue),
        MrtStation("BL15", "ลาดพร้าว", MrtLine.Blue),
        MrtStation("BL16", "รัชดาภิเษก", MrtLine.Blue),
        MrtStation("BL17", "สุทธิสาร", MrtLine.Blue),
        MrtStation("BL18", "ห้วยขวาง", MrtLine.Blue),
        MrtStation("BL19", "ศูนย์วัฒนธรรมแห่งประเทศไทย", MrtLine.Blue),
        MrtStation("BL20", "พระราม 9", MrtLine.Blue),
        MrtStation("BL21", "เพชรบุรี", MrtLine.Blue),
        MrtStation("BL22", "สุขุมวิท", MrtLine.Blue),
        MrtStation("BL23", "ศูนย์การประชุมแห่งชาติสิริกิติ์", MrtLine.Blue),
        MrtStation("BL24", "คลองเตย", MrtLine.Blue),
        MrtStation("BL25", "ลุมพินี", MrtLine.Blue),
        MrtStation("BL26", "สีลม", MrtLine.Blue),
        MrtStation("BL27", "สามย่าน", MrtLine.Blue),
        MrtStation("BL28", "หัวลำโพง", MrtLine.Blue),
        MrtStation("BL29", "วัดมังกร", MrtLine.Blue),
        MrtStation("BL30", "สามยอด", MrtLine.Blue),
        MrtStation("BL31", "สนามไชย", MrtLine.Blue),
        MrtStation("BL32", "อิสรภาพ", MrtLine.Blue),
        MrtStation("BL33", "ท่าพระ", MrtLine.Blue),
        MrtStation("BL34", "บางไผ่", MrtLine.Blue),
        MrtStation("BL35", "บางหว้า", MrtLine.Blue),
        MrtStation("BL36", "เพชรเกษม 48", MrtLine.Blue),
        MrtStation("BL37", "ภาษีเจริญ", MrtLine.Blue),
        MrtStation("BL38", "หลักสอง", MrtLine.Blue),
    )

    private val purpleLineStations = listOf(
        MrtStation("PP01", "คลองบางไผ่", MrtLine.Purple),
        MrtStation("PP02", "ตลาดบางใหญ่", MrtLine.Purple),
        MrtStation("PP03", "สามแยกบางใหญ่", MrtLine.Purple),
        MrtStation("PP04", "บางพลู", MrtLine.Purple),
        MrtStation("PP05", "บางรักใหญ่", MrtLine.Purple),
        MrtStation("PP06", "บางรักน้อยท่าอิฐ", MrtLine.Purple),
        MrtStation("PP07", "ไทรม้า", MrtLine.Purple),
        MrtStation("PP08", "สะพานพระนั่งเกล้า", MrtLine.Purple),
        MrtStation("PP09", "แยกนนทบุรี 1", MrtLine.Purple),
        MrtStation("PP10", "บางกระสอ", MrtLine.Purple),
        MrtStation("PP11", "ศูนย์ราชการนนทบุรี", MrtLine.Purple),
        MrtStation("PP12", "กระทรวงสาธารณสุข", MrtLine.Purple),
        MrtStation("PP13", "แยกติวานนท์", MrtLine.Purple),
        MrtStation("PP14", "วงศ์สว่าง", MrtLine.Purple),
        MrtStation("PP15", "บางซ่อน", MrtLine.Purple),
        MrtStation("PP16", "เตาปูน", MrtLine.Purple),
    )

    val stations = blueLineStations + purpleLineStations
    val stationCodes = stations.map { it.code }
    val stationNames = stations.associate { station ->
        station.code to "${station.name} (${station.line.displayName})"
    }

    fun calculateFare(fromCode: String, toCode: String): Int? {
        val from = stations.find { it.code == fromCode } ?: return null
        val to = stations.find { it.code == toCode } ?: return null

        return when {
            from.line == MrtLine.Blue && to.line == MrtLine.Blue ->
                calculateBlueLineFare(fromCode, toCode)
            from.line == MrtLine.Purple && to.line == MrtLine.Purple ->
                calculatePurpleLineFare(fromCode, toCode)
            else -> calculateBluePurpleFare(from, to)
        }
    }

    private fun calculateBlueLineFare(fromCode: String, toCode: String): Int {
        val distance = stationDistance(blueLineStations, fromCode, toCode)
        return (17 + distance * 2).coerceAtMost(44)
    }

    private fun calculatePurpleLineFare(fromCode: String, toCode: String): Int {
        val distance = stationDistance(purpleLineStations, fromCode, toCode)
        return (17 + distance * 2).coerceAtMost(42)
    }

    private fun calculateBluePurpleFare(from: MrtStation, to: MrtStation): Int {
        val blueStation = if (from.line == MrtLine.Blue) from.code else to.code
        val purpleStation = if (from.line == MrtLine.Purple) from.code else to.code
        val blueFare = calculateBlueLineFare(blueStation, "BL10")
        val purpleFare = calculatePurpleLineFare(purpleStation, "PP16")
        return (blueFare + purpleFare - 17).coerceAtMost(71)
    }

    private fun stationDistance(stations: List<MrtStation>, fromCode: String, toCode: String): Int {
        val fromIndex = stations.indexOfFirst { it.code == fromCode }
        val toIndex = stations.indexOfFirst { it.code == toCode }
        return kotlin.math.abs(fromIndex - toIndex)
    }
}

private val MrtLine.displayName: String
    get() = when (this) {
        MrtLine.Blue -> "สายสีน้ำเงิน"
        MrtLine.Purple -> "สายสีม่วง"
    }
