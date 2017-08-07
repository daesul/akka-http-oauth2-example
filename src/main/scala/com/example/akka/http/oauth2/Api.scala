package com.example.akka.http.oauth2

import java.time.LocalDate

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.Provided
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future


class Api(securityActor : ActorRef) extends Actor{

  override def receive: Receive = Actor.emptyBehavior
}

object Api {
  final case class User(name: String)

  val name = "awesome-protected-api"

  def props(securityActor : ActorRef) = Props(new Api(securityActor : ActorRef))

  def route(securityActor : ActorRef)(implicit timeout: Timeout): Route = {
    import akka.http.scaladsl.server.Directives._

    def verifyToken(possibleToken: Option[AwesomeToken]) = possibleToken match {
      case Some(token) if token.isNotExpired() => possibleToken
      case _ => None
    }

    def authenticator(credentials: Credentials) = credentials match {
      case c @ Provided(token) =>
        val theToken = AwesomeToken(token)
        verifyToken(theToken)
      case _ => None
    }

   def asyncAuthenticator(credentials: Credentials) = credentials match {
      case c @ Provided(token) => (securityActor ? token).mapTo[Option[User]]
      case _ => Future.successful(None)

    }

    def secured =
      authenticateOAuth2("sync-secured", authenticator) { token =>
        path("secured"){
          complete(token.name)
        }
      }




    def asyncSecured = {
      authenticateOAuth2Async("async-secured", asyncAuthenticator ) { user =>
        path("asec") {
          complete(user.name)
        }
      }
    }
    secured ~ asyncSecured
  }
}




