package com.cookie.animind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cookie.animind.ui.AnimeViewModel
import com.cookie.animind.ui.Strings
import com.cookie.animind.ui.components.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: AnimeViewModel,
    onBackClick: () -> Unit,
    onChatClick: () -> Unit // Kept for signature compatibility but unused
) {
    val anime by viewModel.selectedAnime.collectAsStateWithLifecycle()
    val analysis by viewModel.analysisResult.collectAsStateWithLifecycle()
    val comments by viewModel.communityComments.collectAsStateWithLifecycle()
    val similar by viewModel.similarAnime.collectAsStateWithLifecycle()
    val spoilerLevel = "Safe" // Mocked since removed from settings
    val language by viewModel.language.collectAsStateWithLifecycle(initialValue = if (java.util.Locale.getDefault().language == "pt") "Português" else "English")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (anime == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(Strings.get("anime_not_found", language))
            }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                // Hero Image with Gradient Overlay
                Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    AsyncImage(
                        model = anime?.coverImage?.extraLarge ?: anime?.coverImage?.large,
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                    startY = 400f // Arbitrary fade start
                                )
                            )
                    )
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title
                    Text(
                        text = anime?.title?.english ?: anime?.title?.romaji ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Genres & Basic Info
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "⭐ ${anime?.averageScore ?: "?"}%", 
                                style = MaterialTheme.typography.labelLarge, 
                                color = MaterialTheme.colorScheme.secondary, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        anime?.genres?.forEach { genre ->
                             Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = genre, 
                                    style = MaterialTheme.typography.labelLarge, 
                                    color = MaterialTheme.colorScheme.primary, 
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Main AI CTA
                    Button(
                        onClick = { viewModel.analyzeCurrentAnime() },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "✨ ${if (language == "Português") "Vale a pena assistir?" else "Should I Watch?"}", 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // AI Analysis Section
                    if (analysis != null) {
                        val isError = analysis!!.startsWith("Error") || analysis!!.startsWith("Erro")
                        val isLoading = analysis == "Analyzing..." || analysis == "Analisando..."
                        
                        if (isError) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = analysis ?: "", 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { viewModel.analyzeCurrentAnime() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                        Text(if (language == "Português") "Tentar Novamente" else "Try Again")
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (language == "Português") "RECOMENDAÇÃO IA" else "AI RECOMMENDATION", 
                                            style = MaterialTheme.typography.titleMedium, 
                                            fontWeight = FontWeight.ExtraBold, 
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val cleanAnalysis = analysis?.replace(Regex("[*#~]"), "") ?: ""
                                    Text(cleanAnalysis, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSecondaryContainer, lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Stats Area - More modern cards directly here
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).height(90.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
                                Text("⭐ ${anime?.averageScore ?: "?"}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(Strings.get("global_score", language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).height(90.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
                                Text("🔥 #${anime?.popularity ?: "?"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(Strings.get("popularity", language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).height(90.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center) {
                                Text("🎬 ${anime?.episodes ?: "?"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Eps / ${anime?.status?.take(3) ?: "?"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Similar Anime First
                    if (similar.isNotEmpty()) {
                        Text(Strings.get("similar_anime", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 16.dp)
                        ) {
                            items(similar, key = { it.id }) { sim ->
                                Box(modifier = Modifier.width(140.dp)) {
                                    AnimeCard(anime = sim, onClick = { viewModel.selectAnime(sim) })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Community Comments Generation as Carousel
                    Text(Strings.get("community_vibes", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (comments.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.loadCommunityComments() },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Text(Strings.get("generate_hot_takes", language), style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 16.dp)
                        ) {
                            items(comments!!, key = { it.hashCode() }) { review ->
                                Card(
                                    modifier = Modifier.width(300.dp).height(200.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                                        if (review.isSystemMessage) {
                                            Text(review.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (review.avatarUrl != null) {
                                                    AsyncImage(
                                                        model = review.avatarUrl,
                                                        contentDescription = "Avatar",
                                                        modifier = Modifier.size(32.dp).clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                review.author.take(1).uppercase(),
                                                                color = MaterialTheme.colorScheme.onPrimary,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(review.author, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                review.content, 
                                                style = MaterialTheme.typography.bodyMedium, 
                                                color = MaterialTheme.colorScheme.onSecondaryContainer, 
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .verticalScroll(rememberScrollState())
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    androidx.compose.material.icons.Icons.Default.Favorite, 
                                                    contentDescription = "Likes", 
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "${review.likes}", 
                                                    style = MaterialTheme.typography.labelMedium, 
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Full Synopsis moved to the bottom
                    if (!anime?.description.isNullOrBlank()) {
                        Text(Strings.get("synopsis", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            anime?.description ?: "", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    }
                }
            }

            // Floating Custom Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallFloatingActionButton(
                    onClick = onBackClick,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                val isFavorite = favorites.any { it.id == anime?.id }

                SmallFloatingActionButton(
                    onClick = { anime?.let { viewModel.toggleFavorite(it) } },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = "Favorite"
                    )
                }
            }
        }
    }
}
