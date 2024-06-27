package com.softwaremill.endpoints

import com.softwaremill.endpoints.BaseEndpoint._

import scala.concurrent.Future
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

object HelloEndpoint {

  val helloEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get                                                         // http type
      .name("hello-world-get-endpoint")
      .description("Get Hello World message")                            // endpoint's description
      .in("hello".description("Hello endpoint path"))                    // description for uri path, /test uri
      .out(
        stringBody
          .description("type of response")
          .example("~!Hello World!~")
      )                                                                  // This endpoint will return string body. Also, description for body
      .out(statusCode(StatusCode.Ok)
        .description("Specifies response status code for success case")) // Description for result status code

  val helloServerEndpoint: ServerEndpoint[Any, Future] =
    helloEndpoint.serverLogicSuccess(_ => Future.successful("Hello World!"))

  val helloNameEndpoint: PublicEndpoint[String, OutputError, String, Any] =
    endpoint.get
      .name("hello-get-name-endpoint")
      .description("Returns hello name")
      .in("hello-name".description("Hello name endpoint path"))
      .in(query[String]("name"))
      .out(stringBody
        .description("type of response")
        .example("Hello Bob the 5th"))
      .errorOut(
        oneOf[OutputError](
          internalErrorMapping,
          defaultMapping
        )
      )

  val helloUserEndpoint: PublicEndpoint[String, OutputError, String, Any] =
    endpoint.get
      .name("hello-get-user-endpoint")
      .description("Auth user")
      .in("hello-user".description("Hello user endpoint path"))
      .in(header[String]("X-User-Id"))
      .out(stringBody
        .description("type of response"))
      .errorOut(
        oneOf[OutputError](
          unauthorizedMapping,
          defaultMapping
        )
      )

  val helloPostEndpoint: PublicEndpoint[String, OutputError, String, Any] =
    endpoint.post
      .name("hello-post-name-endpoint")
      .description("Returns hello name")
      .in("hello-name".description("Hello name endpoint path"))
      .in(jsonBody[String])
      .out(stringBody
        .description("type of response"))
      .errorOut(
        oneOf[OutputError](
          internalErrorMapping,
          defaultMapping
        )
      )
}
