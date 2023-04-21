import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        var greetingText by remember { mutableStateOf("Hello, World!") }
        var showImage by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                greetingText = "Hello, ${getPlatformName()}"
                showImage = !showImage
            }) {
                SDTextView(SDText(text = greetingText))
            }
            AnimatedVisibility(showImage) {
                Image(
                    painterResource("compose-multiplatform.xml"),
                    null
                )
            }
        }
    }
}

expect fun getPlatformName(): String


private const val DEFAULT_WEIGHT = "400"
private const val DEFAULT_SIZE = 16

@Composable
fun SDTextView(
    textModel: SDText,
    modifier: Modifier = Modifier,
    scope: Any? = null
) {
    Text(
        style = textModel.getTextStyle(),
        text = textModel.text,
        modifier = modifier.modifyIfNotNull(textModel.modifier) {
            toModifier(it, scope)
        }.semantics { if (textModel.isHeading()) heading() }
    )
}

data class SDText(
    val type: SDItemType = SDItemType.TEXT,
    val text: String,
    val size: Int? = null,
    val style: FontType? = null,
    val color: String? = null,
    val decoration: List<DecorationType>? = null,
    val weight: String? = null,
    val isHeading: Boolean? = null,
    val textAlignment: SDAlignment = SDAlignment.START,
    override val action: Action? = null,
    override val modifier: SDModifier? = null
) : SDBase() {

    fun getTextStyle() =
        TextStyle(
            fontWeight = fontWeight(),
            fontSize = getTextSize().sp,
            fontStyle = getStyle(),
            color = getTextColor(),
            textDecoration = textDecoration(),
            textAlign = textAlignment.toTextAlignment()
        )

    private fun getTextSize() = size ?: DEFAULT_SIZE

    private fun getStyle() = style?.fontStyle ?: FontType.NORMAL.fontStyle

    fun getTextColor() = Color.Black

    private fun textDecoration() =
        decoration?.let { decoration -> TextDecoration.combine(decoration.map { it.textDecoration }) }

    private fun fontWeight() = FontWeight(weight?.toInt() ?: DEFAULT_WEIGHT.toInt())

    fun isHeading() = isHeading ?: false
}

abstract class SDBase(open val id: String ="foo") {

    abstract val action: Action?
    abstract val modifier: SDModifier?

    fun getActionId(): String? = when (action) {
        is Action.DeeplinkAction -> (action as Action.DeeplinkAction).value.uri
        is Action.BrowserAction -> (action as Action.BrowserAction).value.uri
        else -> null
    }
}


enum class DecorationType(val textDecoration: TextDecoration) {
    STRIKETHROUGH(TextDecoration.LineThrough),
    UNDERLINE(TextDecoration.Underline),
    UNKNOWN(TextDecoration.None)
}

enum class FontType(val fontStyle: FontStyle) {
    NORMAL(FontStyle.Normal),
    ITALIC(FontStyle.Italic),
    UNKNOWN(FontStyle.Normal)
}

fun <T> Modifier.modifyIfNotNull(modifier: T?, modify: Modifier.(T) -> Modifier): Modifier =
    modifier?.run {
        modify(this)
    } ?: this

fun Modifier.toModifier(serverModifier: SDModifier?, scope: Any?): Modifier {
    return modifyIfNotNull(serverModifier?.adaText) {
        clearAndSetSemantics {
            contentDescription = it
        }
    }
        .modifyIfNotNull(serverModifier?.weight) {
            weightWithScope(
                scope,
                it
            )
        }
        .modifyIfNotNull(serverModifier?.weightWithoutFill) {
            weightWithScope(
                scope,
                it,
                false
            )
        }
        .modifyIfNotNull(serverModifier?.width) {
            width(
                Dp(
                    it + (
                            serverModifier?.paddingStart
                                ?: 0f
                            ) + (serverModifier?.paddingEnd ?: 0f)
                )
            )
        }
        .modifyIfNotNull(serverModifier?.height) {
            height(
                Dp(
                    it + (
                            serverModifier?.paddingTop
                                ?: 0f
                            ) + (serverModifier?.paddingBottom ?: 0f)
                )
            )
        }
        .modifyIfNotNull(serverModifier?.aspectRatio) {
            aspectRatio(it)
        }
        .modifyIfNotNull(serverModifier?.cornerRadius) { clip(RoundedCornerShape(Dp(it))) }
        .modifyIfNotNull(serverModifier?.paddingStart) { padding(start = Dp(it)) }
        .modifyIfNotNull(serverModifier?.paddingEnd) { padding(end = Dp(it)) }
        .modifyIfNotNull(serverModifier?.paddingTop) { padding(top = Dp(it)) }
        .modifyIfNotNull(serverModifier?.paddingBottom) { padding(bottom = Dp(it)) }
}

fun Modifier.weightWithScope(scope: Any?, weight: Float, fill: Boolean = true): Modifier {
    when (scope) {
        is ColumnScope -> {
            with(scope) {
                return weight(weight, fill)
            }
        }
        is RowScope -> {
            with(scope) {
                return weight(weight, fill)
            }
        }
    }
    return this
}

data class SDModifier(
    val paddingStart: Float? = null,
    val paddingEnd: Float? = null,
    val paddingTop: Float? = null,
    val paddingBottom: Float? = null,
    val width: Float? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val weightWithoutFill: Float? = null,
    val backgroundColor: String? = null,
    val cornerRadius: Float? = null,
    val adaText: String? = null,
    val aspectRatio: Float? = null
)
