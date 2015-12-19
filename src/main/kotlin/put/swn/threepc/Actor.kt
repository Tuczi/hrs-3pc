package put.swn.threepc


import akka.actor.Cancellable
import akka.actor.UntypedActor
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by tkuczma on 19.12.15.
 */
fun CommitSiteActorName(number: Int): String {
    return "CommitSiteActor_$number"
}

class CommitSiteActor(val id: Int, val size: Int) : UntypedActor() {
    val actors = (0..size).filter { it != id }.map { context.system().actorFor("user/" + CommitSiteActorName(it)) }
    var counter = 0

    init {
        context.become { canCommit(it, null) }
    }

    override fun onReceive(message: Any?) {
    }

    fun canCommit(message: Any?, timeout: Cancellable?) {
        if (message is StartMessage) {
            println("CanCommit StartMessage")
            actors.forEach { it.tell(CanCommit(), self) }
            counter = 0
            context.become { preCommit(it, timeout()) }
        } else if (message is CanCommit) {
            println("CanCommit CanCommit")
            sender.tell(Confirm(), self)
            context.become { preCommit(it, timeout()) }
        } else if (message is Confirm) {
            println("CanCommit Confirm")
            if (++counter == size - 1)
                timeout?.cancel()
        } else if (message is Abort) {
            println("CanCommit Abort")

        }
    }

    fun preCommit(message: Any, timeout: Cancellable) {
        if (message is Confirm) {
            println("PreCommit Confirm")
            if (++counter == size - 1) {
                timeout.cancel()

                actors.forEach { it.tell(PreCommit(), self) }
                counter = 0
                context.become { doCommit(it, timeout()) }
            }
        } else if (message is Abort) {
            println("PreCommit Abort")

        } else if (message is PreCommit) {
            println("PreCommit PreCommit")
            timeout.cancel()

            sender.tell(Confirm(), self)
            context.become { doCommit(it, timeout()) }
        }
    }

    fun doCommit(message: Any, timeout: Cancellable) {
        if (message is Confirm) {
            println("DoCommit Confirm")
            if (++counter == size - 1) {
                timeout.cancel()

                actors.forEach { it.tell(DoCommit(), self) }
                counter = 0
                context.become { canCommit(it, timeout()) }
            }
        } else if (message is Abort) {
            println("DoCommit Abort")

        } else if (message is DoCommit) {
            println("DoCommit DoCommit")
            timeout.cancel()

            sender.tell(Confirm(), self)
            context.become { canCommit(it, null) }
        }
    }

    fun timeout(): Cancellable {
        return context.system().scheduler().scheduleOnce(Duration.create(5, TimeUnit.NANOSECONDS), self, Abort(), context.dispatcher(), null)
    }
}
