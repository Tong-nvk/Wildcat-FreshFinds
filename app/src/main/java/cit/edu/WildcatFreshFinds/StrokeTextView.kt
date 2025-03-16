package cit.edu.WildcatFreshFinds

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

class StrokeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private val strokePaint = Paint()

    init {
        strokePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 100f // Adjust for thickness
            color = ContextCompat.getColor(context, R.color.maroon) // Stroke color
            textSize = this@StrokeTextView.textSize // Match text size
            typeface = this@StrokeTextView.typeface // Match font
            textAlign = Paint.Align.CENTER // Align center
        }
    }

    override fun onDraw(canvas: Canvas) {
        val text = text.toString()
        val x = (width / 2).toFloat()
        val y = (height / 2 - (strokePaint.descent() + strokePaint.ascent()) / 2)

        // Draw stroke text first
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 100f
        paint.color = ContextCompat.getColor(context, R.color.maroon)
        canvas.drawText(text, x, y, paint)

        // Draw actual text
        paint.style = Paint.Style.FILL
        paint.color = currentTextColor
        canvas.drawText(text, x, y, paint)
    }
}
