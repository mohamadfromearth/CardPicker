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
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
    var rotation: Float = 0f,
    var backImage: Bitmap? = null
)

data class ChosenCardData(
    var xPercent: Float,
    var yPercent: Float,
    var offset: Offset,
    var rotation: Float,
    var scale: Float,
    var backImage: Bitmap? = null,
    var type: ChosenCardType = ChosenCardType.NONE
)

enum class ChosenCardType {
    NONE,
    SELECTED,
    DISABLED
}


class MainActivity : ComponentActivity() {


    companion object {
        val CENTER_ANGLE = Math.toRadians(-101.0)
        const val CARD_DISTANCE_ANGLE = 0.13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            var choseCards by remember {
                mutableStateOf(listOf<ChosenCardData>())
            }

            val image = ContextCompat.getDrawable(this, R.drawable.card2)!!.toBitmap()
            val backImage =
                ContextCompat.getDrawable(this, R.drawable.baseline_sd_card_24)!!.toBitmap()

            var cardDataList by remember {
                mutableStateOf(
                    listOf(
                        Card(1f, backImage = backImage),
                        Card(2f, backImage = backImage),
                        Card(3f, backImage = backImage),
                        Card(4f, backImage = backImage),
                        Card(5f, backImage = backImage),
                        Card(6f, backImage = backImage),
                        Card(7f, backImage = backImage),
                        Card(8f, backImage = backImage),
                        Card(9f, backImage = backImage),
                        Card(10f, backImage = backImage),
                        Card(11f, backImage = backImage),
                        Card(12f, backImage = backImage),
                        Card(13f, backImage = backImage),
                        Card(14f, backImage = backImage),
                        Card(15f, backImage = backImage),
                        Card(16f, backImage = backImage),
                        Card(17f, backImage = backImage),
                        Card(18f, backImage = backImage),
                        Card(19f, backImage = backImage),
                        Card(20f, backImage = backImage),
                        Card(21f, backImage = backImage),
                        Card(22f, backImage = backImage),
                    )
                )
            }






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
                                0.5f,
                                0.2f,
                                Offset(0f, 0f),
                                -76f,
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

    var chosenCardsCount by remember {
        mutableStateOf(0)
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


    var oldCircleCenter by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var newCircleCenter by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    var circleCenter by remember {
        mutableStateOf(Offset(0f, 0f))
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


            circleCenter = Offset(
                lerp(oldCircleCenter.x, newCircleCenter.x, animationValue),
                lerp(oldCircleCenter.y, newCircleCenter.y, animationValue)
            )



            if (animationValue == 1.0f) {
                playTime = 0
                isPlaying = false
                oldCardList = cards
                oldChosenCardList = chosenCards
                if (chosenCards.isNotEmpty()) {
                    Log.d("ChosenCard", "chosen x ${chosenCards[0].offset.x} ")
                }
                oldCircleCenter = newCircleCenter
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

    var canvasHeight by remember {
        mutableStateOf(0f)
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


                var selectedChosedCardIndex = -1

                for (i in chosenCards.indices) {
                    val it = chosenCards[i]
                    val angleInRad = Math
                        .toRadians(it.rotation.toDouble())
                        .toFloat()
                    val x2 = ((it.scale * 118.dp.toPx()) + it.offset.x)
                    val y2 = ((it.scale * 186.dp.toPx()) + it.offset.y)


                    val cornerX1 = it.offset.x
                    val cornerX2 =
                        ((x2 - it.offset.x) * cos(angleInRad) - (it.offset.y - it.offset.y) * sin(
                            angleInRad
                        )) + it.offset.x

                    val cornerX3 =
                        ((it.offset.x - it.offset.x) * cos(angleInRad) - (y2 - it.offset.y) * sin(
                            angleInRad
                        )) + it.offset.x

                    val cornerX4 = ((x2 - it.offset.x) * cos(angleInRad) - (y2 - it.offset.y) * sin(
                        angleInRad
                    )) + it.offset.x

                    val cornerY1 = it.offset.y
                    val cornerY2 =
                        ((x2 - it.offset.x) * sin(angleInRad) + (it.offset.y - it.offset.y) * cos(
                            angleInRad
                        )) + it.offset.y

                    val cornerY3 =
                        ((it.offset.x - it.offset.x) * sin(angleInRad) + (y2 - it.offset.y) * cos(
                            angleInRad
                        )) + it.offset.y

                    val cornerY4 = ((x2 - it.offset.x) * sin(angleInRad) + (y2 - it.offset.y) * cos(
                        angleInRad
                    )) + it.offset.y

                    val bigX = listOf(cornerX1, cornerX2, cornerX3, cornerX4).maxOf { it }
                    val smallX = listOf(cornerX1, cornerX2, cornerX3, cornerX4).minOf { it }
                    val bigY = listOf(cornerY1, cornerY2, cornerY3, cornerY4).maxOf { it }
                    val smallY = listOf(cornerY1, cornerY2, cornerY3, cornerY4).minOf { it }

                    if (
                        offset.x in smallX..bigX
                        &&
                        offset.y in smallY..bigY
                    ) {
                        Log.d("ChosenCardClick", "Chosen card clicked!")
                        selectedChosedCardIndex = i
                        break;
                    }
                }

                if (selectedChosedCardIndex != -1) {
                    when (chosenCards[selectedChosedCardIndex].type) {
                        ChosenCardType.NONE -> {
                            newChosenCardList.forEachIndexed { index, chosenCard ->
                                if (selectedChosedCardIndex == index) {
                                    chosenCard.type = ChosenCardType.SELECTED
                                    oldChosenCardList[index].type = ChosenCardType.SELECTED
                                    chosenCards[index].type = ChosenCardType.SELECTED
                                } else {
                                    if (chosenCard.type == ChosenCardType.NONE) {
                                        chosenCard.type = ChosenCardType.DISABLED
                                        oldChosenCardList[index].type = ChosenCardType.DISABLED
                                        chosenCards[index].type = ChosenCardType.DISABLED
                                    }
                                }
                            }
                            newChosenCardList = newChosenCardList.toList()
                        }
                        ChosenCardType.SELECTED -> {

                        }
                        ChosenCardType.DISABLED -> Unit
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
                                        1f,
                                        backImage = card.backImage
                                    )
                                )
                            }

                        newChosenCardList = newChosenCardList
                            .toMutableList()
                            .apply {
                                add(chosenCardsPosData[chosenCardsCount].apply {
                                    backImage = card.backImage
                                })
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

                if (chosenCardsCount == chosenCardsPosData.size) {
                    newCircleCenter = Offset(0f, canvasHeight + 500.dp.toPx())
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
            if (chosenCardsCount < chosenCardsPosData.size) {
                circleCenter = Offset(width / 2f, height + radius.toPx() / 2f)
                oldCircleCenter = circleCenter
                canvasHeight = height.toDp().toPx()
            }
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

                    when (it.type) {
                        ChosenCardType.NONE -> drawBitmap(image, it.offset.x, it.offset.y, Paint())
                        ChosenCardType.SELECTED -> drawBitmap(
                            it.backImage!!,
                            it.offset.x,
                            it.offset.y,
                            Paint()
                        )
                        ChosenCardType.DISABLED -> drawBitmap(
                            image,
                            it.offset.x,
                            it.offset.y,
                            Paint().apply {
                                alpha = 50
                            })
                    }


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
        it.rotation = Math.toDegrees(angleInRad.toDouble()).toFloat() + 90

    }

}


private fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}





