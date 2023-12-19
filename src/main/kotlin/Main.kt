import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

data class Client(val id: Int)

/**
 * @param [chairs] is a channel representing seats in a hairdresser's chair. The channel is limited to 5 places.
 * @param [barber] this is a coroutine representing the work of a hairdresser. Inside barberWork(), the barber accepts clients from the channel and simulates a haircut.
 */
class BarberShop {
    private val chairs = Channel<Client>(5)
    private val barber = GlobalScope.launch { barberWork() }

    /**
     * @param [visit] function that customers call to visit the hair salon. If the barber is sleeping, they wake him up, otherwise the client simply sits down in a chair if there are free seats.
     */
    fun visit(client: Client) {
        if (!barber.isActive) {
            println("Barber is sleeping, waking him up...")
            barber.start()
        }

        if (!chairs.trySend(client).isSuccess) {
            println("No available chairs, client ${client.id} is leaving.")
        }
    }

    /**
     * for (client in chairs) - The barber is waiting for clients from the chairs channel.
     * finally The block is called when all clients have been served. In this block, the chairs channel is closed to inform all clients that no more clients will be accepted.
     */
    private suspend fun barberWork() {
        try {
            for (client in chairs) {
                println("Barber is cutting hair for client ${client.id}")
                delay(2000) // imitation of the time required for a haircut.
            }
        } finally {
            println("Barber has finished all haircuts.")
            chairs.close() // Close the channel when the barber is done
        }
    }
}

fun main() = runBlocking {
    val barberShop = BarberShop()

    /**
     * 10 clients are generated (from 0 to 9) with a random delay from 1 to 5 seconds before visiting the hairdresser
     */
    repeat(10) { clientId ->
        launch {
            delay((1..5).random().toLong() * 1000) // Random delay for clients
            val client = Client(clientId)
            println("Client ${client.id} enters the barber shop.")
            barberShop.visit(client)
        }
    }

    /**
     * The program waits 8 seconds (8000 ms) to allow some clients to enter the hairdresser.
     * Then a message is displayed that new clients will no longer be accepted.
     */
    delay(15000)
    println("No more new clients will be accepted.")
    coroutineContext.cancelChildren() // Cancel all coroutines
}

//fun main() = application {
//
//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "Barber Shop Simulation",
//    ) {
//        MaterialTheme {
//            //Start app
//
//        }
//    }
//}
