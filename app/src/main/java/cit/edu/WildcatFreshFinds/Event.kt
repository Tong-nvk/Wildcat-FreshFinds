package cit.edu.WildcatFreshFinds

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * Helps prevent events (like showing a Toast) from firing again on configuration change.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}