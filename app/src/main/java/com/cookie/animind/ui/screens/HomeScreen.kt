package com.cookie.animind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cookie.animind.ui.Strings
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.cookie.animind.data.remote.AnimeMedia
import com.cookie.animind.ui.AnimeViewModel
import com.cookie.animind.ui.UiState
import com.cookie.animind.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeMedia) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    var isInitialLoad by remember { mutableStateOf(true) }

    // Debounce search
    LaunchedEffect(searchQuery) {
        if (isInitialLoad) {
            isInitialLoad = false
            return@LaunchedEffect
        }
        delay(500)
        viewModel.searchAnime(searchQuery)
    }

    var selectedTab by remember { mutableStateOf(0) }

    val language by viewModel.language.collectAsStateWithLifecycle(initialValue = if (java.util.Locale.getDefault().language == "pt") "Português" else "English")

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavBarItem(
                        icon = Icons.Default.Home, 
                        label = Strings.get("home", language), 
                        selected = selectedTab == 0, 
                        onClick = { selectedTab = 0 }
                    )
                    NavBarItem(
                        icon = Icons.Default.Search, 
                        label = if (language == "Português") "Buscar" else "Search", 
                        selected = selectedTab == 1, 
                        onClick = { selectedTab = 1 }
                    )
                    NavBarItem(
                        icon = if (selectedTab == 2) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        label = Strings.get("favorites", language), 
                        selected = selectedTab == 2, 
                        onClick = { selectedTab = 2 }
                    )
                    NavBarItem(
                        icon = Icons.Default.Settings, 
                        label = Strings.get("settings", language), 
                        selected = selectedTab == 3, 
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Use Box with visibility instead of when{} to preserve tab state
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    HomeTab(uiState, language, viewModel, onAnimeClick)
                }
                if (selectedTab == 1) {
                    SearchTab(searchQuery, { searchQuery = it }, uiState, language, viewModel, onAnimeClick)
                }
                if (selectedTab == 2) {
                    FavoritesTab(viewModel, language, onAnimeClick)
                }
                if (selectedTab == 3) {
                    SettingsScreen(viewModel = viewModel, onBackClick = {})
                }
            }
        }
    }
}

@Composable
private fun HomeTab(
    uiState: UiState,
    language: String,
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            val bgColor = MaterialTheme.colorScheme.background
            val heroGradient = remember(bgColor) {
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, bgColor.copy(alpha = 0.9f))
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // AI BANNER
                item(key = "ai_banner") {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(if (language == "Português") "\uD83E\uDD16 Descubra o próximo anime" else "\uD83E\uDD16 Discover your next anime", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(if (language == "Português") "Use a inteligência artificial para achar." else "Use artificial intelligence to find it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                // HERO BANNER
                if (uiState.trending.isNotEmpty()) {
                    val heroAnime = uiState.trending.first()
                    item(key = "hero_${heroAnime.id}") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(200.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                .clickable {
                                    viewModel.selectAnime(heroAnime)
                                    onAnimeClick(heroAnime)
                                }
                        ) {
                            AsyncImage(
                                model = heroAnime.coverImage?.extraLarge ?: heroAnime.coverImage?.large,
                                contentDescription = heroAnime.title?.romaji,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient Overlay - cached brush
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(heroGradient)
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${heroAnime.averageScore ?: "??"}%", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    heroAnime.title?.english ?: heroAnime.title?.romaji ?: "Unknown",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    item(key = "trending_row") {
                        TopicRow(if (language == "Português") "🔥 Em Alta" else "🔥 Trending Now", uiState.trending.drop(1), viewModel, onAnimeClick)
                    }
                }

                if (uiState.romance.isNotEmpty()) {
                    item(key = "spacer_romance") { Spacer(modifier = Modifier.height(24.dp)) }
                    item(key = "romance_row") {
                        TopicRow(if (language == "Português") "⭐ Melhor Avaliados (Romance)" else "⭐ Top Rated Romance", uiState.romance, viewModel, onAnimeClick)
                    }
                }

                if (uiState.recent.isNotEmpty()) {
                    item(key = "spacer_recent") { Spacer(modifier = Modifier.height(24.dp)) }
                    item(key = "recent_row") {
                        TopicRow(if (language == "Português") "🆕 Temporada Atual" else "🆕 Current Season", uiState.recent, viewModel, onAnimeClick)
                    }
                }

                if (uiState.movies.isNotEmpty()) {
                    item(key = "spacer_movies") { Spacer(modifier = Modifier.height(24.dp)) }
                    item(key = "movies_row") {
                        TopicRow(if (language == "Português") "\uD83C\uDFAC Filmes Recomendados" else "\uD83C\uDFAC Recommended Movies", uiState.movies, viewModel, onAnimeClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTab(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    uiState: UiState,
    language: String,
    viewModel: AnimeViewModel,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(56.dp),
            placeholder = { Text(if (language == "Português") "Buscar anime..." else "Search anime...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp)) },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        when (uiState) {
            is UiState.Success -> {
                if (!uiState.searchResults.isNullOrEmpty() && searchQuery.isNotBlank()) {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(uiState.searchResults, key = { it.id }) { anime ->
                            AnimeListItem(anime) {
                                viewModel.selectAnime(anime)
                                onAnimeClick(anime)
                            }
                        }
                    }
                } else if (searchQuery.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (language == "Português") "Nenhum resultado." else "No results.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (language == "Português") "Digite algo para buscar..." else "Type something to search...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun FavoritesTab(
    viewModel: AnimeViewModel,
    language: String,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            Strings.get("my_favorites", language),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    Strings.get("no_favorites", language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(favorites, key = { it.id }) { anime ->
                    AnimeListItem(anime) {
                        viewModel.selectAnime(anime)
                        onAnimeClick(anime)
                    }
                }
            }
        }
    }
}

@Composable
fun NavBarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (selected) {
            Box(
                modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
        Icon(
            icon, 
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(if (selected) 28.dp else 24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TopicRow(title: String, animes: List<AnimeMedia>, viewModel: AnimeViewModel, onAnimeClick: (AnimeMedia) -> Unit) {
    Column(modifier = Modifier.height(310.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(animes, key = { it.id }) { anime ->
                AnimeCard(anime) {
                    viewModel.selectAnime(anime)
                    onAnimeClick(anime)
                }
            }
        }
    }
}