package com.softwaremill.endpoints

import akka.http.scaladsl.server.Route
import sttp.apispec.openapi.Info
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SwaggerEndpoint {

  lazy val route: Route =
    AkkaHttpServerInterpreter()
      .toRoute(swaggerEndpoints)

  // add endpoints to the list for swagger documentation
  private lazy val swaggerEndpoints =
    SwaggerInterpreter().fromEndpoints[Future](endpoints, info)

  private lazy val info: Info =
    Info("Tapir Learning Service API", "1.0.0-SNAPSHOT", Some("Researching about Tapir library"))

  private lazy val endpoints = List(
    HelloEndpoint.helloEndpoint,
    HelloEndpoint.helloNameEndpoint,
    HelloEndpoint.helloUserEndpoint,
    HelloEndpoint.helloPostEndpoint,
    BooksEndpoint.getAllBooks,
    BooksEndpoint.getBooks,
    BooksEndpoint.getBookCover,
    BooksEndpoint.addBook
  )
}
