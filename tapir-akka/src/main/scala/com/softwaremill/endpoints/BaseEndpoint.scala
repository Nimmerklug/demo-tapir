package com.softwaremill.endpoints

import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import scala.concurrent.Future

object BaseEndpoint {

  type RouteResponse[T] = Future[Either[OutputError, T]]

  val badRequestMapping: EndpointOutput.OneOfVariant[BadRequestError] =
    oneOfVariantFromMatchType(StatusCode.BadRequest, jsonBody[BadRequestError])

  val notFoundMapping: EndpointOutput.OneOfVariant[NotFoundError] =
    oneOfVariantFromMatchType(StatusCode.NotFound, jsonBody[NotFoundError])

  val unauthorizedMapping: EndpointOutput.OneOfVariant[UnauthorizedError] =
    oneOfVariantFromMatchType(StatusCode.Unauthorized, jsonBody[UnauthorizedError])

  val internalErrorMapping: EndpointOutput.OneOfVariant[InternalServerError] =
    oneOfVariantFromMatchType(StatusCode.InternalServerError, jsonBody[InternalServerError])

  val defaultMapping: EndpointOutput.OneOfVariant[UnknownError] =
    oneOfDefaultVariant(jsonBody[UnknownError])

}

trait OutputError {
  val code: String
  val message: String
}

case class BadRequestError(code: String, message: String) extends OutputError

case class NotFoundError(code: String, message: String) extends OutputError

case class UnauthorizedError(code: String, message: String) extends OutputError

case class InternalServerError(code: String, message: String) extends OutputError

case class UnknownError(code: String, message: String) extends OutputError
