package com.example.cardpicker

import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.rotationMatrix
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import com.example.cardpicker.MainActivity.Companion.CARD_DISTANCE_ANGLE
import com.example.cardpicker.MainActivity.Companion.CENTER_ANGLE
import com.example.cardpicker.ui.theme.CardPickerTheme
import kotlin.math.*


data class Card(
    var index: Float,
    var isSelected: Boolean = false,
    var startX: Float = 0f,
    var endX: Float = 0f,
    var y: Float = 0f,
    var radius: Dp = 500.dp,
    var rotation: Float = 0f
)

data class ChosenCardData(
    var xPercent: Float,
    var yPercent: Float,
    var offset: Offset,
    var rotation: Float,
    var scale: Float
)


class MainActivity : ComponentActivity() {


    companion object {
        val CENTER_ANGLE = Math.toRadians(-101.0)
        const val CARD_DISTANCE_ANGLE = 0.13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var cardDataList by remember {
                mutableStateOf(
                    listOf(
                        Card(1f),
                        Card(2f),
                        Card(3f),
                        Card(4f),
                        Card(5f),
                        Card(6f),
                        Card(7f),
                        Card(8f),
                        Card(9f),
                        Card(10f),
                        Card(11f),
                        Card(12f),
                        Card(13f),
                        Card(14f),
                        Card(15f),
                        Card(16f),
                        Card(17f),
                        Card(18f),
                        Card(19f),
                        Card(20f),
                        Card(21f),
                        Card(22f),
                    )
                )
            }


            val image = ContextCompat.getDrawable(this, R.drawable.card2)!!.toBitmap()

            CardPickerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {


                    CardPicker(
                        modifier = Modifier.fillMaxSize(),
                        image = image,
                        cardList = cardDataList,
                        chosenCardsPosData = listOf(
                            ChosenCardData(
                                0.9f,
                                0.2f,
                                Offset(0f, 0f),
                                90f,
                                0.6f
                            ),
                            ChosenCardData(
                                0.2f,
                                0.2f,
                                Offset(0f, 0f),
                                0f,
                                0.6f
                            )
                        ),
                        radius = 500.dp,
                    )

                }
            }
        }
    }
}


@Composable
fun CardPicker(
    modifier: Modifier,
    image: Bitmap,
    cardList: List<Card>,
    chosenCardsPosData: List<ChosenCardData>,
    radius: Dp,

    ) {

    var maxAngle: Double
    var minAngle: Double


    var oldCardList by remember {
        mutableStateOf(cardList.map { it.copy(index = 1f) }.toList())

    }
    var newCardList by remember {
        mutableStateOf(cardList.map { it.copy() }.toList())
    }

    var cards by remember {
        mutableStateOf(listOf<Card>())
    }

    var oldChosenCardList by remember {
        mutableStateOf(listOf<ChosenCardData>())
    }

    var newChosenCardList by remember {
        mutableStateOf(listOf<ChosenCardData>())
    }

    var chosenCards by remember {
        mutableStateOf(listOf<ChosenCardData>())
    }

    var chosenCardsCount = remember {
        0
    }


    var anim = remember {
        TargetBasedAnimation(
            animationSpec = tween(500),
            typeConverter = Float.VectorConverter,
            initialValue = 0f,
            targetValue = 1f
        )
    }
    var animateKey by remember {
        mutableStateOf(true)
    }
    var playTime by remember { mutableStateOf(0L) }
    var isPlaying by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(animateKey) {
        val startTime = withFrameNanos { it }


        do {

            Log.d("Animating", "Animating ")

            playTime = withFrameNanos { it } - startTime
            val animationValue = anim.getValueFromNanos(playTime)

            cards = oldCardList.mapIndexed { index, card ->
                card.copy(
                    index = lerp(
                        card.index,
                        newCardList[index].index,
                        animationValue
                    ),
                    radius = lerp(
                        card.radius.value,
                        newCardList[index].radius.value, animationValue
                    ).dp,
                    isSelected = newCardList[index].isSelected
                )
            }

            chosenCards = oldChosenCardList.mapIndexed { index, chosenCardData ->
                chosenCardData.copy(
                    offset = Offset(
                        lerp(
                            chosenCardData.offset.x,
                            newChosenCardList[index].offset.x,
                            animationValue
                        ),
                        lerp(
                            chosenCardData.offset.y,
                            newChosenCardList[index].offset.y,
                            animationValue
                        ),
                    ),
                    xPercent = newChosenCardList[index].xPercent,
                    yPercent = newChosenCardList[index].yPercent,
                    rotation = lerp(
                        chosenCardData.rotation,
                        newChosenCardList[index].rotation,
                        animationValue
                    ),
                    scale = lerp(
                        chosenCardData.scale,
                        newChosenCardList[index].scale,
                        animationValue
                    )

                )
            }








            if (animationValue == 1.0f) {
                playTime = 0
                isPlaying = false
                oldCardList = cards
                oldChosenCardList = chosenCards
                if (chosenCards.isNotEmpty()) {
                    Log.d("ChosenCard", "chosen x ${chosenCards[0].offset.x} ")

                }
            }
            Log.d("Animation", "AnimValue:$animationValue ")
        } while (isPlaying)
    }


    var dragAngle by remember {
        mutableStateOf(0f)
    }

    var oldAngle by remember {
        mutableStateOf(dragAngle)
    }


    var dragStartedAngle by remember {
        mutableStateOf(0f)
    }


    var circleCenter = remember {
        Offset(0f, 0f)
    }

    Canvas(modifier = modifier
        .pointerInput(true) {

            detectDragGestures(
                onDragStart = { offset ->
                    dragStartedAngle = -atan2(
                        circleCenter.x - offset.x,
                        circleCenter.y - offset.y
                    )
                },
                onDragEnd = {
                    oldAngle = dragAngle
                }
            ) { change, _ ->
                val touchAngle = -atan2(
                    circleCenter.x - change.position.x,
                    circleCenter.y - change.position.y
                )

                val newAngle = oldAngle + (touchAngle - dragStartedAngle)
                dragAngle = newAngle.coerceIn(
                    maximumValue = abs(CARD_DISTANCE_ANGLE * cards.size / 2).toFloat(),
                    minimumValue = -1 * abs(CARD_DISTANCE_ANGLE * cards.size / 2f).toFloat()
                )
            }


        }
        .pointerInput(true) {
            detectTapGestures { offset ->


                // ======== DETECT CHOSEN CARD CLICK ========//


                for (i in chosenCards.indices) {
                    val it = chosenCards[i]
                    val angleInRad = Math
                        .toRadians(it.rotation.toDouble())
                        .toFloat()
                    val x2 = ((it.scale * 118.dp.toPx()) + it.offset.x)
                    val y2 = ((it.scale * 186.dp.toPx()) + it.offset.y)
                    val startX = it.offset.x
                    val endX = ((x2 - it.offset.x) * cos(angleInRad) - (y2 - it.offset.y) * sin(angleInRad)) + it.offset.x
                    val startY = it.offset.y
                    val endY = ((x2 - it.offset.x) * sin(angleInRad) + (y2 - it.offset.y) * cos(angleInRad)) + it.offset.y

                    Log.d(
                        "Rotation Debug",
                        "ClickOffset: $offset \n CardOffset: ${it.offset} \n" +
                                "X2 Y2 : $x2 $y2 \n" +
                                "startX StartY $startX $startY \n" +
                                "endX endY $endX $endY \n" +
                                "Rotation: ${it.rotation}"
                    )

                    val bigX = if (startX>endX) startX else endX
                    val smallX = if (startX<endX) startX else endX
                    val bigY = if (startY>endY) startY else endY
                    val smallY = if (startY<endY) startY else endY

                    if (
                        offset.x in smallX..bigX
                        &&
                        offset.y in smallY..bigY
                    ) {
                        Log.d("ChosenCardClick", "Chosen card clicked!")
                        break;

                    }
                }


                // ======== DETECT CHOSEN CARD CLICK ========//


                // ============ DETECT CARD CLICK ============= //

                val tempCards = cards
                    .map { it.copy() }
                    .toMutableList()

                var selectedIndex = -1
                var chosenIndex = -1

                for (i in tempCards.indices) {
                    if (offset.x <= tempCards[i].endX && offset.x >= tempCards[i].startX) {
                        Log.d("Removing", "test ")

                        if (tempCards[i].isSelected) {
                            Log.d("Removing", "selected ")
                            chosenIndex = i
                        } else {
                            selectedIndex = i
                            tempCards[i].isSelected = true
                            oldCardList[i].isSelected = true
                            Log.d("Selecting", "selected ")
                            tempCards[i].radius = 550.dp
                            break
                        }
                    }
                }

                for (i in tempCards.indices) {
                    if (i != selectedIndex) {
                        tempCards[i].isSelected = false
                        oldCardList[i].isSelected = false
                        tempCards[i].radius = 500.dp
                    }
                }


                if (chosenIndex != -1) {

                    val card = tempCards[chosenIndex]

                    if (chosenCardsCount < chosenCardsPosData.size) {
                        oldChosenCardList = oldChosenCardList
                            .toMutableList()
                            .apply {
                                add(
                                    ChosenCardData(
                                        chosenCardsPosData[chosenCardsCount].xPercent,
                                        chosenCardsPosData[chosenCardsCount].yPercent,
                                        Offset(card.startX, card.y),
                                        card.rotation,
                                        1f
                                    )
                                )
                            }

                        newChosenCardList = newChosenCardList
                            .toMutableList()
                            .apply {
                                add(chosenCardsPosData[chosenCardsCount])
                            }
                        chosenCardsCount++
                        if (newChosenCardList.isNotEmpty()) Log.d(
                            "chosen::",
                            newChosenCardList.toString()
                        )


                        tempCards.removeAt(chosenIndex)
                        oldCardList = oldCardList
                            .toMutableList()
                            .apply {
                                removeAt(chosenIndex)
                            }
                        for (i in chosenIndex until tempCards.size) {
                            tempCards[i].index = tempCards[i].index - 1
                        }


                    }

                }




                newCardList = tempCards

                Log.d("onTap", "OnTap")
                isPlaying = true


                animateKey = animateKey.not()

                // ============ DETECT CARD CLICK ============= //


            }
        }
    ) {

        drawContext.canvas.nativeCanvas.apply {
            circleCenter = Offset(width / 2f, height + radius.toPx() / 2f)
        }
        maxAngle = CENTER_ANGLE + (cards.size / 2f * CARD_DISTANCE_ANGLE)
        minAngle = CENTER_ANGLE - (cards.size / 2f * CARD_DISTANCE_ANGLE)


        setChosenCardsOffset(this, chosenCardsPosData)
        initCardsXRange(
            cards,
            circleCenter,
            dragAngle,
            radius.toPx(),
            maxAngle,
            minAngle
        )


        drawCardList(
            drawScope = this,
            cardList = cards,
            image = image,
            radius = radius.toPx(),
            circleCenter = circleCenter,
            dragAngle = dragAngle,
            maxAngle = maxAngle,
            minAngle = minAngle
        )

        drawChosenCardsRect(this, chosenCardsPosData, 118.dp.toPx(), 186.dp.toPx(), image)

        drawChosenCards(
            this,
            chosenCards,
            image,
        )
    }


}


private fun drawCardList(
    drawScope: DrawScope,
    cardList: List<Card>,
    image: Bitmap,
    circleCenter: Offset,
    radius: Float,
    dragAngle: Float,
    maxAngle: Double,
    minAngle: Double
) {


    drawScope.apply {
        drawContext.canvas.nativeCanvas.apply {


            cardList.forEach {

                val angleInRad =
                    lerp(
                        minAngle.toFloat(),
                        maxAngle.toFloat(),
                        it.index / cardList.size.toFloat()
                    ) + dragAngle
                Log.d("centerAngle", angleInRad.toFloat().toString())
                val cardX = it.radius.toPx() * cos(angleInRad) + circleCenter.x
                val cardY = it.radius.toPx() * sin(angleInRad) + circleCenter.y
                withRotation(
                    Math.toDegrees(angleInRad.toDouble()).toFloat() + 97,
                    cardX,
                    cardY
                ) {
                    drawBitmap(image, cardX.toFloat(), cardY.toFloat(), Paint())
                }

            }
        }
    }
}

private fun drawChosenCardsRect(
    drawScope: DrawScope,
    chosenCards: List<ChosenCardData>,
    rectWidth: Float,
    rectHeight: Float,
    image: Bitmap
) {

    drawScope.drawContext.canvas.nativeCanvas.apply {

        chosenCards.forEach {
            val x = it.offset.x

            val y = it.offset.y


            withScale(it.scale, it.scale, it.offset.x, it.offset.y) {
                withRotation(it.rotation, it.offset.x, it.offset.y) {

                    drawRect(x, y, x + rectWidth, y + rectHeight, Paint().apply {
                        style = Paint.Style.STROKE
                    })
                }

            }


        }


    }

}

private fun drawChosenCards(
    drawScope: DrawScope,
    chosenCards: List<ChosenCardData>,
    image: Bitmap,
) {
    drawScope.drawContext.canvas.nativeCanvas.apply {
        chosenCards.forEach {
            withScale(it.scale, it.scale, it.offset.x, it.offset.y) {
                withRotation(it.rotation, it.offset.x, it.offset.y) {

                    drawBitmap(image, it.offset.x, it.offset.y, Paint())

                }
            }


        }
    }

}

private fun setChosenCardsOffset(drawScope: DrawScope, chosenCardList: List<ChosenCardData>) {
    drawScope.drawContext.canvas.nativeCanvas.apply {
        chosenCardList.forEach {
            it.offset = Offset(
                lerp(0f, width.toFloat(), it.xPercent),
                lerp(0f, height.toFloat(), it.yPercent),

                )
        }
    }
}

private fun initCardsXRange(
    cardList: List<Card>,

    circleCenter: Offset,
    dragAngle: Float,
    radius: Float,
    maxAngle: Double,
    minAngle: Double
) {
    cardList.forEach {
        val angleInRad =
            lerp(
                minAngle.toFloat(),
                maxAngle.toFloat(),
                it.index / cardList.size.toFloat()
            ) + dragAngle
        Log.d("centerAngle", angleInRad.toFloat().toString())
        val startX = radius * cos(angleInRad) + circleCenter.x
        val endX =
            radius * cos(angleInRad + CARD_DISTANCE_ANGLE.toFloat()) + circleCenter.x
        val y = radius * sin(angleInRad) + circleCenter.y

        Log.d("StartX", "startX ->$startX")
        it.startX = startX
        it.endX = endX
        it.y = y
        it.rotation = Math.toDegrees(angleInRad.toDouble()).toFloat()

    }

}


private fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}





