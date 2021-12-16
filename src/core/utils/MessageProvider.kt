package core.utils

object MessageProvider {

    private var _hasError = false
    val hasError get() = _hasError
    val hasNoError get() = !_hasError

    fun critical(info: String, line: Int? = null) {
        error(info, line)
        throw Exception(info)
    }
    fun error(info: String, line: Int? = null) { print(MessageType.Error, info, line).also { _hasError = true } }
    fun warning(info: String, line: Int? = null) { print(MessageType.Warning, info, line) }
    fun info(info: String, line: Int? = null) { print(MessageType.Info, info, line) }

    fun clear() { _hasError = false }


    private fun print(type: MessageType, info: String, line: Int?) {
        print("[${type.name}]: $info")
        line?.let { print(" on line $it") }
        println()
    }

    private enum class MessageType {
        Error,
        Warning,
        Info
    }

}