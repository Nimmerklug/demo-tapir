package com.softwaremill.endpoints

import Model.*
import cats.effect.IO
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

object BaseEndpoint:

  type RouteResponse[T] = IO[Either[OutputError, T]]

  lazy val apiText: String                      = "api"
  lazy val apiVersionText: String               = "v1.0"
  lazy val baseApiResource: EndpointInput[Unit] = apiText / apiVersionText
  lazy val baseApiPath: String                  = s"$apiText/$apiVersionText"

  lazy val baseEndpoint: PublicEndpoint[Unit, Unit, Unit, Any] =
    endpoint
      .in(baseApiResource)
      .name(apiVersionText)
      .description("Aviation REST API")

  val badRequestMapping: EndpointOutput.OneOfVariant[BadRequestError] =
    oneOfVariantFromMatchType(StatusCode.BadRequest, jsonBody[BadRequestError])

  val notFoundMapping: EndpointOutput.OneOfVariant[NotFoundError] =
    oneOfVariantFromMatchType(StatusCode.NotFound, jsonBody[NotFoundError])

  val conflictMapping: EndpointOutput.OneOfVariant[ConflictError] =
    oneOfVariantFromMatchType(StatusCode.Conflict, jsonBody[ConflictError])

  val internalErrorMapping: EndpointOutput.OneOfVariant[NotFoundError] =
    oneOfVariantFromMatchType(StatusCode.NotFound, jsonBody[NotFoundError])

  val defaultMapping: EndpointOutput.OneOfVariant[UnknownError] =
    oneOfDefaultVariant(jsonBody[UnknownError])

  def buildContentLocation(resourcePath: String, resourceId: String): String = {
    s"/$baseApiPath/$resourcePath/$resourceId"
  }

object Model:
  sealed trait OutputError {
    val code: String
    val message: String
  }

  case class BadRequestError(code: String, message: String) extends OutputError

  case class NotFoundError(code: String, message: String) extends OutputError

  case class ConflictError(code: String, message: String) extends OutputError

  case class ServerError(code: String, message: String) extends OutputError

  case class ServiceUnavailableError(code: String, message: String) extends OutputError

  case class UnknownError(code: String, message: String) extends OutputError
