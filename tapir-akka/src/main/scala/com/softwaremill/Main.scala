package com.softwaremill

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import com.softwaremill.endpoints.SwaggerEndpoint
import com.softwaremill.routes.{BooksRoute, HelloRoute}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  implicit lazy val system: ActorSystem           = ActorSystem("WebAppActorSystem")
  implicit val executionContext: ExecutionContext = system.dispatcher
  system.log.info(s"Starting WebServer")
  lazy val routes: Route = {
    Directives.concat(
      HelloRoute.routes,
      BooksRoute.routes,
      SwaggerEndpoint.route,
      AkkaHttpServerInterpreter(customServerOptions)
        .toRoute(metricsEndpoint)
    )
  }
  val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
  val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint
  val customServerOptions: AkkaHttpServerOptions = AkkaHttpServerOptions.customiseInterceptors
    .metricsInterceptor(prometheusMetrics.metricsInterceptor())
    .options
  val server: Future[Http.ServerBinding] =
    Http()
      .newServerAt("localhost", 9090)
      .bind(routes)

  server.map { binding =>
    val address = binding.localAddress
    println(s"Successfully started on http:/$address/docs")
  } recover { case ex =>
    println("Failed to start the server due to: " + ex.getMessage)
  }

  /*
  server.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error(s"Failed to bind HTTP endpoint, terminating system: $ex")
      system.terminate()
  }
   */
}
