package com.cookie.animind.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookie.animind.AnimeApplication
import com.cookie.animind.data.remote.AnimeMedia
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

sealed class UiState {
    object Loading : UiState()
    data class Success(
        val trending: List<AnimeMedia>,
        val recent: List<AnimeMedia> = emptyList(),
        val romance: List<AnimeMedia> = emptyList(),
        val movies: List<AnimeMedia> = emptyList(),
        val searchResults: List<AnimeMedia>? = null
    ) : UiState()
    data class Error(val message: String) : UiState()
}

data class UiReview(
    val author: String,
    val avatarUrl: String?,
    val content: String,
    val isSystemMessage: Boolean = false,
    val likes: Int = 0
)

class AnimeViewModel : ViewModel() {
    private val repo = AnimeApplication.animeRepository
    private val settingsRepo = AnimeApplication.settingsRepository

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedAnime = MutableStateFlow<AnimeMedia?>(null)
    val selectedAnime: StateFlow<AnimeMedia?> = _selectedAnime.asStateFlow()

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()

    private val _communityComments = MutableStateFlow<List<UiReview>?>(null)
    val communityComments: StateFlow<List<UiReview>?> = _communityComments.asStateFlow()

    private val _similarAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val similarAnime: StateFlow<List<AnimeMedia>> = _similarAnime.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory.asStateFlow()

    val apiKey = settingsRepo.apiKey
    val responseMode = settingsRepo.responseMode
    val useAi = settingsRepo.useAi
    val language = settingsRepo.language

    private val _favorites = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val favorites: StateFlow<List<AnimeMedia>> = _favorites.asStateFlow()

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val trendingDeferred = async { repo.getTrendingAnime() }
                val recentDeferred = async { repo.getRecentAnime() }
                val romanceDeferred = async { repo.getRomanceAnime() }
                val moviesDeferred = async { repo.getAnimeMovies() }
                
                _uiState.value = UiState.Success(
                    trendingDeferred.await(), 
                    recentDeferred.await(), 
                    romanceDeferred.await(), 
                    moviesDeferred.await()
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchAnime(query: String) {
        if (query.isBlank()) {
            loadLists()
            return
        }
        viewModelScope.launch {
            val currentSuccess = _uiState.value as? UiState.Success
            _uiState.value = UiState.Loading
            try {
                val results = repo.searchAnime(query)
                if (currentSuccess != null) {
                    _uiState.value = currentSuccess.copy(searchResults = results)
                } else {
                    _uiState.value = UiState.Success(emptyList(), emptyList(), emptyList(), emptyList(), results)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectAnime(anime: AnimeMedia) {
        val cleanDesc = anime.description?.replace(Regex("<.*?>"), "") ?: ""
        _selectedAnime.value = anime.copy(description = cleanDesc)
        _analysisResult.value = null
        _communityComments.value = null
        _chatHistory.value = emptyList()
        viewModelScope.launch {
            _similarAnime.value = repo.getTrendingAnime().shuffled().take(5)
        }
    }

    fun getAnimeDetails(id: Int) {
        viewModelScope.launch {
            try {
                val anime = repo.getAnimeById(id)
                if (anime != null) {
                    _selectedAnime.value = anime
                }
            } catch (e: Exception) {
                // Ignore for now, let selectedAnime retain basic data passed by navigation argument if any.
            }
        }
    }

    fun analyzeCurrentAnime() {
        val anime = _selectedAnime.value ?: return
        
        viewModelScope.launch {
            val currentApiKey = apiKey.first()
            val isAiEnabled = useAi.first()
            val mode = responseMode.first()
            val lang = language.first()

            _analysisResult.value = if (lang == "Português") "Analisando..." else "Analyzing..."
            val result = repo.analyzeAnime(anime, currentApiKey, isAiEnabled, mode, lang)
            _analysisResult.value = result
        }
    }

    fun loadCommunityComments() {
        val anime = _selectedAnime.value ?: return
        
        viewModelScope.launch {
            val currentApiKey = apiKey.first()
            val isAiEnabled = useAi.first()
            val lang = language.first()

            _communityComments.value = listOf(UiReview(author = "System", avatarUrl = null, content = if (lang == "Português") "Buscando avaliações reais do AniList..." else "Fetching real reviews from AniList...", isSystemMessage = true))
            
            try {
                val reviews = repo.getAnimeReviews(anime.id)
                val activeReviews = reviews.take(5)
                
                if (activeReviews.isEmpty()) {
                     _communityComments.value = listOf(UiReview(author = "System", avatarUrl = null, content = if (lang == "Português") "Nenhuma avaliação encontrada." else "No reviews found.", isSystemMessage = true))
                     return@launch
                }

                // Prepare UI Reviews
                var uiReviews = activeReviews.map { r ->
                    UiReview(
                        author = r.user?.name ?: "Unknown",
                        avatarUrl = r.user?.avatar?.medium ?: r.user?.avatar?.large,
                        content = r.summary?.replace(Regex("<.*?>"), "")?.take(200)?.let { txt -> "$txt..." } ?: "",
                        likes = r.score ?: (10..500).random(),
                        isSystemMessage = false
                    )
                }

                if (lang == "Português" && isAiEnabled && currentApiKey.isNotBlank()) {
                     _communityComments.value = listOf(UiReview(author = "System", avatarUrl = null, content = "Traduzindo avaliações...", isSystemMessage = true))
                     val combinedText = uiReviews.joinToString(separator = "\n|||\n") { it.content }
                     val translationPrompt = "Translate the following user reviews to Portuguese. Keep them separated by the exact same delimiter '|||'. Do not add any extra text or formatting.\n\n$combinedText"
                     val request = com.cookie.animind.data.remote.GenerateContentRequest(
                         contents = listOf(com.cookie.animind.data.remote.Content(parts = listOf(com.cookie.animind.data.remote.Part(text = translationPrompt))))
                     )
                     val translated = try {
                         val response = com.cookie.animind.data.remote.ApiClient.geminiService.generateContent(currentApiKey, request)
                         response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                     } catch(e: Exception) { "Error" }
                     
                     if (translated.isNotBlank() && !translated.startsWith("Error")) {
                          val translatedTexts = translated.split("|||").map { it.trim() }
                          uiReviews = uiReviews.mapIndexed { index, review ->
                              if (index < translatedTexts.size && translatedTexts[index].isNotBlank()) {
                                  review.copy(content = translatedTexts[index])
                              } else review
                          }
                          _communityComments.value = uiReviews
                     } else {
                          _communityComments.value = uiReviews // fallback to english
                     }
                } else {
                     _communityComments.value = uiReviews
                }
            } catch (e: Exception) {
               _communityComments.value = listOf(UiReview(author = "System", avatarUrl = null, content = if (lang == "Português") "Erro ao buscar avaliações." else "Error fetching reviews.", isSystemMessage = true))
            }
        }
    }

    fun sendMessage(msg: String, currentPersona: String) {
        val anime = _selectedAnime.value ?: return
        
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add("User" to msg)
        _chatHistory.value = currentHistory

        // Add dummy typing indicator
        _chatHistory.value = currentHistory + listOf("AI" to "...")

        viewModelScope.launch {
            val currentApiKey = apiKey.first()
            val isAiEnabled = useAi.first()
            val lang = language.first()

            val response = repo.chatAboutAnime(anime, msg, currentPersona, currentApiKey, isAiEnabled, lang)
            val updatedHistory = currentHistory + listOf("AI" to response)
            _chatHistory.value = updatedHistory
        }
    }

    fun toggleFavorite(anime: AnimeMedia) {
        val current = _favorites.value.toMutableList()
        if (current.any { it.id == anime.id }) {
            current.removeAll { it.id == anime.id }
        } else {
            current.add(anime)
        }
        _favorites.value = current
    }

    fun updateApiKey(key: String) {
        viewModelScope.launch { settingsRepo.setApiKey(key) }
    }

    fun updateResponseMode(mode: String) {
        viewModelScope.launch { settingsRepo.setResponseMode(mode) }
    }

    fun updateUseAi(use: Boolean) {
        viewModelScope.launch { settingsRepo.setUseAi(use) }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch { settingsRepo.setLanguage(lang) }
    }
}
