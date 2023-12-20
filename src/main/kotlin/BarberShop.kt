import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BarberShop(
    val log: SnapshotStateList<String>,
    val chairsState: SnapshotStateList<Boolean>,
    val barberState: MutableState<Boolean>
) {
    private val chairs = Channel<Client>(5)
    private val barber = GlobalScope.launch { barberWork() }

    fun visit(client: Client) {
        if (!barber.isActive) {
            log.add("Barber is sleeping, waking him up...")
            barber.start()
        }

        val emptyChairIndex = chairsState.indexOfFirst { !it }
        if (emptyChairIndex != -1) {
            chairsState[emptyChairIndex] = true // The client takes the chair
            GlobalScope.launch {
                if (chairs.trySend(client).isSuccess) {
                    log.add("Client ${client.id} takes a seat in the waiting area.")
                } else {
                    log.add("No available chairs, client ${client.id} is leaving.")
                    chairsState[emptyChairIndex] = false // Free the chair if the client leaves
                }
            }
        } else {
            log.add("No available chairs, client ${client.id} is leaving.")
        }
    }

    private suspend fun barberWork() {
        try {
            for (client in chairs) {
                barberState.value = true
                log.add("Barber is cutting hair for client ${client.id}")
                chairsState[chairsState.indexOfFirst { it }] = false // Free up space in the queue
                delay(2000)
                barberState.value = false
                log.add("Barber finished haircut and client ${client.id} left the barber shop.")
            }
        } finally {
            log.add("Barber has finished all haircuts.")
            chairs.close()
        }
    }
}