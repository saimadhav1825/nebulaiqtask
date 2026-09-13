package com.app.nebulaiqtask

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.app.nebulaiqtask.presentation.navigation.NebulaNavGraph
import com.app.nebulaiqtask.presentation.theme.NebulaTheme
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    KoinContext {
        NebulaTheme {
            NebulaNavGraph()
        }
    }
}