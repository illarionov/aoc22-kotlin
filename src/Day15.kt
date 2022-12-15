import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.math.abs

fun main() {
    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
        .parseField()
    val part1TestResult = part1(testInput, 10)
    check(part1TestResult == 26)

    val input = readInput("Day15")
        .parseField()
    val part1Result = part1(input, 2000000)
    println(part1Result)

    val part2TestResult = part2(testInput, XY(20, 20))
    println(part2TestResult)
    check(part2TestResult == 56000011L)

    val part2Result = part2(input, XY(4000000, 4000000))
    println(part2Result)
}

private data class XY(val x: Int, val y: Int)

private data class SensorData(val sensor: XY, val beacon: XY) {
    val distance: Int = let {
        abs(it.sensor.x - it.beacon.x) + abs(it.sensor.y - it.beacon.y)
    }

    val leftTop: XY = XY(x = sensor.x - distance, sensor.y - distance)
    val rightBottom: XY = XY(x = sensor.x + distance, sensor.y + distance)

    fun getCoverageAt(y: Int): IntRange {
        val d = distance - abs(y - sensor.y)
        if (d < 0) return IntRange.EMPTY

        val left = sensor.x - d
        val right = sensor.x + d
        return left..right
    }
}

private class Area(
    val sensors: List<SensorData>
) {

    val xRange: IntRange = let { f ->
        val minX = f.sensors.minOf { it.leftTop.x }
        val maxX = f.sensors.maxOf { it.rightBottom.x }
        minX..maxX
    }

    val yRange: IntRange = let { f ->
        val minY = f.sensors.minOf { it.leftTop.y }
        val maxY = f.sensors.maxOf { it.rightBottom.y }
        minY..maxY
    }

    val beacons: Set<XY> by lazy(NONE) {
        sensors.map(SensorData::beacon).toSet()
    }

    fun getCoveragesAt(y: Int): List<IntRange> {
        return sensors
            .map { r -> r.getCoverageAt(y) }
            .filter { !it.isEmpty() }
    }
}

private fun part1(area: Area, y: Int): Int {
    val coverages = area.getCoveragesAt(y)
    return (area.xRange)
        .count { i ->
            coverages.any { i in it && XY(i, y) !in area.beacons }
        }
}

private fun part2(area: Area, max: XY): Long {
    val xMin = max(area.xRange.first, 0)
    val xMax = min(area.xRange.last, max.x)
    val yMin = max(area.yRange.first, 0)
    val yMax = min(area.yRange.last, max.y)

    var y = yMin
    while (y <= yMax) {
        var x = xMin
        val coverages = area.getCoveragesAt(y)
        while (x <= xMax) {
            val c = coverages.find { x in it }
            if (c != null) {
                x = c.last + 1
            } else {
                println("x: $x, y: $y")
                return x * 4_000_000L + y
            }
        }
        y += 1
    }

    return -1
}

private fun List<String>.parseField(): Area {
    return Area(sensors = this.map(String::parseReport))
}

private val reportStringRegex =
    """Sensor at x=([+-]?\d+), y=([+-]?\d+): closest beacon is at x=([+-]?\d+), y=([+-]?\d+)""".toRegex()

private fun String.parseReport(): SensorData {
    val (x, y, bx, by) = reportStringRegex.matchEntire(this)
        ?.groupValues
        ?.drop(1)
        ?.map(String::toInt)
        ?: error("Can not parse $this")

    return SensorData(
        sensor = XY(x, y),
        beacon = XY(bx, by)
    )
}

