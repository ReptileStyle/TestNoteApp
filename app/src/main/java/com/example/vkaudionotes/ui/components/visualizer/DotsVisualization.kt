package com.example.vkaudionotes.ui.components.visualizer


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random


@Composable
fun VoiceVisualizerDots(
    amplitude:Int = 0,
    dotsSize: Dp = 5.dp,
    color: Color = MaterialTheme.colors.primary
) {
    val maxOffset = 10f

    @Composable
    fun Dot(
        offset: Float
    ) = Spacer(
        Modifier
            .size(dotsSize)
            .offset(y = -offset.dp)
            .background(
                color = color,
                shape = CircleShape
            )
    )


    val value1 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/2500)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val value2 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/2000)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val value3 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/1500)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val value4 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/1000)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val value5 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/1500)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val value6 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/2000)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    val value7 by animateFloatAsState(
        targetValue = (amplitude.toFloat()/2500)*(Random.nextFloat()-Random.nextFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = maxOffset.dp)
    ) {
        val spaceSize = 2.dp
        Dot(value1.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
        Spacer(Modifier.width(spaceSize))
        Dot(value2.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
        Spacer(Modifier.width(spaceSize))
        Dot(value3.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
        Spacer(Modifier.width(spaceSize))
        Dot(value4.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
        Spacer(Modifier.width(spaceSize))
        Dot(value5.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
        Spacer(Modifier.width(spaceSize))
        Dot(value6.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
        Spacer(Modifier.width(spaceSize))
        Dot(value7.coerceIn(minimumValue = -5.0f, maximumValue = 5.0f))
    }
}

