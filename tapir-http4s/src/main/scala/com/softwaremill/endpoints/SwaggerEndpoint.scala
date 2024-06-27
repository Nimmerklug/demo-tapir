package com.softwaremill.endpoints

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SwaggerEndpoint:

  val apiEndpoints: List[ServerEndpoint[Any, IO]] = List(
    HelloWorldEndpoints.helloServerEndpoint,
    HelloWorldEndpoints.booksListingServerEndpoint
  ) ++ AnimalEndpoints.serverEndpoints

  val docEndpoints: List[ServerEndpoint[Any, IO]] = SwaggerInterpreter()
    .fromServerEndpoints[IO](apiEndpoints, "demo-tapir", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[IO] = PrometheusMetrics.default[IO]()
  val metricsEndpoint: ServerEndpoint[Any, IO] = prometheusMetrics.metricsEndpoint

  val all: List[ServerEndpoint[Any, IO]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)
