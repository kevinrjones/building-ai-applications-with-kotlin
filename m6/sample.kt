import java.util.concurrent.Executors

fun fetchAll(urls: List<String>): List<String> {
    val results = mutableListOf<String>()
    val executor = Executors.newFixedThreadPool(10)
    for (url in urls) {
        executor.submit {
            val body = java.net.URL(url).readText()
            results.add(body) // unsynchronized access from multiple threads
        }
    }
    executor.shutdown()
    return results // returned before tasks finish
}

fun parseCount(input: String): Int {
    return input.toInt() // throws on non-numeric input
}
