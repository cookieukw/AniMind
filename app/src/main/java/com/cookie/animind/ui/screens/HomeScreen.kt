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
            when (selectedTab) {
                0 -> {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is UiState.Success -> {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // AI BANNER
                                item {
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
                                if (state.trending.isNotEmpty()) {
                                    val heroAnime = state.trending.first()
                                    item {
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
                                            // Gradient Overlay
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                                            colors = listOf(
                                                                androidx.compose.ui.graphics.Color.Transparent,
                                                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                                                            )
                                                        )
                                                    )
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(16.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("${heroAnime.averageScore ?: "??"}%", style = MaterialTheme.typography.labelMedium, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    heroAnime.title?.english ?: heroAnime.title?.romaji ?: "Unknown",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = androidx.compose.ui.graphics.Color.White,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    
                                    item {
                                        TopicRow(if (language == "Português") "🔥 Em Alta" else "🔥 Trending Now", state.trending.drop(1), viewModel, onAnimeClick)
                                    }
                                }

                                if (state.romance.isNotEmpty()) {
                                    item { Spacer(modifier = Modifier.height(24.dp)) }
                                    item {
                                        TopicRow(if (language == "Português") "⭐ Melhor Avaliados (Romance)" else "⭐ Top Rated Romance", state.romance, viewModel, onAnimeClick)
                                    }
                                }
                                
                                if (state.recent.isNotEmpty()) {
                                    item { Spacer(modifier = Modifier.height(24.dp)) }
                                    item {
                                        TopicRow(if (language == "Português") "🆕 Temporada Atual" else "🆕 Current Season", state.recent, viewModel, onAnimeClick)
                                    }
                                }

                                if (state.movies.isNotEmpty()) {
                                    item { Spacer(modifier = Modifier.height(24.dp)) }
                                    item {
                                        TopicRow(if (language == "Português") "\uD83C\uDFAC Filmes Recomendados" else "\uD83C\uDFAC Recommended Movies", state.movies, viewModel, onAnimeClick)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Search Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
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

                        when (val state = uiState) {
                            is UiState.Success -> {
                                if (!state.searchResults.isNullOrEmpty() && searchQuery.isNotBlank()) {
                                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                                        items(state.searchResults, key = { it.id }) { anime ->
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
                2 -> {
                    // Favorites
                    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                    
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
                3 -> {
                    SettingsScreen(viewModel = viewModel, onBackClick = {})
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
    Column {
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