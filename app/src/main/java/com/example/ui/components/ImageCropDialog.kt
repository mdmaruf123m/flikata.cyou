package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.FlikataPrimary
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    isCoverCrop: Boolean, // true for wide banner (16:9), false for profile pic (1:1 circle)
    onDismiss: () -> Unit,
    onCropDone: (Uri) -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }

                    Text(
                        text = if (isCoverCrop) "Crop Cover Banner" else "Crop Profile Picture",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                val croppedUri = cropAndSaveImage(
                                    context = context,
                                    uri = imageUri,
                                    scale = scale,
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    isCover = isCoverCrop
                                )
                                isSaving = false
                                onCropDone(croppedUri ?: imageUri)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FlikataPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("crop_done_btn")
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Interactive Crop Viewport with Mask
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    val maxOffset = 500f * scale
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val viewportWidth = maxWidth
                        val viewportHeight = maxHeight

                        // Scaled & Panned Image
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Crop Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offsetX
                                    translationY = offsetY
                                }
                        )

                        // Dark Scrim Canvas with Cutout Window
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val cropRect = if (isCoverCrop) {
                                // 16:9 or 2:1 Rectangular Banner Cutout
                                val rectWidth = w * 0.92f
                                val rectHeight = rectWidth / 1.77f
                                Rect(
                                    left = (w - rectWidth) / 2f,
                                    top = (h - rectHeight) / 2f,
                                    right = (w + rectWidth) / 2f,
                                    bottom = (h + rectHeight) / 2f
                                )
                            } else {
                                // 1:1 Circular Profile Cutout
                                val diameter = minOf(w * 0.85f, h * 0.6f)
                                Rect(
                                    left = (w - diameter) / 2f,
                                    top = (h - diameter) / 2f,
                                    right = (w + diameter) / 2f,
                                    bottom = (h + diameter) / 2f
                                )
                            }

                            val cropPath = Path().apply {
                                if (isCoverCrop) {
                                    addRoundRect(
                                        androidx.compose.ui.geometry.RoundRect(
                                            rect = cropRect,
                                            cornerRadius = CornerRadius(16f, 16f)
                                        )
                                    )
                                } else {
                                    addOval(cropRect)
                                }
                            }

                            // Darken everything OUTSIDE the crop window
                            clipPath(cropPath, clipOp = ClipOp.Difference) {
                                drawRect(color = Color.Black.copy(alpha = 0.65f))
                            }

                            // White border guide around crop window
                            if (isCoverCrop) {
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.8f),
                                    topLeft = Offset(cropRect.left, cropRect.top),
                                    size = Size(cropRect.width, cropRect.height),
                                    cornerRadius = CornerRadius(16f, 16f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            } else {
                                drawOval(
                                    color = Color.White.copy(alpha = 0.8f),
                                    topLeft = Offset(cropRect.left, cropRect.top),
                                    size = Size(cropRect.width, cropRect.height),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }

                            // Rule of thirds subtle grid lines inside crop window
                            val stepX = cropRect.width / 3f
                            val stepY = cropRect.height / 3f
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(cropRect.left + stepX, cropRect.top),
                                end = Offset(cropRect.left + stepX, cropRect.bottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(cropRect.left + stepX * 2, cropRect.top),
                                end = Offset(cropRect.left + stepX * 2, cropRect.bottom),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(cropRect.left, cropRect.top + stepY),
                                end = Offset(cropRect.right, cropRect.top + stepY),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(cropRect.left, cropRect.top + stepY * 2),
                                end = Offset(cropRect.right, cropRect.top + stepY * 2),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                // Bottom Controls: Zoom Slider & Reset
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF18191A))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pinch or slide to zoom • Drag to adjust position",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 1f..4f,
                            colors = SliderDefaults.colors(
                                thumbColor = FlikataPrimary,
                                activeTrackColor = FlikataPrimary,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Real Bitmap cropping logic:
 * Reads source bitmap, applies scale & translation, crops to square or banner aspect ratio,
 * and saves into app's cache directory, returning the cropped file Uri.
 */
private fun cropAndSaveImage(
    context: Context,
    uri: Uri,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    isCover: Boolean
): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null

        val origWidth = originalBitmap.width
        val origHeight = originalBitmap.height

        // Target aspect ratio: 1:1 for Profile, 16:9 (1.777f) for Banner
        val targetAspect = if (isCover) 16f / 9f else 1f

        var cropWidth: Int
        var cropHeight: Int

        if (origWidth.toFloat() / origHeight > targetAspect) {
            // Original is wider than target aspect -> cap height
            cropHeight = (origHeight / scale).toInt().coerceIn(1, origHeight)
            cropWidth = (cropHeight * targetAspect).toInt().coerceIn(1, origWidth)
        } else {
            // Original is taller than target aspect -> cap width
            cropWidth = (origWidth / scale).toInt().coerceIn(1, origWidth)
            cropHeight = (cropWidth / targetAspect).toInt().coerceIn(1, origHeight)
        }

        // Center start points adjusted by pan offset
        val normalizedPanX = (offsetX / 500f) * (origWidth - cropWidth) / 2f
        val normalizedPanY = (offsetY / 500f) * (origHeight - cropHeight) / 2f

        val startX = ((origWidth - cropWidth) / 2 - normalizedPanX.toInt()).coerceIn(0, origWidth - cropWidth)
        val startY = ((origHeight - cropHeight) / 2 - normalizedPanY.toInt()).coerceIn(0, origHeight - cropHeight)

        val croppedBitmap = Bitmap.createBitmap(originalBitmap, startX, startY, cropWidth, cropHeight)

        // Save cropped bitmap to temporary cache file
        val filename = "cropped_${if (isCover) "cover" else "profile"}_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.cacheDir, filename)
        val outStream = FileOutputStream(outputFile)
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, outStream)
        outStream.flush()
        outStream.close()

        Uri.fromFile(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
