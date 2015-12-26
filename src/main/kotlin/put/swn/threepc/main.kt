package put.swn.threepc

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import scala.util.Random


fun main(args: Array<String>) {
    val system = ActorSystem.create("three-phase-commit")
    val size = 10
    val actors = (0..size).map { system.actorOf(Props.create(CommitSiteActor::class.java, it, size, ::decision), CommitSiteActorName(it)) }

    Thread.sleep(1000)
    actors[0].tell(StartMessage(), ActorRef.noSender())
}

fun decision(id:Int): Boolean {
    val p = Random().nextGaussian()

    return Math.abs(p) >= 0.01
}