package put.swn.threepc

/**
 * Created by tkuczma on 19.12.15.
 *
 * TODO enum class
 */

open class Message;
class StartMessage : Message();

abstract class Commit : Message();
class CanCommit : Commit();
class PreCommit : Commit();
class DoCommit : Commit();

abstract class Status : Message();
class Abort : Status();
class Confirm : Status();
class Timeout: Message();
