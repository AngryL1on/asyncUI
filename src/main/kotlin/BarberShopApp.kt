import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() = application {
    val log = mutableStateListOf<String>()
    val chairsState = mutableStateListOf(false, false, false, false, false)
    val barberState = mutableStateOf(false)
    val barberShop = BarberShop(log, chairsState, barberState)

    // Running a coroutine to add clients
    GlobalScope.launch {
        repeat(10) { clientId ->
            delay((1..5).random().toLong() * 1000) // Random delay for clients
            val client = Client(clientId)
            log.add("Client ${client.id} enters the barber shop.")
            barberShop.visit(client)
        }
        delay(15000) // Waiting a while before announcing closure
        log.add("No more new clients will be accepted.")
        coroutineContext.cancelChildren() // Terminate all child coroutines
    }

    // Launch window
    Window(
        onCloseRequest = ::exitApplication,
        title = "Barber Shop Simulation",
    ) {
        MaterialTheme {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Barber Shop Simulation", style = MaterialTheme.typography.h5)

                // Visualization of a barber's chair
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    if (barberState.value) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Person, contentDescription = "Barber at work")
                        }
                    } else {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Close, contentDescription = "Barber's empty chair")
                        }
                    }
                }

                // Visualization of queue seats
                Row {
                    chairsState.forEachIndexed { index, occupied ->
                        if (occupied) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Person, contentDescription = "Client in waiting chair $index")
                            }
                        } else {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Close, contentDescription = "Empty waiting chair $index")
                            }
                        }
                    }
                }

                // Log of events
                LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    items(log.size) { index ->
                        Text(log[index])
                    }
                }
            }
        }
    }
}
