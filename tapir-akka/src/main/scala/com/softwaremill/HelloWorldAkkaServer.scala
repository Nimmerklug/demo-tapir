package com.softwaremill

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.model.StatusCode
import io.circe.generic.auto._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.StdIn

object HelloWorldAkkaServer extends App {
  implicit val actorSystem: ActorSystem = ActorSystem()
  import actorSystem.dispatcher

  sealed trait ErrorInfo
  case class NotFound(what: String) extends ErrorInfo
  case class Unauthorized(realm: String) extends ErrorInfo
  case class Unknown(code: Int, msg: String) extends ErrorInfo
  case object NoContent extends ErrorInfo

  val helloWorld: PublicEndpoint[String, ErrorInfo, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody).errorOut(
      oneOf[ErrorInfo](
        oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound].description("not found"))),
        oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized].description("unauthorized"))),
        oneOfVariant(statusCode(StatusCode.NoContent).and(emptyOutputAs(NoContent))),
        //oneOfVariant(statusCode(StatusCode.BadRequest).and(plainBody[String]),
        oneOfDefaultVariant(jsonBody[Unknown].description("unknown"))
      )
    )

  // tapir route
  val helloWorldRoute: Route = {
    import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

    AkkaHttpServerInterpreter().toRoute(helloWorld.serverLogicSuccess(name => Future.successful(s"Hello, $name!")))
  }

  // direct akka http route
  val helloWorldRoute2: Route = {
    import akka.http.scaladsl.server.Directives._

    get {
      path("hello2") {
        parameter("name".as[String]) { name =>
          complete(s"Hello, $name!")
        }
      }
    }
  }

  // expose documentation of endpoints
  val swaggerUIRoute =
    AkkaHttpServerInterpreter().toRoute(
      SwaggerInterpreter()
        .fromEndpoints[Future](List(helloWorld), "Hello", "1.0.0")
    )

  // combining the routes
  val combinedRoutes = {
    import akka.http.scaladsl.server.Directives._
    helloWorldRoute ~ helloWorldRoute2 ~ swaggerUIRoute
  }

  // running the server
  val bind = Http()
    .newServerAt("localhost", 8888)
    .bindFlow(combinedRoutes)
    .map { binding =>
      val address = binding.localAddress
      println(s"Successfully started on http:/$address/docs")
    } recover { case ex =>
    println("Failed to start the server due to: " + ex.getMessage)
  }

  StdIn.readLine()
  Await.result(bind.transformWith { r => actorSystem.terminate().transform(_ => r) }, 1.minute)
}
