package com.example.myway.screens.modulo3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myway.R
import com.example.myway.data.FavoritesRepository
import com.example.myway.data.models.FavoritePlace
import com.example.myway.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun Favoritos(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { FavoritesRepository(context) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }

    val favorites by if (searchQuery.isEmpty()) {
        repository.getAllFavorites().collectAsState(initial = emptyList())
    } else {
        repository.searchFavorites(searchQuery).collectAsState(initial = emptyList())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2),
            contentDescription = stringResource(R.string.fondo_app),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Encabezado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flecha),
                    contentDescription = stringResource(R.string.volver),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(35.dp)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = stringResource(R.string.favoritos),
                    color = Blanco,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Image(
                    painter = painterResource(id = R.drawable.icono_perfil),
                    contentDescription = stringResource(R.string.perfil),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable { navController.navigate("perfil_ajustes") }
                )
            }

            // Subtítulo
            Text(
                text = stringResource(R.string.lugares_favoritos),
                color = Blanco,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(stringResource(R.string.buscar_favoritos), fontFamily = Nunito) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.buscar)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.limpiar)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco,
                    focusedBorderColor = Azul4,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Contenido principal
            when {
                favorites.isEmpty() && searchQuery.isEmpty() -> {
                    EmptyFavoritesState()
                }

                favorites.isEmpty() && searchQuery.isNotEmpty() -> {
                    NoResultsState(query = searchQuery)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = favorites, key = { it.id }) { favorite ->
                            FavoriteItemCard(
                                favorite = favorite,
                                onItemClick = {
                                    navController.navigate("detalles_lugar/${favorite.id}/${favorite.name}")
                                },
                                onDeleteClick = {
                                    scope.launch { repository.deleteFavorite(favorite.id) }
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
fun FavoriteItemCard(
    favorite: FavoritePlace,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (favorite.photoUrl != null) {
                AsyncImage(
                    model = java.io.File(favorite.photoUrl),
                    contentDescription = favorite.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_favorite_outline)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Azul4.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Azul4,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.name,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (favorite.address != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = favorite.address,
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.1f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite_filled),
                    contentDescription = stringResource(R.string.eliminar_favoritos),
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyFavoritesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_favorite_outline),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Blanco.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_favoritos),
            color = Blanco,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_favoritos_descripcion),
            color = Blanco.copy(alpha = 0.8f),
            fontFamily = Nunito,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NoResultsState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Blanco.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_resultados),
            color = Blanco,
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_resultados_descripcion, query),
            color = Blanco.copy(alpha = 0.8f),
            fontFamily = Nunito,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
