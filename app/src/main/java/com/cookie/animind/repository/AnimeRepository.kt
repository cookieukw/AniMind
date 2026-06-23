package com.cookie.animind.repository

import com.cookie.animind.BuildConfig
import com.cookie.animind.data.remote.*

class AnimeRepository {
    private val anilist = ApiClient.anilistService
    private val gemini = ApiClient.geminiService

    suspend fun getTrendingAnime(): List<AnimeMedia> {
        return getCategoryAnime("sort: TRENDING_DESC")
    }

    suspend fun getRomanceAnime(): List<AnimeMedia> {
        return getCategoryAnime("sort: POPULARITY_DESC, genre: \"Romance\"")
    }

    suspend fun getAnimeMovies(): List<AnimeMedia> {
        return getCategoryAnime("sort: POPULARITY_DESC, format: MOVIE")
    }

    suspend fun getRecentAnime(): List<AnimeMedia> {
        return getCategoryAnime("sort: START_DATE_DESC, status: RELEASING")
    }

    private suspend fun getCategoryAnime(filter: String): List<AnimeMedia> {
        val query = """
            query {
              Page(page: 1, perPage: 8) {
                media(type: ANIME, $filter) {
                  id
                  title {
                    romaji
                    english
                  }
                  description
                  averageScore
                  popularity
                  genres
                  status
                  episodes
                  coverImage {
                    large
                  }
                }
              }
            }
        """.trimIndent()
        val response = anilist.query(GraphQLRequest(query = query))
        return response.data?.Page?.media ?: emptyList()
    }

    suspend fun searchAnime(queryStr: String): List<AnimeMedia> {
        val query = """
            query(${'$'}search: String) {
              Page(page: 1, perPage: 20) {
                media(search: ${'$'}search, type: ANIME, sort: POPULARITY_DESC) {
                  id
                  title {
                    romaji
                    english
                  }
                  description
                  averageScore
                  popularity
                  genres
                  status
                  episodes
                  coverImage {
                    large
                  }
                }
              }
            }
        """.trimIndent()

        val response = anilist.query(GraphQLRequest(query = query, variables = mapOf("search" to queryStr)))
        return response.data?.Page?.media ?: emptyList()
    }
    
    suspend fun getAnimeById(id: Int): AnimeMedia? {
        val query = """
            query {
              Media(id: $id, type: ANIME) {
                  id
                  title {
                    romaji
                    english
                  }
                  description
                  averageScore
                  popularity
                  genres
                  status
                  episodes
                  coverImage {
                    extraLarge
                    large
                  }
              }
            }
        """.trimIndent()
        val response = anilist.query(GraphQLRequest(query = query)) 
        return response.data?.Media
    }

    suspend fun getAnimeReviews(id: Int): List<Review> {
        val query = """
            query {
              Page(page: 1, perPage: 5) {
                reviews(mediaId: $id, sort: RATING_DESC) {
                  id
                  summary
                  rating
                  score
                  user {
                    name
                    avatar {
                      large
                      medium
                    }
                  }
                }
              }
            }
        """.trimIndent()
        val response = anilist.query(GraphQLRequest(query = query))
        return response.data?.Page?.reviews ?: emptyList()
    }

    suspend fun analyzeAnime(anime: AnimeMedia, apiKey: String, useAi: Boolean, responseMode: String, language: String): String {
        if (!useAi) return if (language == "Português") "A IA está desativada nas configurações." else "AI is disabled in settings."
        if (apiKey.isBlank()) return if (language == "Português") "Por favor, configure sua API Key nas configurações." else "Please configure your API Key in settings."

        val prompt = """
            You are a specialized anime analyst.
            
            Use the following data:
            - Anime Title: ${anime.title?.english ?: anime.title?.romaji}
            - Description: ${anime.description}
            - Genres: ${anime.genres?.joinToString()}
            - Average Score: ${anime.averageScore}
            
            You must reply entirely in the following language: $language.
            Do NOT use heavy markdown headers (*, **, #, ##).
            
            Format your response EXACTLY like this clear, simple structure:
            
            NOTA IA: X.X/10
            VEREDITO: (One very short sentence saying if it's worth watching)
            
            PONTOS FORTES:
            ✔ (short bullet point 1)
            ✔ (short bullet point 2)
            ✔ (short bullet point 3)
            
            PONTOS FRACOS:
            ✖ (short bullet point 1)
            
            RESUMO:
            (1 short paragraph with your detailed opinion. No more than 3-4 sentences.)
        """.trimIndent()

        return callGemini(apiKey, prompt)
    }
    
    suspend fun chatAboutAnime(anime: AnimeMedia, userMessage: String, persona: String, apiKey: String, useAi: Boolean, language: String): String {
        if (!useAi) return "A Inteligência Artificial está desativada nas configurações."
        if (apiKey.isBlank()) return "Por favor, configure sua API Key nas configurações."

         val prompt = """
            You are a specialized anime analyst. Persona: $persona.
            You must reply entirely in the following language: $language.
            You are talking about the anime: ${anime.title?.english ?: anime.title?.romaji}.
            
            User's message/question:
            "$userMessage"
            
            Do not use markdown formatting (such as **, *, #) in your response, use plain text.
            If the user asks about a specific episode, focus on that episode conceptually while maintaining series context.
            Respond functionally and helpfully according to your persona.
        """.trimIndent()

        return callGemini(apiKey, prompt)
    }

    suspend fun translateText(text: String, apiKey: String, targetLanguage: String): String {
        if (text.isBlank() || apiKey.isBlank()) return text
        val prompt = "Translate the following text to $targetLanguage. Do not add any extra explanations or formatting, just the raw translated text.\n\n$text"
        return callGemini(apiKey, prompt)
    }

    private suspend fun callGemini(apiKey: String, prompt: String): String {
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        return try {
            val response = gemini.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from AI."
        } catch (e: Exception) {
            "Error analyzing with AI: ${e.message}"
        }
    }
}
