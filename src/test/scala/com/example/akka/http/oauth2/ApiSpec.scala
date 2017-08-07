package com.example.akka.http.oauth2

import java.time.LocalDate

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.{Authorization, HttpChallenges, OAuth2BearerToken}
import akka.http.scaladsl.server.{AuthenticationFailedRejection, AuthorizationFailedRejection}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActor.{KeepRunning, NoAutoPilot}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.example.akka.http.oauth2.Api.User
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt


class ApiSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val authFailedRejection = AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, HttpChallenges.oAuth2("sync-secured"))
  val authWrongRejection = AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, HttpChallenges.oAuth2("sync-secured"))
  val asyncAuthFailedRejection = AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, HttpChallenges.oAuth2("async-secured"))
  val asyncAuthWrongRejection = AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, HttpChallenges.oAuth2("async-secured"))

  val now = LocalDate.now()
  val tomorrow = now.plusDays(1)
  val yesterday = now.minusDays(1)

  val expiresTomorrow = AwesomeToken("Jane Doe", tomorrow)
  val expiresYesterday = AwesomeToken("John Doe", yesterday)


  val fakeSecurityActor = TestProbe()
  fakeSecurityActor.setAutoPilot(
    (sender: ActorRef, msg: Any) => msg match {
      case rawToken: String =>
        val possibleToken = AwesomeToken(rawToken)
        possibleToken match {
          case Some(token) if token.name == "Jane Doe" =>
            sender ! Some(User(token.name))
            NoAutoPilot
          case _ =>
            sender ! None
            KeepRunning
        }

    }
  )

  implicit val timeout = Timeout(1000.second)
  "ApiSpec.route" can {

    val route = Api.route(fakeSecurityActor.ref)
    "secured route" should {


      "fail without token" in {
        Get("/secured") ~> route ~> check {
          rejections should contain(authFailedRejection)
        }
      }

      "fail with invalid token" in {
        Get("/secured") ~> Authorization(OAuth2BearerToken("theToken")) ~> route ~> check {
          rejections should contain(authWrongRejection)
        }
      }

      "fail with expired token" in {
        Get("/secured") ~> Authorization(OAuth2BearerToken(expiresYesterday.asBearerToken())) ~> route ~> check {
          rejections should contain(authWrongRejection)
        }
      }

      "completes successful" in {
        Get("/secured") ~> Authorization(OAuth2BearerToken(expiresTomorrow.asBearerToken())) ~> route ~> check {
          responseAs[String] should equal("Jane Doe")
        }
      }
    }

    "asyncSecured route" should {

      "fail without token" in {
        Get("/asec") ~> route ~> check {
          rejections should contain(asyncAuthFailedRejection)
        }
      }

      "fail with invalid token" in {
        Get("/asec") ~> Authorization(OAuth2BearerToken("theToken")) ~> route ~> check {
          rejections should contain(asyncAuthWrongRejection)
        }
      }

      "fail with expired token" in {
        Get("/asec") ~> Authorization(OAuth2BearerToken(expiresYesterday.asBearerToken())) ~> route ~> check {
          rejections should contain(asyncAuthWrongRejection)
        }
      }

      "completes successful" in {
        Get("/asec") ~> Authorization(OAuth2BearerToken(expiresTomorrow.asBearerToken())) ~> route ~> check {
          responseAs[String] should equal("Jane Doe")
        }
      }
    }

  }
}
