package com.example.ui.screens.story

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel

data class StoryGradient(
    val id: String,
    val name: String,
    val brush: Brush,
    val previewColor: Color
)

val STORY_GRADIENTS = listOf(
    StoryGradient(
        id = "gradient_blue",
        name = "Royal Blue",
        brush = Brush.verticalGradient(listOf(Color(0xFF00B0FF), Color(0xFF0044FF))),
        previewColor = Color(0xFF1877F2)
    ),
    StoryGradient(
        id = "purple",
        name = "Purple Magenta",
        brush = Brush.verticalGradient(listOf(Color(0xFF8E24AA), Color(0xFF4A148C))),
        previewColor = Color(0xFF8E24AA)
    ),
    StoryGradient(
        id = "orange",
        name = "Sunset Orange",
        brush = Brush.verticalGradient(listOf(Color(0xFFFF6D00), Color(0xFFD50000))),
        previewColor = Color(0xFFFF6D00)
    ),
    StoryGradient(
        id = "crimson",
        name = "Crimson Red",
        brush = Brush.verticalGradient(listOf(Color(0xFFFF1744), Color(0xFF880E4F))),
        previewColor = Color(0xFFFF1744)
    ),
    StoryGradient(
        id = "teal",
        name = "Emerald Teal",
        brush = Brush.verticalGradient(listOf(Color(0xFF00BFA5), Color(0xFF004D40))),
        previewColor = Color(0xFF00BFA5)
    ),
    StoryGradient(
        id = "black",
        name = "Midnight Black",
        brush = Brush.verticalGradient(listOf(Color(0xFF303030), Color(0xFF121212))),
        previewColor = Color(0xFF212121)
    )
)

@Composable
fun CreateStoryScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(0) } // 0 = Photo Story, 1 = Text Story
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var storyText by remember { mutableStateOf("") }
    var selectedGradientId by remember { mutableStateOf("gradient_blue") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val cached = viewModel.cacheMediaFromUri(uri, isVideo = false)
            selectedImageUri = Uri.parse(cached)
            selectedMode = 0
        }
    }

    val currentGradient = STORY_GRADIENTS.firstOrNull { it.id == selectedGradientId } ?: STORY_GRADIENTS[0]

    val canShare = if (selectedMode == 0) {
        selectedImageUri != null
    } else {
        storyText.trim().isNotEmpty()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create story",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = {
                        if (selectedMode == 0 && selectedImageUri != null) {
                            viewModel.addStory(
                                storyImage = selectedImageUri.toString(),
                                textContent = null,
                                backgroundColor = null
                            )
                            onClose()
                        } else if (selectedMode == 1 && storyText.trim().isNotEmpty()) {
                            viewModel.addStory(
                                storyImage = null,
                                textContent = storyText.trim(),
                                backgroundColor = selectedGradientId
                            )
                            onClose()
                        }
                    },
                    enabled = canShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlikataPrimary,
                        disabledContainerColor = FlikataPrimary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("share_story_btn")
                ) {
                    Text(
                        text = "Share",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Mode Selector: Photo Story vs Text Story
            TabRow(
                selectedTabIndex = selectedMode,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = FlikataPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedMode]),
                        color = FlikataPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedMode == 0,
                    onClick = { selectedMode = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo Story", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedMode == 1,
                    onClick = { selectedMode = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatColorText, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Text Story", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMode == 0) {
                    // PHOTO STORY MODE
                    if (selectedImageUri != null) {
                        // Image Preview Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Story Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // "Change photo" overlay button
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change photo", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    } else {
                        // Empty State: Prompt to select photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    FlikataPrimary.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(FlikataPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Pick Image",
                                        tint = FlikataPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Select photo from Gallery",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap here to choose a picture to share with your friends",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // TEXT STORY MODE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(currentGradient.brush),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedTextField(
                            value = storyText,
                            onValueChange = { storyText = it },
                            placeholder = {
                                Text(
                                    text = "Start typing...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .testTag("story_text_input")
                        )
                    }
                }
            }

            // Bottom Palette for Text Story Mode
            if (selectedMode == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Background Gradient",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(STORY_GRADIENTS) { gradient ->
                            val isSelected = gradient.id == selectedGradientId
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(gradient.previewColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedGradientId = gradient.id },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
