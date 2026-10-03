package com.example.data.remote

import com.example.data.model.AuthResponse
import com.example.data.model.ChatMessage
import com.example.data.model.CommentResponse
import com.example.data.model.GenericResponse
import com.example.data.model.Post
import com.example.data.model.StoriesResponse
import com.example.data.model.Story
import com.example.data.model.UploadResponse
import com.example.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FlikataApiService {

    @FormUrlEncoded
    @POST("auth/login.php")
    suspend fun login(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("auth/register.php")
    suspend fun register(
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("account_type") accountType: String, // "email" or "phone"
        @Field("email_or_phone") emailOrPhone: String,
        @Field("dob") dob: String,
        @Field("gender") gender: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @Multipart
    @POST("upload.php")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody? = null
    ): Response<UploadResponse>

    @FormUrlEncoded
    @POST("posts/create_post.php")
    suspend fun createPost(
        @Field("user_id") userId: String,
        @Field("author_name") authorName: String,
        @Field("content") content: String,
        @Field("background_style") backgroundStyle: String?,
        @Field("privacy") privacy: String,
        @Field("image_url") imageUrl: String?,
        @Field("video_url") videoUrl: String? = null
    ): Response<Post>

    @GET("posts/get_posts.php")
    suspend fun getPosts(
        @Query("user_id") userId: String? = null
    ): Response<List<Post>>

    @FormUrlEncoded
    @POST("posts/toggle_like.php")
    suspend fun toggleLike(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("posts/set_reaction.php")
    suspend fun setReaction(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String,
        @Field("emoji") emoji: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("comments/add_comment.php")
    suspend fun addComment(
        @Field("post_id") postId: String?,
        @Field("reel_id") reelId: String?,
        @Field("user_id") userId: String,
        @Field("text") text: String
    ): Response<CommentResponse>

    @GET("chats/get_conversations.php")
    suspend fun getChats(
        @Query("user_id") userId: String
    ): Response<List<ChatMessage>>

    @FormUrlEncoded
    @POST("profile/update_profile.php")
    suspend fun updateProfile(
        @Field("user_id") userId: String,
        @Field("full_name") fullName: String? = null,
        @Field("bio") bio: String? = null,
        @Field("occupation") occupation: String? = null,
        @Field("location") location: String? = null,
        @Field("profile_pic") profilePic: String? = null,
        @Field("cover_pic") coverPic: String? = null
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("posts/delete_post.php")
    suspend fun deletePost(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @GET("reels/get_reels.php")
    suspend fun getReels(
        @Query("user_id") userId: String? = null
    ): Response<ApiResponse<List<ReelModel>>>

    @FormUrlEncoded
    @POST("reels/add_reel.php")
    suspend fun addReel(
        @Field("user_id") userId: String,
        @Field("media_url") mediaUrl: String,
        @Field("caption") caption: String? = null,
        @Field("audio_title") audioTitle: String? = null
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("reels/delete_reel.php")
    suspend fun deleteReel(
        @Field("reel_id") reelId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @GET("reels/reel_settings.php")
    suspend fun getReelSettings(): Response<ApiResponse<ReelSettingsData>>

    @GET("stories/get_stories.php")
    suspend fun getStories(
        @Query("user_id") userId: String? = null
    ): Response<StoriesResponse>

    @FormUrlEncoded
    @POST("stories/add_story.php")
    suspend fun addStory(
        @Field("user_id") userId: String,
        @Field("story_image") storyImage: String? = null,
        @Field("text_content") textContent: String? = null,
        @Field("background_color") backgroundColor: String? = null
    ): Response<GenericResponse>
}
