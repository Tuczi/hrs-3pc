package put.swn.threepc

import akka.actor.ActorPath
import akka.actor.UntypedActor
import akka.pattern.Patterns
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by tkuczma on 19.12.15.
 */
fun CommitSiteActorName(number: Int): String {
    return "CommitSiteActor_$number"
}

class CommitSiteActor(val id: Int, val size: Int) : UntypedActor() {
    val t = Timeout(Duration.create(10, TimeUnit.SECONDS))
    val actors = (0..size).filter { it != id }.map { context.system().actorFor("user/"+CommitSiteActorName(it)) }

    override fun onReceive(message: Any?) {
        if (message is StartMessage) {
            //TODO Await and onFailure

            val CanCommitRequestFutures = actors.map { Patterns.ask(it, CanCommit(), t) }
            val CanCommitRequestResults = CanCommitRequestFutures.map { Await.result(it, t.duration()) }

            val PreCommitRequestFutures = actors.map { Patterns.ask(it, PreCommit(), t) }
            val PreCommitRequestResults = PreCommitRequestFutures.map { Await.result(it, t.duration()) }

            val DoCommitRequestFutures = actors.map { Patterns.ask(it, DoCommit(), t) }
            val DoCommitRequestResults = DoCommitRequestFutures.map { Await.result(it, t.duration()) }

            println("Done")
        } else if (message is Commit) {

            sender.tell(Confirm(), self)

        }
    }

}
