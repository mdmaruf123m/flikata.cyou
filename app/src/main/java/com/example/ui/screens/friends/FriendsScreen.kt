package com.example.ui.screens.friends

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel

data class FriendItem(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val mutualFriends: Int,
    val bio: String = "",
    val isVerified: Boolean = false,
    val timeAgo: String = "",
    var isConfirmed: Boolean = false,
    var isRequestSent: Boolean = false
)

enum class FriendsTab {
    SUGGESTIONS,
    REQUESTS,
    YOUR_FRIENDS
}

@Composable
fun FriendsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    // Intercept device back button to return to Home
    BackHandler {
        onBack()
    }

    var selectedTab by remember { mutableStateOf(FriendsTab.SUGGESTIONS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Friend requests state list
    val friendRequests = remember {
        mutableStateListOf(
            FriendItem(
                id = "req_1",
                name = "Sabbir Ahmed",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                mutualFriends = 8,
                bio = "Photography enthusiast & traveler",
                isVerified = true,
                timeAgo = "1d ago"
            ),
            FriendItem(
                id = "req_2",
                name = "Nusrat Jahan",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
                mutualFriends = 14,
                bio = "Studying Computer Science at DU",
                isVerified = false,
                timeAgo = "2d ago"
            ),
            FriendItem(
                id = "req_3",
                name = "Tanvir Hasan",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400",
                mutualFriends = 3,
                bio = "Full-stack developer & gamer",
                isVerified = false,
                timeAgo = "4d ago"
            )
        )
    }

    // Suggested Friends list
    val suggestions = remember {
        mutableStateListOf(
            FriendItem(
                id = "sug_1",
                name = "Rakib Khan",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                mutualFriends = 19,
                bio = "Designer & Tech lover • Dhaka",
                isVerified = true
            ),
            FriendItem(
                id = "sug_2",
                name = "Sadia Islam",
                avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400",
                mutualFriends = 11,
                bio = "Content Creator • Flikata Reels",
                isVerified = true
            ),
            FriendItem(
                id = "sug_3",
                name = "Mahfuzur Rahman",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                mutualFriends = 6,
                bio = "Digital Marketing Strategist",
                isVerified = false
            ),
            FriendItem(
                id = "sug_4",
                name = "Farzana Akhter",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
                mutualFriends = 15,
                bio = "Fashion & Lifestyle Blogger",
                isVerified = false
            ),
            FriendItem(
                id = "sug_5",
                name = "Arafat Hossain",
                avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=400",
                mutualFriends = 4,
                bio = "Musician & Guitarist",
                isVerified = false
            )
        )
    }

    // Your existing Friends list
    val yourFriends = remember {
        mutableStateListOf(
            FriendItem(
                id = "frnd_1",
                name = "Shafiqul Islam",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                mutualFriends = 25,
                bio = "Software Engineer at Brain Station",
                isVerified = true
            ),
            FriendItem(
                id = "frnd_2",
                name = "Rima Akter",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
                mutualFriends = 32,
                bio = "Photographer & Creative Director",
                isVerified = true
            ),
            FriendItem(
                id = "frnd_3",
                name = "Asif Mahmud",
                avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400",
                mutualFriends = 18,
                bio = "Product Designer • UI/UX",
                isVerified = false
            )
        )
    }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Minimal Custom Header with Back Button (NO default FlikataHeader!)
        Surface(
            color = surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("friends_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Friends",
                        color = onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )

                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Close Search" else "Search Friends",
                            tint = onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Search Bar Field (Toggled)
                AnimatedVisibility(visible = isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search friends...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlikataPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                // Tabs: Suggestions, Requests, Your Friends
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTab == FriendsTab.SUGGESTIONS,
                            onClick = { selectedTab = FriendsTab.SUGGESTIONS },
                            label = { Text("Suggestions", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlikataPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        val reqCount = friendRequests.count { !it.isConfirmed }
                        FilterChip(
                            selected = selectedTab == FriendsTab.REQUESTS,
                            onClick = { selectedTab = FriendsTab.REQUESTS },
                            label = {
                                Text(
                                    if (reqCount > 0) "Requests ($reqCount)" else "Requests",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlikataPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedTab == FriendsTab.YOUR_FRIENDS,
                            onClick = { selectedTab = FriendsTab.YOUR_FRIENDS },
                            label = {
                                Text(
                                    "Your Friends (${yourFriends.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlikataPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // Body Content based on selected tab
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                FriendsTab.SUGGESTIONS -> {
                    val filteredSuggestions = suggestions.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    item {
                        Text(
                            text = "People you may know",
                            color = onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (filteredSuggestions.isEmpty()) {
                        item {
                            EmptyState(text = "No friend suggestions right now.")
                        }
                    } else {
                        items(filteredSuggestions, key = { it.id }) { item ->
                            SuggestionFriendCard(
                                item = item,
                                onAddFriend = {
                                    item.isRequestSent = !item.isRequestSent
                                },
                                onRemove = {
                                    suggestions.remove(item)
                                }
                            )
                        }
                    }
                }

                FriendsTab.REQUESTS -> {
                    val filteredRequests = friendRequests.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    item {
                        val pendingCount = friendRequests.count { !it.isConfirmed }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Friend Requests",
                                color = onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (pendingCount > 0) {
                                Text(
                                    text = "$pendingCount pending",
                                    color = FlikataPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (filteredRequests.isEmpty()) {
                        item {
                            EmptyState(text = "No pending friend requests.")
                        }
                    } else {
                        items(filteredRequests, key = { it.id }) { item ->
                            FriendRequestCard(
                                item = item,
                                onConfirm = {
                                    item.isConfirmed = true
                                    yourFriends.add(0, item)
                                },
                                onDelete = {
                                    friendRequests.remove(item)
                                }
                            )
                        }
                    }
                }

                FriendsTab.YOUR_FRIENDS -> {
                    val filteredFriends = yourFriends.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Friends",
                                color = onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${yourFriends.size} friends",
                                color = onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (filteredFriends.isEmpty()) {
                        item {
                            EmptyState(text = "No friends found matching your search.")
                        }
                    } else {
                        items(filteredFriends, key = { it.id }) { item ->
                            YourFriendCard(
                                item = item,
                                onMessageClick = {
                                    // Open chats or conversation
                                    viewModel.navigateTo(com.example.ui.viewmodel.Screen.CHATS)
                                },
                                onUnfriend = {
                                    yourFriends.remove(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRequestCard(
    item: FriendItem,
    onConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlikataAvatar(
                name = item.name,
                avatarUrl = item.avatarUrl,
                size = 64.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }

                    if (item.timeAgo.isNotBlank()) {
                        Text(
                            text = item.timeAgo,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.mutualFriends > 0) {
                    Text(
                        text = "${item.mutualFriends} mutual friends",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (item.isConfirmed) {
                    Surface(
                        color = FlikataPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = FlikataPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Request Accepted",
                                color = FlikataPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = FlikataPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text("Confirm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Text(
                                "Delete",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionFriendCard(
    item: FriendItem,
    onAddFriend: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddFriend() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlikataAvatar(
                name = item.name,
                avatarUrl = item.avatarUrl,
                size = 64.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 14.dp)
                    }
                }

                if (item.bio.isNotBlank()) {
                    Text(
                        text = item.bio,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.mutualFriends > 0) {
                    Text(
                        text = "${item.mutualFriends} mutual friends",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onAddFriend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.isRequestSent) MaterialTheme.colorScheme.surfaceVariant else FlikataPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isRequestSent) Icons.Default.Check else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = if (item.isRequestSent) FlikataPrimary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.isRequestSent) "Following" else "Follow",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isRequestSent) FlikataPrimary else Color.White
                        )
                    }

                    Button(
                        onClick = onRemove,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(38.dp)
                    ) {
                        Text(
                            "Remove",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YourFriendCard(
    item: FriendItem,
    onMessageClick: () -> Unit,
    onUnfriend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlikataAvatar(
                name = item.name,
                avatarUrl = item.avatarUrl,
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 14.dp)
                    }
                }

                if (item.bio.isNotBlank()) {
                    Text(
                        text = item.bio,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onMessageClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FlikataPrimary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Message",
                    tint = FlikataPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}
