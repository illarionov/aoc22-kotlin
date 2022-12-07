data class File(
    val name: String,
    val size: Long
)

class Node(
    val name: String,
    var parent: Node?,
    val dirs: MutableMap<String, Node> = mutableMapOf(),
    val files: MutableList<File> = mutableListOf()
) {
    val size: Long
        get() {
            val fileSize = files.map(File::size).sum()
            val dirsSize = dirs.values.map(Node::size).sum()
            return fileSize + dirsSize
        }
}

enum class ParseState {
    WAITING_COMMAND,
    PARSE_LS_OUTPUT
}

val cdDirPattern = Regex("""\$ cd (.+)""")
val dirItemPattern = Regex("""dir (.+)""")
val fileItemPattern = Regex("""(\d+) (.+)""")

fun List<String>.parseTree(): Node {
    val root = Node(name = "/", null)
    root.parent = root

    var currentNode: Node = root
    var state: ParseState = ParseState.WAITING_COMMAND
    this.forEach { s ->
        if (s.startsWith("$ ")) {
            if (s == "$ ls") {
                state = ParseState.PARSE_LS_OUTPUT
            } else {
                val dirName = cdDirPattern.matchEntire(s)?.groupValues?.get(1)
                    ?: error("Can not parse command `$s`")
                currentNode = when (dirName) {
                    ".." -> currentNode.parent!!
                    "/" -> root
                    else -> {
                        currentNode.dirs[dirName]
                            ?: error("No such directory $dirName")
                    }
                }
                state = ParseState.WAITING_COMMAND
            }
        } else {
            check(state == ParseState.PARSE_LS_OUTPUT) {
                "Waiting LS output on string `$s`"
            }
            val result = dirItemPattern.matchEntire(s)
            if (result != null) {
                val dirName = result.groups[1]!!.value
                currentNode.dirs[dirName] = Node(dirName, currentNode)
            } else {
                val fileResult = fileItemPattern.matchEntire(s)?.groupValues
                check(fileResult != null) { "Unknown command `$s`" }
                currentNode.files += File(
                    name = fileResult[2],
                    size = fileResult[1].toLong(),
                )
            }
        }
    }
    return root
}

fun main() {
    fun getTotalSizeAtMost10000(root: Node): Long {
        val childSizes = root.dirs.values.sumOf(::getTotalSizeAtMost10000)
        val thisSize = root.size.let {
            if (it <= 100000) it else 0
        }
        return thisSize + childSizes
    }

    fun part1(input: List<String>): Long {
        val tree = input.parseTree()
        return getTotalSizeAtMost10000(tree)
    }

    fun findMinDirAtLeast(toDelete: Long, root: Node): Long {
        if (root.size < toDelete) return Long.MAX_VALUE
        return minOf(root.dirs.values.minOfOrNull { d ->
            findMinDirAtLeast(toDelete, d)
        } ?: root.size, root.size)
    }

    fun part2(input: List<String>): Long {
        val tree = input.parseTree()
        val freeSpace = 70000000 - tree.size
        check(freeSpace < 30000000)
        val toDelete = 30000000 - freeSpace
        return findMinDirAtLeast(toDelete, tree)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    val part1TestResult = part1(testInput)
    println(part1TestResult)
    check(part1TestResult == 95437L)

    val input = readInput("Day07")
    val part1Result = part1(input)
    //check(part1Result == 1427048L)
    println(part1Result)
    val part2Result = part2(input)
    //check(part2Result == 2940614L)
    println(part2Result)
}
