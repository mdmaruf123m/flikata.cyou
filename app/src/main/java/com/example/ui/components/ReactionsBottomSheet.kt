package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.Reaction
import com.example.ui.theme.FlikataDivider
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionsBottomSheet(
    post: Post,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("All") }

    val likeCount = remember(post.reactions) { post.reactions.count { it.emoji == "👍" || it.emoji == "Like" } }
    val loveCount = remember(post.reactions) { post.reactions.count { it.emoji == "❤️" || it.emoji == "Love" } }
    val hahaCount = remember(post.reactions) { post.reactions.count { it.emoji == "😆" || it.emoji == "Haha" } }
    val totalCount = post.reactions.size.coerceAtLeast(post.likesCount)

    val filteredReactions = remember(selectedTab, post.reactions) {
        when (selectedTab) {
            "Like" -> post.reactions.filter { it.emoji == "👍" || it.emoji == "Like" }
            "Love" -> post.reactions.filter { it.emoji == "❤️" || it.emoji == "Love" }
            "Haha" -> post.reactions.filter { it.emoji == "😆" || it.emoji == "Haha" }
            else -> post.reactions
        }
    }

    val sheetSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val sheetOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val textSecondary = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    val sheetOutline = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Filter Tabs Row matching Graphic Reaction Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabChip(
                    label = "All $totalCount",
                    reactionType = null,
                    isSelected = selectedTab == "All",
                    onClick = { selectedTab = "All" }
                )
                if (likeCount > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    TabChip(
                        label = "$likeCount",
                        reactionType = "Like",
                        isSelected = selectedTab == "Like",
                        onClick = { selectedTab = "Like" }
                    )
                }
                if (loveCount > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    TabChip(
                        label = "$loveCount",
                        reactionType = "Love",
                        isSelected = selectedTab == "Love",
                        onClick = { selectedTab = "Love" }
                    )
                }
                if (hahaCount > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    TabChip(
                        label = "$hahaCount",
                        reactionType = "Haha",
                        isSelected = selectedTab == "Haha",
                        onClick = { selectedTab = "Haha" }
                    )
                }
            }

            Divider(color = sheetOutline, thickness = 0.5.dp)

            // People List or Empty State
            if (filteredReactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reactions yet",
                        color = textSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(filteredReactions) { reaction ->
                        ReactionUserRow(reaction = reaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    label: String,
    reactionType: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (reactionType != null) {
                GraphicReactionIcon(type = reactionType, size = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) FlikataPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .background(FlikataPrimary, CircleShape)
            )
        }
    }
}

@Composable
private fun ReactionUserRow(reaction: Reaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = reaction.userAvatar,
                contentDescription = reaction.userName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = reaction.userName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }

        GraphicReactionIcon(
            type = reaction.emoji,
            size = 24.dp
        )
    }
}
