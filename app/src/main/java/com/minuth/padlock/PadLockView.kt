package com.minuth.padlock

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.contains

/**
 * Created by Minuth Prom.
 * Email: minuthprom321@gmail.com
 *​Crated date: 8/12/2020, 2:46 PM
 */
class PadLockView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet){

    companion object{
        private const val PERCENT_PIN_BODY_TOP = 0.3846153846153846f
        private const val PERCENT_PIN_BODY_HEIGHT = 1.3f

        private const val PERCENT_LOCK_STATUS_BORDER_WIDTH = 0.0769230769230769f
        private const val PERCENT_LOCK_STATUS_LEFT = 0.1076923076923077f
        private const val PERCENT_LOCK_STATUS_RIGHT = 0.6615384615384615f
        private const val PERCENT_LOCK_STATUS_BOTTOM = PERCENT_PIN_BODY_TOP

        private const val PERCENT_START_TOP_KEY_NUM = 0.4076923076923077f
        private const val PERCENT_START_LEFT_KEY_NUM = 0.0615384615384615f
        private const val PERCENT_KEY_NUM_WIDTH = 0.1846153846153846f
        private const val PERCENT_KEY_NUM_HEIGHT = 0.1153846153846154f
        private const val PERCENT_KEY_NUM_MARGIN_TOP = 0.0384615384615385f
        private const val PERCENT_KEY_NUM_MARGIN_LEFT = 0.0461538461538462f

        private const val PERCENT_LABEL_FONT_SIZE = 0.0615384615384615f

        private const val DEFAULT_BODY_COLOR = Color.RED
        private const val DEFAULT_SHACKLE_COLOR = Color.BLACK
        private const val DEFAULT_KEY_PAD_COLOR = Color.BLACK
        private const val DEFAULT_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_ON_KEY_PAD_SELECTED_COLOR = Color.YELLOW
        private const val DEFAULT_WIDTH = 500 // default width 500 and height = 130%(1.3) of width so height = 1.3 * 500 = 650
        private const val DEFAULT_UNLOCK_STATUS = false
    }
    private var size = DEFAULT_WIDTH * PERCENT_PIN_BODY_HEIGHT // size is represent for height
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var bodyColor: Int
    private var shackleColor: Int
    private var keyPadColor: Int
    private var textColor: Int
    private var onSelectedKeyPadColor: Int

    private var touchPoint: PointF? = null
    private var keyPadIsClick = false
    private var keyPadClickedIsValid = false
    private var selectedKeyPad: KeyPads? =null
    private var inputPin: String = ""

    var eventListener: EventListener? = null

    var unlockStatus = false
        set(value) {
            field = value
            invalidate()
        }


    init {
        val typedArray = context.theme.obtainStyledAttributes(attributeSet,R.styleable.PadLockView,0,0)
        bodyColor = typedArray.getColor(R.styleable.PadLockView_bodyColor, DEFAULT_BODY_COLOR)
        shackleColor = typedArray.getColor(R.styleable.PadLockView_shackleColor, DEFAULT_SHACKLE_COLOR)
        keyPadColor = typedArray.getColor(R.styleable.PadLockView_keyPadColor, DEFAULT_KEY_PAD_COLOR)
        textColor = typedArray.getColor(R.styleable.PadLockView_textColor, DEFAULT_TEXT_COLOR)
        unlockStatus = typedArray.getBoolean(R.styleable.PadLockView_unlockStatus, DEFAULT_UNLOCK_STATUS)
        onSelectedKeyPadColor = typedArray.getColor(R.styleable.PadLockView_onKeyPadSelectedColor, DEFAULT_ON_KEY_PAD_SELECTED_COLOR)
        typedArray.recycle()
        onKeyPadTouch()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPinBody(canvas)
        drawShackle(canvas)
        drawKeyNum(canvas)
    }

    private fun drawPinBody(canvas: Canvas){
        val rect = RectF(0f,size * PERCENT_PIN_BODY_TOP,size * PERCENT_PIN_BODY_HEIGHT, size.toFloat())
        paint.apply {
            style = Paint.Style.FILL
            color = bodyColor
        }
        canvas.drawRoundRect(rect,20f,20f,paint)
    }
    private fun drawShackle(canvas: Canvas){
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = size * PERCENT_LOCK_STATUS_BORDER_WIDTH
            color = shackleColor
        }
        val rect = RectF(size * PERCENT_LOCK_STATUS_LEFT,0f + (paint.strokeWidth /2f),size * PERCENT_LOCK_STATUS_RIGHT,size * PERCENT_LOCK_STATUS_BOTTOM)
        val keyUnlockedValue = if(unlockStatus) 0.8f else 1f
        val path = Path().apply {
            moveTo(size * PERCENT_LOCK_STATUS_LEFT,size * PERCENT_LOCK_STATUS_BOTTOM * keyUnlockedValue)
            arcTo(rect,180f,180f,false)
            lineTo(size* PERCENT_LOCK_STATUS_RIGHT,size * PERCENT_LOCK_STATUS_BOTTOM)
        }
        canvas.drawPath(path,paint)
    }

    private fun drawKeyNum(canvas: Canvas){
        paint.apply {
            color = keyPadColor
            style = Paint.Style.FILL
        }
        val paintText = TextPaint().apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            textSize = size * PERCENT_LABEL_FONT_SIZE
        }

        val keyNums = arrayListOf(
            arrayListOf(KeyPads.KEY_1, KeyPads.KEY_2,KeyPads.KEY_3),
            arrayListOf(KeyPads.KEY_4,KeyPads.KEY_5,KeyPads.KEY_6),
            arrayListOf(KeyPads.KEY_7,KeyPads.KEY_8,KeyPads.KEY_9),
            arrayListOf(KeyPads.KEY_CLEAR,KeyPads.KEY_0,KeyPads.KEY_OK)
        )
        val width = size * PERCENT_KEY_NUM_WIDTH
        val height = size * PERCENT_KEY_NUM_HEIGHT
        val marginLeft = size * PERCENT_KEY_NUM_MARGIN_LEFT
        val marginTop = size * PERCENT_KEY_NUM_MARGIN_TOP
        var top = size * PERCENT_START_TOP_KEY_NUM
        var bottom = height+ top
        for (row in keyNums){
            var left = size * PERCENT_START_LEFT_KEY_NUM
            var right = width + left
            for (key in row){
                val rect = RectF(left,top,right,bottom)
                val textBound = Rect()
                paintText.getTextBounds(key.text,0,key.text.length,textBound)

                if(touchPoint != null ){
                    if(rect.contains(touchPoint!!) && keyPadIsClick){
                        paint.color = onSelectedKeyPadColor
                        keyPadClickedIsValid = true
                        if(key != KeyPads.KEY_CLEAR && key != KeyPads.KEY_OK){
                            selectedKeyPad = key
                        }

                        when(key){
                            KeyPads.KEY_OK ->{
                                eventListener?.onConfirmClicked(inputPin)
                                clearPin()

                            }
                            KeyPads.KEY_CLEAR -> {
                                eventListener?.onClearClicked()
                                clearPin()
                            }

                        }

                    }
                    else{
                        paint.color = keyPadColor
                    }
                }
                canvas.drawRect(rect, paint)
                canvas.drawText(key.text,rect.right - (width /2),rect.bottom - (height /2) + (textBound.height()/2), paintText)
                left = right + marginLeft
                right = left + width
            }
            top = bottom + marginTop
            bottom = top + height
        }

    }

    fun getPinValue(): String{
        return inputPin
    }

    private fun clearPin(){
        selectedKeyPad = null
        inputPin = ""
    }

    private fun onKeyPadTouch() {
        setOnTouchListener { v, event ->
            touchPoint = PointF(event.x, event.y)
            when(event.action){
                MotionEvent.ACTION_DOWN -> keyPadIsClick = true
                MotionEvent.ACTION_UP ->{
                    if(keyPadClickedIsValid && selectedKeyPad != null){
                        keyPadClickedIsValid = false
                        inputPin += selectedKeyPad?.text
                    }
                    keyPadIsClick = false
                }
            }
            invalidate()
            true
        }
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        size = measuredWidth * PERCENT_PIN_BODY_HEIGHT
        setMeasuredDimension(measuredWidth,size.toInt())
    }

    enum class KeyPads(val text: String){
        KEY_0("0"),
        KEY_1("1"),
        KEY_2("2"),
        KEY_3("3"),
        KEY_4("4"),
        KEY_5("5"),
        KEY_6("6"),
        KEY_7("7"),
        KEY_8("8"),
        KEY_9("9"),
        KEY_CLEAR("X"),
        KEY_OK("OK")
    }

    interface EventListener{
        fun onConfirmClicked(pinValue: String)
        fun onClearClicked()
    }

}