package com.cookie.animind.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class AniListResponse(
    val data: AniListData?
)

@JsonClass(generateAdapter = true)
data class AniListData(
    val Page: PageData?,
    val Media: AnimeMedia?
)

@JsonClass(generateAdapter = true)
data class PageData(
    val media: List<AnimeMedia>?,
    val reviews: List<Review>?
)

@JsonClass(generateAdapter = true)
data class Review(
    val id: Int,
    val summary: String?,
    val rating: Int?,
    val score: Int?,
    val user: ReviewUser?
)

@JsonClass(generateAdapter = true)
data class ReviewUser(
    val name: String?,
    val avatar: UserAvatar?
)

@JsonClass(generateAdapter = true)
data class UserAvatar(
    val large: String?,
    val medium: String?
)

@JsonClass(generateAdapter = true)
data class AnimeMedia(
    val id: Int,
    val title: AnimeTitle?,
    val description: String?,
    val averageScore: Int?,
    val popularity: Int?,
    val genres: List<String>?,
    val status: String?,
    val episodes: Int?,
    val coverImage: CoverImage?
)

@JsonClass(generateAdapter = true)
data class AnimeTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
)

@JsonClass(generateAdapter = true)
data class CoverImage(
    val extraLarge: String?,
    val large: String?,
    val medium: String?,
    val color: String?
)

interface AniListApiService {
    @POST("/")
    suspend fun query(@Body request: GraphQLRequest): AniListResponse
}
