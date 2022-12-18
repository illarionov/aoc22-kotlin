fun main() {
    val testInput = readInput("Day18_test")
        .map(String::toCoordinates)
    val input = readInput("Day18")
        .map(String::toCoordinates)

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 64)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        // check(it == 3576)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 58)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        // check(it == 2066)
    }
}

private fun part1(input: List<Xyz>): Int {
    val set = input.toSet()
    return input.sumOf { (x, y, z) ->
        directions.map { (dx, dy, dz) ->
            Xyz(x = x + dx, y = y + dy, z = z + dz)
        }.count {
            it !in set
        }
    }
}

enum class OneSideStatus {
    BLOCKED, OPEN
}

private fun part2(input: List<Xyz>): Int {
    val open = mutableSetOf<Xyz>()
    val blocksSet = input.toSet()
    val xRange = input.minOf { it.x }..input.maxOf { it.x }
    val yRange = input.minOf { it.y }..input.maxOf { it.y }
    val zRange = input.minOf { it.z }..input.maxOf { it.z }

    fun getStatus(b: Xyz, d: XyzDirection): OneSideStatus {
        var (xx, yy, zz) = listOf(b.x + d.dx, b.y + d.dy, b.z + d.dz)
        while (xx in xRange && yy in yRange && zz in zRange) {
            val p = Xyz(xx, yy, zz)
            if (p in blocksSet) {
                return OneSideStatus.BLOCKED
            }
            if (p in open) {
                return OneSideStatus.OPEN
            }
            xx += d.dx
            yy += d.dy
            zz += d.dz
        }
        return OneSideStatus.OPEN
    }

    // Прокатило. Тут должен быть BFS, по идее
    repeat(2) {
        for (z in zRange.first - 1..zRange.last + 1) {
            for (y in yRange.first - 1..yRange.last + 1) {
                for (x in xRange.first - 1..xRange.last + 1) {
                    val p = Xyz(x, y, z)
                    if (p in blocksSet || p in open) continue
                    val status = directions.map { d -> getStatus(p, d) }
                    if (status.any { it == OneSideStatus.OPEN }) {
                        open += p
                    }
                }
            }
        }
    }

    return input.sumOf { xyz ->
        directions.map { (dx, dy, dz) ->
            xyz.copy(x = xyz.x + dx, y = xyz.y + dy, z = xyz.z + dz)
        }.count {
            it in open
        }
    }
}

private data class Xyz(val x: Int, val y: Int, val z: Int)
private data class XyzDirection(val dx: Int = 0, val dy: Int = 0, val dz: Int = 0)

private val directions = listOf(
    XyzDirection(dx = -1),
    XyzDirection(dx = 1),
    XyzDirection(dy = -1),
    XyzDirection(dy = 1),
    XyzDirection(dz = -1),
    XyzDirection(dz = 1),
)

private fun String.toCoordinates(): Xyz {
    return this.split(",").let { (x, y, z) ->
        Xyz(x.toInt(), y.toInt(), z.toInt())
    }
}