package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.todolist.ui.tasks.TaskListScreen
import com.example.todolist.ui.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the entire To-Do app.
 *
 * `@AndroidEntryPoint` lets any Composable below this Activity obtain a
 * Hilt-provided ViewModel via `hiltViewModel()`. The TaskViewModel chain
 * (Repository → DAO → Database → Hilt module) is wired by Hilt at install
 * time, so the screen just calls `TaskListScreen()` and gets a live VM.
 *
 * `enableEdgeToEdge()` lets content draw behind the status & nav bars for
 * a modern, immersive look. Compose handles inset padding automatically
 * via the Scaffold's innerPadding inside TaskListScreen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    TaskListScreen()
                }
            }
        }
    }
}
