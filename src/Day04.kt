val pairPattern: Regex = Regex("""(\d+)-(\d+),(\d+)-(\d+)""")

data class SectionAssignment(
    val first: IntRange,
    val second: IntRange
)

fun String.parseSectionAssignment(): SectionAssignment {
    val result = pairPattern.matchEntire(this)?.groupValues ?:
        error ("Can not parse `$this`")
    return SectionAssignment(
        result[1].toInt() .. result[2].toInt(),
        result[3].toInt() .. result[4].toInt(),
    )
}

fun main() {
    fun part1(input: List<SectionAssignment>): Int {
        return input.count { a ->
            (a.first - a.second).isEmpty()
                || (a.second - a.first).isEmpty()
        }
    }

    fun part2(input: List<SectionAssignment>): Int {
        return input.count {
            a -> a.first.intersect(a.second).isNotEmpty()
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
        .map(String::parseSectionAssignment)
    println(part1(testInput))
    check(part1(testInput) == 2)

    val input = readInput("Day04")
        .map(String::parseSectionAssignment)
    println(part1(input))
    println(part2(input))
}
