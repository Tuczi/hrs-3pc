package put.swn

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import put.swn.business.actor.InitVoting
import put.swn.business.actor.MemberActor
import put.swn.business.actor.MemberActorName


fun main(args: Array<String>) {
    val system = ActorSystem.create("three-phase-commit")
    val size = 3
    val actors = (0..size).map { system.actorOf(Props.create(MemberActor::class.java, it, size), MemberActorName(it)) }

    Thread.sleep(1000)
    actors[0].tell(InitVoting(), ActorRef.noSender())
}
