package put.swn.threepc

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor

fun main(args: Array<String>) {
    val system = ActorSystem.create("helloakka")
    val testActor = system.actorOf(Props.create(TestActor::class.java), "TestActor_1")

    testActor.tell(Message("Hello, world!"), ActorRef.noSender())
}

class TestActor : UntypedActor() {
    override fun onReceive(message: Any?) {
        if (message is Message) {
            println(message.m)
        }
    }
}

class Message(val m: String);
