package put.swn.business.actor

import akka.actor.ActorRef
import akka.actor.Kill
import akka.actor.Props
import akka.actor.UntypedActor
import put.swn.threepc.*
import scala.concurrent.duration.Duration
import scala.util.Random
import java.util.concurrent.TimeUnit

/**
 * Created by tkuczma on 02.01.16.
 */
internal class MemberActor(val id: Int, val size: Int) : UntypedActor() {
    val actors = (0..size).filter { it != id }.map { context.system().actorFor("user/" + MemberActorName(it)) }
    var threepcActor: ActorRef? = null

    override fun onReceive(message: Any?): Unit {
        when (message) {
        //Voting moderator
            is InitVoting -> {
                threepcActor = context.system().actorOf(Props.create(CommitSiteActor::class.java, id, size, self, ::decision), CommitSiteActorName(id))

                actors.forEach { it.tell(Init3pc(), self) }
                println("Voting inited.")
                context.system().scheduler().scheduleOnce(Duration.create(5, TimeUnit.SECONDS), { threepcActor?.tell(StartMessage(), self); println("StartVoting") }, context.dispatcher())
            }

            is Init3pc -> {
                threepcActor = context.system().actorOf(Props.create(CommitSiteActor::class.java, id, size, self, ::decision), CommitSiteActorName(id))
            }

            is Confirm -> {
                println("Voting passed")
                threepcActor?.tell(Kill.getInstance(), self)
            }

            is Abort -> {
                println("Voting did not passed")
                threepcActor?.tell(Kill.getInstance(), self)
            }
        }
    }
}

fun MemberActorName(number: Int): String? {
    return "MemberActor_$number"
}

fun decision(id: Int): Boolean {
    val p = Random().nextGaussian()

    return Math.abs(p) >= 0.01
}

class InitVoting
class Init3pc
