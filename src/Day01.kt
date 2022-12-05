fun main() {
    fun part1(input: List<String>): Int {
        var current = 0
        var max: Int = -1
        input.forEach {s: String ->
            when {
                s.isEmpty() -> {
                    max = maxOf(max, current)
                    current = 0
                }
                else -> {
                    current += s.toInt()
                }
            }
        }
        max = maxOf(max, current)
        return max
    }

    fun part2(input: List<String>): Int {
        var current = 0
        val max = intArrayOf(-1, -1, -1, -1)
        input.forEach {s: String ->
            when {
                s.isEmpty() -> {
                    max[3] = current
                    max.sortDescending()
                    max[3] = -1
                    current = 0
                }
                else -> {
                    current += s.toInt()
                }
            }
        }
        max[3] = current
        max.sortDescending()
        max[3] = 0
        return max.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
