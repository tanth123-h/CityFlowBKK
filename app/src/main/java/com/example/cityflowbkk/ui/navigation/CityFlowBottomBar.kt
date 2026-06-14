package com.example.cityflowbkk.ui.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cityflowbkk.navigation.BottomNavItem
import com.example.cityflowbkk.ui.icons.HomeIconGraphic

@Composable
fun CityFlowBottomBar(
    selectedItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = item == selectedItem,
                onClick = { onItemClick(item) },
                icon = {
                    HomeIconGraphic(
                        icon = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(item.label)
                },
            )
        }
    }
}
