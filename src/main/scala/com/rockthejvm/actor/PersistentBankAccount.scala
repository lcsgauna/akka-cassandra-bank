package com.rockthejvm.actor
import akka.actor.typed.ActorRef

// A single bank account
class PersistentBankAccount {
  /*
    - fault tolerance
    - audience
  */
  // command = messages
  sealed trait Command
  case class CreateBankAccount(user:String,currency:String, initialBalance:Double,replyTo:ActorRef[Response])
  case class UpdateBalance(id:String, currency: String, amount:Double /*can be 0*/, replyTo:ActorRef[Response)



  // events = to persist to Cassandra
  // state
  // response
  sealed trait Response

}
