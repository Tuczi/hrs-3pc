package put.swn.threepc

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor
import akka.dispatch.OnSuccess
import akka.pattern.Patterns
import akka.util.Timeout
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    val system = ActorSystem.create("helloakka")
    val testActor = system.actorOf(Props.create(TestActor::class.java), "TestActor_1")

    val t = Timeout(Duration.create(5, TimeUnit.SECONDS));
    val future = Patterns.ask(testActor, Message("Hello, world!"), t)

    future.onSuccess(PrintResult<Any>(), system.dispatcher())//onComplete, onSuccess, onFailure
}

class PrintResult<T> : OnSuccess<T>() {
    override fun onSuccess(result: T) {
        println("Sucess. Result: " + result)
    }
}

class TestActor : UntypedActor() {
    override fun onReceive(message: Any?) {
        if (message is Message) {
            println(message.m)
            sender.tell("ok", self)
        }
    }
}

class Message(val m: String);
