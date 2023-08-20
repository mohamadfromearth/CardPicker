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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withRotation
import com.example.cardpicker.MainActivity.Companion.CARD_DISTANCE_ANGLE
import com.example.cardpicker.MainActivity.Companion.CENTER_ANGLE
import com.example.cardpicker.ui.theme.CardPickerTheme
import kotlin.math.*


data class Card(
    var index: Float,
    var isSelected: Boolean = false,
    var startX: Float = 0f,
    var endX: Float = 0f,
    var radius: Dp = 500.dp,
)

data class CardPos(
    var startX: Float,
    var endX: Float
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

            var chosenCardPosList by remember {
                mutableStateOf(
                    listOf(
                        Offset(100f, 100f),
                        Offset(200f, 100f),
                        Offset(300f, 100f)
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
                        chosenCardPosList = chosenCardPosList,
                        radius = 500.dp,
                        selectedRadius = 500.dp
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
    chosenCardPosList: List<Offset>,
    radius: Dp,
    selectedRadius: Dp
) {

    var maxAngle = CENTER_ANGLE + (cardList.size / 2f * CARD_DISTANCE_ANGLE)
    var minAngle = CENTER_ANGLE - (cardList.size / 2f * CARD_DISTANCE_ANGLE)


    var startEndXPos = ArrayList<CardPos>()


    var oldCardList by remember {
        mutableStateOf(cardList.map { it.copy(index = 1f) }.toList())

    }
    var newCardList by remember {
        mutableStateOf(cardList.map { it.copy() }.toList())
    }

    var cards by remember {
        mutableStateOf(listOf<Card>())
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





            if (animationValue == 1.0f) {
                playTime = 0
                isPlaying = false
                oldCardList = cards
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
                    maximumValue = abs(CARD_DISTANCE_ANGLE * cardList.size / 2).toFloat(),
                    minimumValue = -1 * abs(CARD_DISTANCE_ANGLE * cardList.size / 2f).toFloat()
                )
            }


        }
        .pointerInput(true) {
            detectTapGestures { offset ->

//
//                val tempCards = cards
//                    .map { it.copy() }
//                    .toList()
//                tempCards.forEach {
//                    Log.d(
//                        "Ontapstartx",
//                        "OnTapStartx:${it.startX} endx ${it.endX} offset: ${offset.x}"
//                    )
//                    if (offset.x <= it.endX && offset.x >= it.startX) {
//                        it.isSelected = true
//                        Log.d("Selecting", "selected ")
//                        it.radius = 550.dp
//                    }
//                }


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




                newCardList = tempCards

                Log.d("onTap", "OnTap")
                isPlaying = true


                animateKey = animateKey.not()


            }
        }
    ) {

        drawContext.canvas.nativeCanvas.apply {
            circleCenter = Offset(width / 2f, height + radius.toPx() / 2f)
        }
        maxAngle = CENTER_ANGLE + (cards.size / 2f * CARD_DISTANCE_ANGLE)
        minAngle = CENTER_ANGLE - (cards.size / 2f * CARD_DISTANCE_ANGLE)


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

        Log.d("StartX", "startX ->$startX")
        it.startX = startX
        it.endX = endX

    }

}


private fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}





