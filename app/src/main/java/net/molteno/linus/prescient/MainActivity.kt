package net.molteno.linus.prescient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.ui.theme.PrescientTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
fun Greeting(name: String, modifier: Modifier = Modifier, viewModel: TestViewModel = hiltViewModel()) {
    val currentHp by viewModel.hp30.collectAsState()
    val solarRegions by viewModel.solarRegions.collectAsState()

    Column {
        Text(
            text = "fetched ${solarRegions?.count()} solar regions",
            modifier = modifier
        )
        HpDisplay(hp30 = currentHp?.maxBy { it.time })
    }
}

@Composable
fun HpDisplay(hp30: HpEntry?) {
    if (hp30 == null) return
    Column {
        Text("rom ${hp30.time} to ${hp30.time.plusMinutes(30).toLocalTime()} = ${hp30.hp30}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrescientTheme {
        Greeting("Android")
    }
}