import java.util.*
import kotlin.math.abs

fun main() {
    val testInput = readInput("Day20_test")
        .map(String::toLong)
    val input = readInput("Day20")
        .map(String::toLong)

    part1(testInput).also {
        println("Part 1, test input: $it")
        check(it == 3L)
    }

    part1(input).also {
        println("Part 1, real input: $it")
        check(it == 4914L)
    }

    part2(testInput).also {
        println("Part 2, test input: $it")
        check(it == 1623178306L)
    }

    part2(input).also {
        println("Part 2, real input: $it")
        check(it == 7973051839072)
    }
}

private data class V(
    val value: Long,
    val originalPosition: Int
) {
    override fun toString(): String {
        return "$value($originalPosition)"
    }
}

private fun part1(input: List<Long>): Long {
    val mixedList: MutableList<V> = LinkedList(input.mapIndexed { index, i -> V(i, index) })
    mixedList.mix(input)

    val indexOfZero = mixedList.indexOfFirst { v -> v.value == 0L }
    return mixedList[(1000 + indexOfZero) % input.size].value +
            mixedList[(2000 + indexOfZero) % input.size].value +
            mixedList[(3000 + indexOfZero) % input.size].value
}

private fun part2(input: List<Long>): Long {
    val input2 = input.map { i -> i * 811589153L }
    val mixedList: MutableList<V> = LinkedList(input2.mapIndexed { index, i -> V(i, index) })

    repeat(10) {
        mixedList.mix(input2)
    }
    val indexOfZero = mixedList.indexOfFirst { v -> v.value == 0L }
    return mixedList[(1000 + indexOfZero) % input2.size].value +
            mixedList[(2000 + indexOfZero) % input2.size].value +
            mixedList[(3000 + indexOfZero) % input2.size].value
}

private fun MutableList<V>.mix(input: List<Long>) {
    val listSize = input.size
    input.forEachIndexed { index, i ->
        val oldPosition = indexOfFirst { v -> v.originalPosition == index }
        val repeatCnt = (abs(i) % (listSize - 1))
        if (i > 0) {
            val newPosition = (oldPosition + repeatCnt) % listSize
            moveValue(oldPosition, newPosition.toInt())
        } else if (i < 0) {
            val newPosition = (oldPosition + listSize - repeatCnt - 1) % listSize
            moveValue(oldPosition, newPosition.toInt())
        }
    }
}

private fun <T> MutableList<T>.moveValue(from: Int, to: Int) {
    val oldValue = this[from]
    if (to > from) {
        add(to + 1, oldValue)
        removeAt(from)
    } else if (to < from) {
        removeAt(from)
        add(to + 1, oldValue)
    }
}

