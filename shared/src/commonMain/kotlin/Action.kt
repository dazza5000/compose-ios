

sealed class Action {
    data class DeeplinkAction(
        val type: ActionOpenType,
        val value: Deeplink
    ) : Action()

    data class BrowserAction(
        val type: ActionOpenType,
        val value: Browser
    ) : Action()
}

data class Deeplink(val uri: String)

data class Browser(
    val uri: String,
    val isThirdParty: Boolean = false
)
enum class ActionOpenType {
    BROWSER,
    DEEP_LINK,
    UNKNOWN
}
