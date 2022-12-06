fun main() {
    fun part1(input: String, markerLength: Int = 4): Int {
        var i = markerLength - 1
        while (i <= input.lastIndex) {
            var lastMatch: Int? = null
            var j = 0
            while (lastMatch == null && j < markerLength) {
                val compareSymbol = input[i - j]
                lastMatch = (j + 1 until markerLength).firstOrNull { k ->
                    input[i - k] == compareSymbol
                }
                j += 1
            }
            if (lastMatch != null) {
                i += (markerLength - lastMatch)
            } else {
                return i + 1
            }
        }
        return 0
    }

    fun part2(input: String): Int {
        //return input.size
        return part1(input, 14)
        // Dirty solution with windowed, sets
//        var i = 0
//        return input.windowed(14, 1) { t: CharSequence ->
//            if (t.toSet().size == 14) {
//                return@windowed i
//            }
//            i += 1
//            return@windowed -1
//        }.first { it != -1 } + 14
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test").single()
    val part1 = part1(testInput)
    println("part1: $part1")
    check(part1 == 11)

    val input = readInput("Day06").single()
    val part1Result = part1(input)
    println(part1Result)
    //check(part1Result == 1034)
    val part2Result = part2(input)
    println(part2Result)
    //check(part2Result == 2472)
}
