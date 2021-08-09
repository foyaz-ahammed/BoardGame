package com.exam.board.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.exam.board.R
import com.exam.board.entities.State

class CircleView(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
    View(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    private var state: State = State.BLANK
    private val outLinePaint = Paint()
    private val fillPaint = Paint()

    init {
        outLinePaint.color = Color.GRAY
        outLinePaint.style = Paint.Style.STROKE
        outLinePaint.strokeWidth = context.resources.getDimension(R.dimen.outline_width)
        fillPaint.style = Paint.Style.FILL
    }

    fun updateView(newState: State) {
        state = newState
        if(state == State.PLAYER) {
            fillPaint.color = context.getColor(R.color.light_red)
        } else if(state == State.COMPUTER) {
            fillPaint.color = context.getColor(R.color.light_blue)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(canvas == null) return

        val circleWidth = width - paddingStart - paddingEnd

        if(state != State.BLANK) {
            canvas.drawCircle(paddingStart + circleWidth.toFloat()/2, paddingTop + circleWidth.toFloat()/2, circleWidth.toFloat()/2, fillPaint)
        }
        canvas.drawCircle(paddingStart + circleWidth.toFloat()/2, paddingTop + circleWidth.toFloat()/2, circleWidth.toFloat()/2, outLinePaint)
    }
}