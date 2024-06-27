package com.softwaremill.endpoints

import Database.*
import cats.effect.IO
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

object AnimalEndpoints:

  private val apiTag = "kitten"

  case class ErrorResponse(message: String)
//ServerEndpoint[R, F] - requirements (websockets, streams) | F effect type of the logic = scala.concurrent.Future or cats.effect.IO
//ServerEndpoint.Full[A, U, I, E, O, R, F]
//Endpoint[A, I, E, O, R]
//PublicEndpoint[I, E, O, R] - A security inputs = Unit
//serverLogic(f: I => F[Either[E, O]]
// the server logic of type I => F[Either[E, O]] for public endpoint
// the security logic of type A => F[Either[E, U]] and the main logic of type U => I => F[Either[E, O]]
//If either the security logic, or the main logic fails, an error of type E might be returned

  val kittensListing = endpoint.get
    .in("kittens")
    .errorOut(stringBody)
    .out(jsonBody[List[Kitten]])

  val kittensListingServerEndpoint: ServerEndpoint[Any, IO] =
    kittensListing.serverLogicSuccess(_ => IO.pure(Database.kittens))

  val kittensGet: Endpoint[Unit, Long, (StatusCode, ErrorResponse), (StatusCode, Kitten), Any] = endpoint.get
    .in("kitten")
    .in(path[Long]("id"))
    .errorOut(statusCode)
    .errorOut(jsonBody[ErrorResponse])
    .out(statusCode)
    .out(jsonBody[Kitten])

  val kittensGetServerEndpoint: ServerEndpoint[Any, IO] = kittensGet.serverLogic(kittenId => {
    val gotKittenOpt = Database.kittens.find(_.id == kittenId)
    gotKittenOpt
      .map(gotKitten => {
        Database.kittens = Database.kittens.filterNot(_.id == kittenId)
        IO.pure(Right(StatusCode.Ok -> gotKitten))
      })
      .getOrElse(
        IO.pure(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id $kittenId was not found")))
      )
  })

  val kittensPost: Endpoint[Unit, Kitten, (StatusCode, ErrorResponse), (StatusCode, Kitten), Any] = endpoint.post
    .in("kitten")
    .in(jsonBody[Kitten])
    .errorOut(statusCode)
    .errorOut(jsonBody[ErrorResponse])
    .out(statusCode)
    .out(jsonBody[Kitten])

  val kittensPostServerEndpoint: ServerEndpoint[Any, IO] = kittensPost.serverLogic(kitten => {
    if (kitten.id <= 0) {
      IO.pure(Left(StatusCode.BadRequest -> ErrorResponse("negative ids are not accepted")))
    } else {
      if (Database.kittens.exists(_.id == kitten.id)) {
        IO.pure(Left(StatusCode.BadRequest -> ErrorResponse(s"kitten with id ${kitten.id} already exists")))
      } else {
        Database.kittens = Database.kittens :+ kitten
        IO.pure(Right(StatusCode.Ok -> kitten))
      }
    }
  })

  val kittensPut: Endpoint[Unit, Kitten, (StatusCode, ErrorResponse), (StatusCode, Kitten), Any] = endpoint.put
    .in("kitten")
    .in(jsonBody[Kitten])
    .errorOut(statusCode)
    .errorOut(jsonBody[ErrorResponse])
    .out(statusCode)
    .out(jsonBody[Kitten])

  val kittensPutServerEndpoint: ServerEndpoint[Any, IO] = kittensPut.serverLogic(kitten => {
    val updatedKittenOpt = Database.kittens
      .find(_.id == kitten.id)
      .map(_.copy(name = kitten.name, gender = kitten.gender, ageInDays = kitten.ageInDays))
    updatedKittenOpt
      .map(updatedKitten => {
        Database.kittens = Database.kittens.filterNot(_.id == kitten.id) :+ updatedKitten
        IO.pure(Right(StatusCode.Ok -> updatedKitten))
      })
      .getOrElse(
        IO.pure(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id ${kitten.id} was not found")))
      )
  })

  val kittensDelete: Endpoint[Unit, Long, (StatusCode, ErrorResponse), (StatusCode, Kitten), Any] = endpoint.delete
    .in("kitten")
    .in(path[Long]("id"))
    .errorOut(statusCode)
    .errorOut(jsonBody[ErrorResponse])
    .out(statusCode)
    .out(jsonBody[Kitten])

  val kittensDeleteServerEndpoint: ServerEndpoint[Any, IO] = kittensDelete.serverLogic(kittenId => {
    val deletedKittenOpt = Database.kittens.find(_.id == kittenId)
    deletedKittenOpt
      .map(deletedKitten => {
        Database.kittens = Database.kittens.filterNot(_.id == kittenId)
        IO.pure(Right(StatusCode.Ok -> deletedKitten))
      })
      .getOrElse(
        IO.pure(Left(StatusCode.NotFound -> ErrorResponse(s"kitten with id $kittenId was not found")))
      )
  })

  val serverEndpoints: List[ServerEndpoint[Any, IO]] = List(
    kittensListingServerEndpoint.tag(apiTag),
    kittensGetServerEndpoint.tag(apiTag),
    kittensPostServerEndpoint.tag(apiTag),
    kittensPutServerEndpoint.tag(apiTag),
    kittensDeleteServerEndpoint.tag(apiTag)
  )

object Database:
  case class Kitten(id: Long, name: String, gender: String, ageInDays: Int)

  var kittens = List(
    Kitten(1L, "mew", "male", 20),
    Kitten(2L, "mews", "female", 25),
    Kitten(3L, "smews", "female", 29)
  )
