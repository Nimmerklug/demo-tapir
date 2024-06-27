package com.softwaremill.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.softwaremill.endpoints.BooksEndpoint
import com.softwaremill.endpoints.Database._
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BooksRoute {

  lazy val routes: Route =
    getAllBooksRoute ~
      getBooksRoute ~
      //getBookCoverRoute ~
      addBookRoute


  private val getAllBooksRoute: Route = AkkaHttpServerInterpreter().toRoute(
    BooksEndpoint.getAllBooks.serverLogic { case
      (booksYearQuery, booksLimitQuery) =>
      if (booksLimitQuery.getOrElse(0) < 0) {
        Future.successful(Left((StatusCode.BadRequest, ErrorInfo("Limit must be positive"))))
      } else {
        val filteredByYear = booksYearQuery.map(year => books.filter(_.year == year)).getOrElse(books)
        val limited = booksLimitQuery.map(limit => filteredByYear.take(limit)).getOrElse(filteredByYear)
        Future.successful(Right(limited))
      }
    }
  )

  private val getBooksRoute: Route = AkkaHttpServerInterpreter().toRoute(
    BooksEndpoint.getBooks.serverLogic(booksQuery =>
      if (booksQuery.limit.getOrElse(0) < 0) {
        Future.successful(Left((StatusCode.BadRequest, ErrorInfo("Limit must be positive"))))
      } else {
        val filteredByYear = booksQuery.year.map(year => books.filter(_.year == year)).getOrElse(books)
        val limited        = booksQuery.limit.map(limit => filteredByYear.take(limit)).getOrElse(filteredByYear)
        Future.successful(Right(limited))
      })
  )

  //  private val getBookCoverRoute: Route = AkkaHttpServerInterpreter().toRoute(
//    BooksEndpoint.getBookCover.serverLogic(bookId =>
//      bookCovers.get(bookId) match {
  //        case None => Future.successful(Left((StatusCode.NotFound, ErrorInfo("Book not found"))))
  //        case Some(bookCoverPath) => Future.successful(Right(bookCoverPath))
//      })
//  )

  private val addBookRoute: Route = AkkaHttpServerInterpreter().toRoute(
    BooksEndpoint.addBook.serverSecurityLogic(
        authToken =>
          if (authToken.value == "secret") {
            Future.successful(Right(authToken.value))
          } else {
            Future.successful(Left((StatusCode.Unauthorized, ErrorInfo("Incorrect auth token"))))
          }
      )
      .serverLogic(_ => newBook => {
        val book = Book(UUID.randomUUID(),
          newBook.title,
          newBook.year,
          Author(newBook.authorName, Country(newBook.authorCountry)))
        books = books :+ book
        newBook.cover.foreach { cover =>
          bookCovers = bookCovers + (book.id -> cover)
        }
        Future.successful(Right(()))
      }
      )
  )
}
