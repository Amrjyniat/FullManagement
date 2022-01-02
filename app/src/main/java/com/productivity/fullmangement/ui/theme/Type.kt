package com.productivity.fullmangement.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.productivity.fullmangement.R

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

val texTitleStyle = TextStyle(
    fontFamily = FontFamily.Default,
    color = Color.Black,
    fontSize = 18.sp
)

val texDetailsStyle = texTitleStyle.copy(fontSize = 20.sp, color = Color.DarkGray)

val textHintStyle = Typography.subtitle1.copy(color = Color.Gray.copy(alpha = 0.7f))