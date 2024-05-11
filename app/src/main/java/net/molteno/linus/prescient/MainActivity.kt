package net.molteno.linus.prescient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.fetchHp30
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree());
        enableEdgeToEdge()
        setContent {
            PrescientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var fetch by remember { mutableStateOf(false) }
    var currentHp by remember { mutableStateOf<HpEntry?>(null) }

    Column {
        LaunchedEffect(fetch) {
            launch(Dispatchers.IO) {
                val hp30 = fetchHp30()
                currentHp = hp30?.maxBy { it.time }
            }
        }
        Text(
            text = "Hello $name! current hp30: $currentHp",
            modifier = modifier
        )
        Button(onClick = { fetch = true }) {
            Text("Fetch hp30")
        }
        HpDisplay(hp30 = currentHp)
    }
}

@Composable
fun HpDisplay(hp30: HpEntry?) {
    if (hp30 == null) return
    Row {
        Text("From ${hp30.time} to ${hp30.time.plusMinutes(30).toLocalTime()} = ${hp30.hp30}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrescientTheme {
        Greeting("Android")
    }
}