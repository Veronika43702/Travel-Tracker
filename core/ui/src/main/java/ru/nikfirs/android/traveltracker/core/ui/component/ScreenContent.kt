package ru.nikfirs.android.traveltracker.core.ui.component

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.nikfirs.android.traveltracker.core.ui.extension.clickableOnce
import ru.nikfirs.android.traveltracker.core.ui.model.IconType
import ru.nikfirs.android.traveltracker.core.ui.model.TopBarActionModel
import ru.nikfirs.android.traveltracker.core.ui.navigation.BottomNavBarRoute
import ru.nikfirs.android.traveltracker.core.ui.navigation.getBottomNavBarItems
import ru.nikfirs.android.traveltracker.core.ui.navigation.getSelectedIcon
import ru.nikfirs.android.traveltracker.core.ui.navigation.getUnselectedIcon
import ru.nikfirs.android.traveltracker.core.ui.theme.AppTheme

@Composable
fun Screen(
    topTitle: String = "",
    navigateBack: (() -> Unit)? = null,
    actions: List<TopBarActionModel> = emptyList(),
    bottomNavRouteRoute: Any? = null,
    navigateRoute: (Any) -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            if (topTitle.isNotBlank()) {
                TopBar(topTitle, navigateBack, actions)
            }
        },
        bottomBar = {
            if (bottomNavRouteRoute != null) {
                BottomBar(
                    selectedRoute = bottomNavRouteRoute,
                    onRouteClick = navigateRoute,
                )
            }
        },
        content = { padding ->
            Box(
                content = { content(padding) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    )
}

@Composable
private fun TopBar(
    title: String,
    navigateBack: (() -> Unit)? = null,
    actions: List<TopBarActionModel> = emptyList(),
) {
    val roundedCornerForIcons = 12.dp
    val horizontalPaddingFromSides = 16.dp
    val iconSize = 24.dp
    val iconPadding = 6.dp
    val iconPaddingHorExtra = 4.dp

    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .shadow(4.dp)
    ) {
        if (navigateBack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = horizontalPaddingFromSides)
                    .clip(RoundedCornerShape(roundedCornerForIcons))
                    .clickableOnce { navigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(iconPadding)
                        .padding(horizontal = iconPaddingHorExtra)
                        .size(iconSize)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (actions.size == 2) 92.dp else 60.dp)
        )
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = horizontalPaddingFromSides),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (actions.size > 2) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(roundedCornerForIcons))
                        .clickableOnce { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(iconPadding)
                            .padding(iconPaddingHorExtra)
                            .size(iconSize)
                    )

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        actions.filter { !it.title.isNullOrBlank() }.forEach { action ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(MaterialTheme.shapes.extraLarge),
                                text = { Text(action.title ?: "") },
                                onClick = {
                                    menuExpanded = false
                                    action.onClick()
                                }
                            )
                        }
                    }
                }
            } else {
                actions.forEach { action ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(roundedCornerForIcons))
                            .clickableOnce { action.onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        when (action.icon) {
                            is IconType.DrawableRes -> Icon(
                                painter = painterResource(id = action.icon.resId),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(iconPadding)
                                    .padding(
                                        horizontal = if (actions.size < 2) {
                                            iconPaddingHorExtra
                                        } else 0.dp
                                    )
                                    .size(iconSize)
                            )

                            is IconType.VectorIcon -> Icon(
                                imageVector = action.icon.imageVector,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(iconPadding)
                                    .padding(
                                        horizontal = if (actions.size < 2) {
                                            iconPaddingHorExtra
                                        } else 0.dp
                                    )
                                    .size(iconSize)
                            )

                            null -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    selectedRoute: Any,
    onRouteClick: (Any) -> Unit,
) {
    val items = getBottomNavBarItems()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        items.forEach { item ->
            val icon = if (selectedRoute == item) {
                item.getSelectedIcon()
            } else {
                item.getUnselectedIcon()
            }
            NavigationBarItem(
                selected = selectedRoute == item,
                onClick = { onRouteClick(item) },
                icon = {
                    when (icon) {
                        is IconType.DrawableRes -> Icon(
                            painter = painterResource(id = icon.resId),
                            contentDescription = null
                        )

                        is IconType.VectorIcon -> Icon(
                            imageVector = icon.imageVector,
                            contentDescription = null
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                alwaysShowLabel = false,
                modifier = Modifier.height(72.dp)
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ScreenPreview1() {
    AppTheme {
        Screen(
            topTitle = "title",
            bottomNavRouteRoute = BottomNavBarRoute.Home,
            navigateBack = {}
        ) {
            Text("content")
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ScreenPreview2() {
    AppTheme {
        Column {
            TopBar(
                title = "Top Bar with buttons on both side",
                actions = listOf(
                    TopBarActionModel(
                        IconType.VectorIcon(Icons.Default.Search),
                        "search"
                    ) {},
                    TopBarActionModel(
                        IconType.VectorIcon(Icons.Default.Settings),
                        "settings"
                    ) {},
                    TopBarActionModel(
                        IconType.VectorIcon(Icons.Default.Settings),
                        "settings"
                    ) {},
                ),
                navigateBack = {}
            )
            TopBar(
                title = "Empty Top Bar",
            )
            TopBar(
                title = "Top Bar with buttons on both side",
                actions = listOf(
                    TopBarActionModel(
                        IconType.VectorIcon(Icons.Default.Search)
                    ) {},
                    TopBarActionModel(
                        IconType.VectorIcon(Icons.Default.Settings)
                    ) {},
                ),
                navigateBack = {}
            )
            TopBar(
                title = "Top Bar with buttons on both side",
                actions = listOf(
                    TopBarActionModel(
                        IconType.VectorIcon(Icons.Default.Settings)
                    ) {},
                ),
                navigateBack = {}
            )
            Spacer(Modifier.height(100.dp))
        }
    }
}