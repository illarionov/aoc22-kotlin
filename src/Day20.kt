import kotlin.math.abs

fun main() {
    val testInput = readInput("Day20_test").map(String::toLong)
    val input = readInput("Day20").map(String::toLong)

    part1v2(testInput).also {
        println("Part 1, test input: $it")
        check(it == 3L)
    }

    part1v2(input).also {
        println("Part 1 1, real input: $it")
        check(it == 4914L)
    }

    part2v2(testInput).also {
        println("Part 2 2, test input: $it")
        check(it == 1623178306L)
    }

    part2v2(input).also {
        println("Part 2 2, real input: $it")
        check(it == 7973051839072)
    }
}

private fun part1v2(input: List<Long>): Long {
    val mixer = Mixer(input)
    mixer.mix()
    return listOf(1000, 2000, 3000).sumOf(mixer::getValueFromPositionOfZero)
}

private fun part2v2(input: List<Long>): Long {
    val input2 = input.map { i -> i * 811589153L }
    val mixer = Mixer(input2)
    repeat(10) { mixer.mix() }
    return listOf(1000, 2000, 3000).sumOf(mixer::getValueFromPositionOfZero)
}

private class NumberNode(
    val value: Long
) {
    var pred: NumberNode? = null /* by Delegates.notNull() */
    var next: NumberNode? = null /* by Delegates.notNull() */
}

private class Mixer(input: List<Long>) {
    val nodes = run {
        val inputNodes: List<NumberNode> = input.map { i -> NumberNode(i) }
        inputNodes.zipWithNext().forEach { (pred, next) ->
            pred.next = next
            next.pred = pred
        }
        inputNodes.last().next = inputNodes.first()
        inputNodes.first().pred = inputNodes.last()
        inputNodes
    }

    val zeroNode = nodes.first { n -> n.value == 0L }

    private val size: Int = nodes.size

    fun mix() {
        nodes.forEach { currentNode ->
            val nodesTraverse = (abs(currentNode.value) % (size - 1)).toInt()
            if (nodesTraverse == 0 || currentNode.value == 0L) return@forEach

            var newLeft: NumberNode = currentNode
            var newRight: NumberNode = currentNode
            if (currentNode.value > 0) {
                repeat(nodesTraverse) {
                    newLeft = newLeft.next!!
                }
                newRight = newLeft.next!!
            } else {
                repeat(nodesTraverse) {
                    newRight = newRight.pred!!
                }
                newLeft = newRight.pred!!
            }
            val oldLeft = currentNode.pred
            val oldRight = currentNode.next

            newLeft.next = currentNode
            currentNode.pred = newLeft
            newRight.pred = currentNode
            currentNode.next = newRight

            oldLeft!!.next = oldRight
            oldRight!!.pred = oldLeft
        }
    }

    fun getValueFromPositionOfZero(position: Int): Long {
        return nodesFromZero()
            .drop(position % size)
            .first()
    }

    fun nodesFromZero(): Sequence<Long> {
        return sequence {
            val root = zeroNode
            var current = root
            do {
                yield(current.value)
                current = current.next!!
            } while (current != root)
        }
    }
}