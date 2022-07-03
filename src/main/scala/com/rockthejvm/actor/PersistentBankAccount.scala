package com.rockthejvm.actor
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

// A single bank account
class PersistentBankAccount {
  /*
    - fault tolerance
    - audience
  */

  // command = messages
  sealed trait Command
  case class CreateBankAccount(user:String,currency:String, initialBalance:Double,replyTo:ActorRef[Response])extends Command
  case class UpdateBalance(id:String, currency: String, amount:Double /*can be 0*/, replyTo:ActorRef[Response]) extends Command
  case class GetBankAccount(id:String,replyTo:ActorRef[Response]) extends Command

  // events = to persist to Cassandra
  trait Event
  case class BankAccountCreated(bankAccount: BankAccount) extends Event
  case class BalanceUpdated(amount:Double) extends Event

  // state
  case class BankAccount(id:String,user:String,currency:String,balance: Double)

  // response
  sealed trait Response
  case class BankAccountCreatedResponse(id:String) extends Response
  case class BankAccountUpdatedResponse(maybeBankAccount: Option[BankAccount]) extends Response
  case class GetBankAccountResponse(maybeBankAccount: Option[BankAccount]) extends Response

  // command handler = message handler => persist an event
  val commandHandler : (BankAccount,Command) => Effect[Event,BankAccount] = (state,command) =>
    command match {
      case  CreateBankAccount(user, currency, initialBalance, replyTo) =>
        val id =  state.id
        Effect
          .persist(BankAccountCreated(BankAccount(id,user,currency,initialBalance)))
          .thenReply(replyTo)(_ => BankAccountCreatedResponse(id))
    }

  // event handler => update state
  val eventHandler: (BankAccount,Event) => BankAccount = ???

  // state
  def apply (id:String) : Behavior[Command] =
    EventSourcedBehavior[Command,Event,BankAccount](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = BankAccount(id,"","",0.0), // unused
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
}
