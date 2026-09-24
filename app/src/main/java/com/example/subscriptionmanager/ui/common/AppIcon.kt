package com.example.subscriptionmanager.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.subscriptionmanager.util.getAppIcon

@Composable
fun AppIcon(
    packageName: String?,
    fallbackLetter: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawable = remember(key1 = packageName) {
        packageName?.let { getAppIcon(context, it)}
    }

    if (drawable != null) {
        Image(
            bitmap = drawable.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = modifier.size(48.dp)
        )
    } else {
        Box(
            modifier = modifier
                .size(48.dp)
                .background(Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(fallbackLetter.uppercase())
        }
    }
}