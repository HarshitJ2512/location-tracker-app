package com.example.locationtracker2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

class PersonView: View {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // Some colors for the face background, eyes and mouth.
    private var faceColor = Color.YELLOW
    private var eyesColor = Color.BLACK
    private var mouthColor = Color.BLACK
    private var borderColor = Color.BLACK
    // Face border width in pixels
    private var borderWidth = 4.0f
    // View size in pixels
    private var size = 600
    private val mouthPath=Path()

    constructor(context: Context,attrs:AttributeSet?):super(context,attrs){
//        invalidate()
        this.setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.i("from person view","on draw called")
        drawFaceBackground(canvas)
        drawEyes(canvas)
        drawMouth(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        size = Math.min(measuredWidth, measuredHeight)
// 2
        setMeasuredDimension(size, size)
    }

    private fun drawFaceBackground(canvas: Canvas?) {
        canvas ?: return
        paint.color = faceColor
        paint.style = Paint.Style.FILL

        // 2
        val radius = size / 2f

        // 3
        canvas.drawCircle(size / 2f, size / 2f, radius, paint)

        // 4
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth

        // 5
        canvas.drawCircle(size / 2f, size / 2f, radius - borderWidth / 2f, paint)
    }

    private fun drawEyes(canvas: Canvas?) {
        canvas ?: return
        paint.color = eyesColor
        paint.style = Paint.Style.FILL

// 2
        val leftEyeRect = RectF(size * 0.32f, size * 0.23f, size * 0.43f, size * 0.50f)

        canvas.drawOval(leftEyeRect, paint)

// 3
        val rightEyeRect = RectF(size * 0.57f, size * 0.23f, size * 0.68f, size * 0.50f)

        canvas.drawOval(rightEyeRect, paint)
    }

    private fun drawMouth(canvas: Canvas?) {
        canvas ?: return
        mouthPath.moveTo(size * 0.22f, size * 0.7f)
// 2
        mouthPath.quadTo(size * 0.50f, size * 0.80f, size * 0.78f, size * 0.70f)
// 3
        mouthPath.quadTo(size * 0.50f, size * 0.90f, size * 0.22f, size * 0.70f)
// 4
        paint.color = mouthColor
        paint.style = Paint.Style.FILL
// 5
        canvas.drawPath(mouthPath, paint)

    }
}