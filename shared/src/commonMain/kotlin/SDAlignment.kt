import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

enum class SDAlignment {
    CENTER,
    END,
    START;

    fun toVerticalAlignment(): Alignment.Vertical = when (this) {
        CENTER -> Alignment.CenterVertically
        END -> Alignment.Bottom
        else -> Alignment.Top
    }

    fun toHorizontalAlignment(): Alignment.Horizontal = when (this) {
        CENTER -> Alignment.CenterHorizontally
        END -> Alignment.End
        else -> Alignment.Start
    }

    fun toTextAlignment(): TextAlign = when (this) {
        CENTER -> TextAlign.Center
        END -> TextAlign.End
        else -> TextAlign.Start
    }
}
