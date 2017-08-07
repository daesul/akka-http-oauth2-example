package com.example.akka.http.oauth2

import java.time.LocalDate

import org.scalatest.{Matchers, WordSpec}

class AwesomeTokenSpec extends WordSpec with Matchers {

  "AwesomeToken" can {

      val invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoidGVzdDIiLCJleGEiOiIyMDE4LTA4LTE0In0=.uNwPPiP5WLzTtnWdhYeg1Jdfupwx5Rb7R6/C9iUUAg0="
      val today = LocalDate.of(2017, 8, 14)
      val yesterday = LocalDate.of(2017, 8, 13)
      val tomorrow = LocalDate.of(2017, 8, 14)

      val expireYesterday = AwesomeToken("test1", yesterday)
      val expireTommorow = AwesomeToken("test2", tomorrow)

    "isExpired" should {

      "be false if token expires tomorrow" in {
        expireTommorow.isExpired(today) shouldBe false
      }

      "be true if token expires yesterday" in {
        expireYesterday.isExpired(today) shouldBe true
      }
    }

    "isNotExpired" should {

      "be true if token expires tomorrow" in {
        expireTommorow.isNotExpired(today) shouldBe true
      }

      "be false if token expires yesterday" in {
        expireYesterday.isNotExpired(today) shouldBe false
      }
    }

    "apply with tokenAsString" should {

      "return Some" in {
        val token = expireTommorow.asBearerToken
        AwesomeToken.apply(token).get should equal(expireTommorow)
      }

      "return None" in {
        AwesomeToken.apply(invalidToken) should be(None)
      }
    }

  }


}
