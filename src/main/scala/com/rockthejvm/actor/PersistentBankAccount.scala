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
      case  CreateBankAccount(user, currency, initialBalance, bank) =>
        val id =  state.id
        /*
          - Bank creates me
          - Bank sends me CreateBankAccount
          - I persist BankAccountCreated
          - I update my state
          - Reply back to bank with the BankAccountCreatedResponse
          - The bank surfaces the response to the Http server
         */
        Effect
          .persist(BankAccountCreated(BankAccount(id,user,currency,initialBalance))) // persisted into Cassandra
          .thenReply(bank)(_ => BankAccountCreatedResponse(id))
      case UpdateBalance(_, _, amount, bank) =>
        val newBalance = state.balance + amount
        if(newBalance < 0)
          Effect.reply(bank)(BankAccountUpdatedResponse(None))
        else
          Effect
            .persist(BalanceUpdated(amount))
            .thenReply(bank)(newState => BankAccountUpdatedResponse(Some(newState)))
      case GetBankAccount(_, bank) =>
        Effect.reply(bank)(GetBankAccountResponse(Some(state)))
    }

  // event handler => update state
  val eventHandler: (BankAccount,Event) => BankAccount = (state,event) =>
    event match {
      case BankAccountCreated(bankAccount) =>
        bankAccount
      case BalanceUpdated(amount) =>
        state.copy(balance = state.balance + amount)
    }

  // state
  def apply (id:String) : Behavior[Command] =
    EventSourcedBehavior[Command,Event,BankAccount](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = BankAccount(id,"","",0.0), // unused
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
}
