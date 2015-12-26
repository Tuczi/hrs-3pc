package put.swn.threepc

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props


fun main(args: Array<String>) {
    val system = ActorSystem.create("three-phase-commit")
    val size = 10
    val actors = (0..size).map { system.actorOf(Props.create(CommitSiteActor::class.java, it, size, ::decision), CommitSiteActorName(it)) }

    Thread.sleep(1000)
    actors[0].tell(StartMessage(), ActorRef.noSender())
}

fun decision(id:Int): Boolean {
    return id == 1
}