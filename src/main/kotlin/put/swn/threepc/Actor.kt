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
    var timeout: Cancellable? = null

    init {
        context.become { canCommit(it) }
    }

    override fun onReceive(message: Any?) {
    }

    fun reset(timeout: Cancellable? = null) {
        this.counter = 0
        this.timeout = timeout
    }

    fun checkCounter() = (++counter == size - 1)

    fun timeout(): Cancellable = context.system().scheduler().scheduleOnce(Duration.create(5, TimeUnit.SECONDS), self, Abort(), context.dispatcher(), null)

    fun canCommit(message: Any?): Unit = when (message) {
        is StartMessage -> {
            println("CanCommit StartMessage")
            actors.forEach { it.tell(CanCommit(), self) }

            reset(timeout())
            context.become { preCommit(it) }
        }

        is Confirm -> {
            println("CanCommit Confirm")
            if (checkCounter())
                timeout?.cancel()
        }

        is Abort -> {
            println("CanCommit Abort")
        }

        is CanCommit -> {
            println("CanCommit CanCommit")
            sender.tell(Confirm(), self)

            reset(timeout())
            context.become { preCommit(it) }
        }
    }

    fun preCommit(message: Any): Unit = when (message) {
        is Confirm -> {
            println("PreCommit Confirm")
            if (checkCounter()) {
                timeout?.cancel()

                actors.forEach { it.tell(PreCommit(), self) }
                reset(timeout())
                context.become { doCommit(it) }
            }
        }

        is Abort -> {
            println("PreCommit Abort")
        }

        is PreCommit -> {
            println("PreCommit PreCommit")
            timeout?.cancel()

            sender.tell(Confirm(), self)
            reset(timeout())
            context.become { doCommit(it) }
        }
    }


    fun doCommit(message: Any): Unit = when (message) {
        is Confirm -> {
            println("DoCommit Confirm")
            if (++counter == size - 1) {
                timeout?.cancel()

                actors.forEach { it.tell(DoCommit(), self) }
                reset(timeout())
                context.become { canCommit(it) }
            }
        }

        is Abort -> {
            println("DoCommit Abort")

        }

        is DoCommit -> {
            println("DoCommit DoCommit")
            timeout?.cancel()

            sender.tell(Confirm(), self);
            reset()
            context.become { canCommit(it) }
        }
    }
}
