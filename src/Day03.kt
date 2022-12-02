fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf { s: String ->
            val leftSet = s.substring(0, s.length / 2).toSet()
            val rightSet = s.substring(s.length / 2).toSet()
            val intersect = leftSet.intersect(rightSet)
            check(intersect.size == 1)
            intersect.first().priority
        }
    }

    fun part2(input: List<String>): Int {
        return input.windowed(3, 3) { l: List<String> ->
            val badges = l.map(String::toSet).reduce(Set<Char>::intersect)
            val c = badges.first()
            check(badges.size == 1)
            c.priority
        }.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)

    val input = readInput("Day03")
    val result1 = part1(input)
    println(result1)
    val input2 = readInput("Day03_2")
    val result2 = part2(input2)
    println(result2)
}

val Char.priority get() = when(this) {
    in 'a' .. 'z' -> this - 'a' + 1
    in 'A' .. 'Z' -> this - 'A' + 27
    else -> error("Unknown symbol")
}
