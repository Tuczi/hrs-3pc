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

class CommitSiteActor(val id: Int, val size: Int, val decision: (id:Int) -> Boolean = {id:Int -> true}) : UntypedActor() {
    val actors = (0..size).filter { it != id }.map { context.system().actorFor("user/" + CommitSiteActorName(it)) }
    var counter = 0
    var timeout: Cancellable? = null
    var isCoordinator = false

    init {
        context.become { initial(it) }
    }

    override fun onReceive(message: Any?) {
    }

    fun reset(timeout: Cancellable? = null) {
        this.counter = 0
        this.timeout = timeout
    }

    fun checkCounter() = (++counter == size - 1)

    fun timeout(): Cancellable = context.system().scheduler().scheduleOnce(Duration.create(5, TimeUnit.SECONDS), self, Timeout(), context.dispatcher(), null)

    fun initial(message: Any?): Unit = when (message) {
        is StartMessage -> {
            println("Initial StartMessage")
            isCoordinator = true
            actors.forEach { it.tell(CanCommit(), self) }

            reset(timeout())
            context.become { waiting(it) }
        }

        is Confirm -> {
            println("Initial Confirm")
            if (checkCounter())
                timeout?.cancel()
        }

        is Abort -> {
            println("Initial Abort")
            timeout?.cancel()
            if(isCoordinator)
                actors.forEach { it.tell(Abort(), self) }
            context.become { aborted(it) }
        }

        is Timeout -> {
            println("Initial Abort")
            timeout?.cancel()
            if(isCoordinator)
                actors.forEach { it.tell(Abort(), self) }
            context.become { aborted(it) }
        }

        is CanCommit -> {
            if (decision(id)) {
                println("Initial CanCommit (YES)")
                sender.tell(Confirm(), self)
                reset(timeout())
                context.become { waiting(it) }
            } else {
                println("Initial CanCommit (NO)")
                sender.tell(Abort(), self)
                context.become { aborted(it) }
            }
        }
    }

    fun waiting(message: Any): Unit = when (message) {
        is Confirm -> {
            println("Waiting Confirm")
            if (checkCounter()) {
                timeout?.cancel()

                actors.forEach { it.tell(PreCommit(), self) }
                reset(timeout())
                context.become { prepared(it) }
            }
        }

        is Abort -> {
            println("Waiting Abort")
            timeout?.cancel()
            if(isCoordinator) {
                actors.forEach { it.tell(Abort(), self) }
            }
            context.become { aborted(it) }
        }

        is Timeout -> {
            println("Waiting Timeout")
            timeout?.cancel()
            if(isCoordinator) {
                actors.forEach { it.tell(Abort(), self) }
            }
            context.become { aborted(it) }
        }

        is PreCommit -> {
            println("Waiting PreCommit")
            timeout?.cancel()

            sender.tell(Confirm(), self)
            reset(timeout())
            context.become { prepared(it) }
        }
    }


    fun prepared(message: Any): Unit = when (message) {
        is Confirm -> {
            println("Prepared Confirm")
            if (++counter == size - 1) {
                timeout?.cancel()

                actors.forEach { it.tell(DoCommit(), self) }
                reset(timeout())
                context.become { commited(it) }
            }
        }

        is Abort -> {
            println("Prepared Abort")
            timeout?.cancel()
            if(isCoordinator) {
                actors.forEach { it.tell(Abort(), self) }
            }
            context.become { aborted(it) }
        }

        is Timeout -> {
            println("Prepared Timeout")
            timeout?.cancel()
            if(isCoordinator) {
                actors.forEach { it.tell(Abort(), self) }
                context.become { aborted(it) }
            } else {
                context.become { commited(it) }
            }

        }

        is DoCommit -> {
            println("Prepared DoCommit")
            timeout?.cancel()

            sender.tell(Confirm(), self);
            reset()
            context.become { commited(it) }
        }
    }

    fun commited(message: Any) = println("Commited")

    fun aborted(message: Any) = println("Aborted")

}
