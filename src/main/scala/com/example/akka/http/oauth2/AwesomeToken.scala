package com.example.akka.http.oauth2

import java.nio.charset.StandardCharsets
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.example.akka.http.oauth2.AwesomeToken._
import io.circe._
import io.circe.parser._


final case class AwesomeToken(name: String, exa: LocalDate) {

  def encodePayload(): String = {
    val rawPayload = Json.fromFields(Seq(
      ("name", Json.fromString(name)),
      ("exa", Json.fromString(exa.format(DateTimeFormatter.ISO_DATE)))
    ))
    encode(rawPayload.noSpaces)
  }

  def asBearerToken(): String = {
    val payload = encodePayload
    tokenHeader + "." + payload + "." + sign(tokenHeader, payload)
  }

  def isExpired(now: LocalDate = LocalDate.now()): Boolean = {
    exa isBefore now
  }

  def isNotExpired(now: LocalDate = LocalDate.now()): Boolean = {
    !isExpired(now)
  }

}

object AwesomeToken {

  val tokenHeader = encode("""{"alg":"HS256","typ":"JWT"}""")

  val algorihtm = "HmacSHA256"

  def sign(header: String, payload: String): String = {
    val secret = "secret"
    val raw = header + "." + payload
    val mac = Mac.getInstance(algorihtm)
    val secretKey = new SecretKeySpec(secret.getBytes, algorihtm)
    mac.init(secretKey)
    encode(mac.doFinal(raw.getBytes()))
  }

  private def encode(raw: String): String =
    encode(raw.getBytes(StandardCharsets.UTF_8))

  private def encode(bytes: Array[Byte]): String =
    Base64.getEncoder.encodeToString(bytes)

  private def decode(encoded: String): String =
    new String(Base64.getDecoder.decode(encoded), StandardCharsets.UTF_8)

  def apply(token: String): Option[AwesomeToken] = {
    import io.circe.java8.time._

    val tokenParts = token.split("\\.")
    if (valid(tokenParts)) {
      val payloadAsString = decode(tokenParts(1))
      val payloadAsJson: Json = parse(payloadAsString).getOrElse(Json.Null)
      val name = payloadAsJson.hcursor.get[String]("name").toOption.get
      val exa = payloadAsJson.hcursor.get[LocalDate]("exa").toOption.get
      Some(AwesomeToken(name,exa))
    } else None
  }

  private def unchanged(head: String, payload: String, signature: String) =
    signature == sign(head, payload)

  private def valid(tokenParts: Array[String]) = {
    tokenParts match {
      case Array(head, payload, signature) => unchanged(head, payload, signature)
      case _ => false
    }
  }

}
