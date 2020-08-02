package io.github.random_internet_cat.util

/**
 * Returns a [Set] containing all elements that appear more than once in this [Iterable].
 *
 * @param T the element type of this [Iterable]
 */
fun <T> Iterable<T>.repeatingElements(): Set<T> {
    val alreadySeen = mutableSetOf<T>()
    val duplicates = mutableSetOf<T>()

    for (element in this) {
        if (alreadySeen.contains(element)) {
            duplicates += element
        } else {
            alreadySeen += element
        }
    }

    return duplicates
}

private inline fun <T, K> Iterable<T>.checkDistinctByImpl(
    selector: (T) -> K,
    onRepeat: (item: T, key: K) -> Unit
): Set<K> {
    val alreadySeenKeys = mutableSetOf<K>()

    for (item in this) {
        val key = selector(item)

        if (alreadySeenKeys.contains(key)) {
            onRepeat(item, key)
        }

        alreadySeenKeys += key
    }

    return alreadySeenKeys
}

private inline fun <T> Iterable<T>.checkDistinctImpl(onRepeat: (item: T) -> Unit): Set<T> {
    return checkDistinctByImpl(
        selector = { it },
        onRepeat = { item, _ -> onRepeat(item) }
    )
}

/**
 * Returns a [Set] containing the same elements as this [Collection], if this [Collection] contains no repeating
 * elements, otherwise throws [IllegalArgumentException].
 */
fun <T> Iterable<T>.toSetCheckingDistinct(): Set<T> {
    return checkDistinctImpl(onRepeat = {
        throw IllegalArgumentException("Expected all elements to be distinct, but found repeating element: $it")
    })
}

/**
 * Returns `true` if this [Iterable] does not contain any repeating elements, and `false` otherwise.
 *
 * @param T the element type of this [Iterable]
 */
fun <T> Iterable<T>.allAreDistinct(): Boolean {
    checkDistinctImpl(onRepeat = { return false })

    return true
}

/**
 * Throws an [IllegalArgumentException] if this [Iterable] has any elements that appear more than once.
 *
 * @param T the element type of this [Iterable]
 */
fun <T> Iterable<T>.requireAllAreDistinct() {
    // This will do the distinct checking for us, so we can just throw away the result
    toSetCheckingDistinct()
}

/**
 * Returns `true` if this [Iterable] has no elements such that results of [selector] are the same, and `false`
 * otherwise.
 *
 * @param T the element type of this [Iterable]
 * @param K the result type of [selector]
 * @param selector the function to map elements to keys
 */
fun <T, K> Iterable<T>.allAreDistinctBy(selector: (T) -> K): Boolean {
    checkDistinctByImpl(
        selector = selector,
        onRepeat = { _, _ -> return false }
    )

    return true
}

/**
 * Throws an [IllegalArgumentException] if this [Iterable] has two elements such that results of [selector] are the
 * same.
 *
 * @param T the element type of this [Iterable]
 * @param K the result type of [selector]
 * @param selector the function to map elements to keys
 */
fun <T, K> Iterable<T>.requireAllAreDistinctBy(selector: (T) -> K) {
    checkDistinctByImpl(
        selector = selector,
        onRepeat = { item, key ->
            throw IllegalArgumentException(
                "Expected all elements to be distinct, but found repeating key: $key (from element $item)"
            )
        }
    )
}

/**
 * Returns `true` if all elements in this [Collection] are equal, and `false` otherwise.
 *
 * In particular, returns `true` for an empty collection.
 *
 * @param T the element type of this collection
 */
fun <T> Collection<T>.allAreEqual(): Boolean {
    if (isEmpty()) return true
    if (size == 1) return true

    val value = first()

    for (element in this) {
        if (value != element) return false
    }

    return true
}

/**
 * Throws [IllegalArgumentException] if all elements in this [Collection] are not equal.
 *
 * In particular, does not throw for an empty collection.
 *
 * @param T the element type of this collection
 */
fun <T> Collection<T>.requireAllAreEqual() {
    require(allAreEqual()) {
        "Expected all elements in collection $this to be equal"
    }
}

/**
 * If this map contains the key [key], returns the value corresponding to [key], otherwise throws
 * an [IllegalStateException].
 *
 * The value type of the receiver is non-nullable in order to avoid confusion about what to do in the case of a null
 * value, which has caused some contention in the standard library.
 *
 * @param K the key type of this [Map]
 * @param V the non-nullable value type of this [Map]
 * @param key the key for which the corresponding value is to be retrieved
 */
fun <K, V : Any> Map<K, V>.getOrFail(key: K): V {
    return getOrElse(key) { error("Missing expected key in map: $key") }
}
