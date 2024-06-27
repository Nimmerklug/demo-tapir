package com.softwaremill.endpoints

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.softwaremill.endpoints.Database._
import io.circe.generic.auto._
import sttp.capabilities.akka.AkkaStreams
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import java.util.UUID

object BooksEndpoint {

implicit val yearCodec: PlainCodec[Year] = implicitly[PlainCodec[Int]].map(new Year(_))(_.year)
  private lazy val baseEndpoint: PublicEndpoint[Unit, (StatusCode, ErrorInfo), Unit, Any] = endpoint
    .in("api" / "v1.0")
    .errorOut(statusCode.and(jsonBody[ErrorInfo]))

  private val booksQueryInput: EndpointInput[BooksQuery] =
    query[Option[Year]]("year").and(query[Option[Int]]("limit")).mapTo[BooksQuery]

  val getAllBooks: PublicEndpoint[(Option[Year], Option[Int]), (StatusCode, ErrorInfo), List[Book], Any] = endpoint.get
    .in("api" / "v1.0" / "books")
    .in(query[Option[Year]]("year"))
    .in(query[Option[Int]]("limit"))
    .errorOut(statusCode)
    .errorOut(jsonBody[ErrorInfo])
    .out(jsonBody[List[Book]])

  val getBooks: PublicEndpoint[BooksQuery, (StatusCode, ErrorInfo), List[Book], Any] = baseEndpoint.get
    .in("book")
    .in(booksQueryInput)
    .out(jsonBody[List[Book]].example(List(Database.books.head)))

  val getBookCover: Endpoint[Unit, UUID, (StatusCode, ErrorInfo), Source[ByteString, Any],  AkkaStreams] =
    baseEndpoint.get
      .in("book" /  path[UUID]("bookId")  / "cover")
      .out(streamTextBody(AkkaStreams)(CodecFormat.TextPlain()))

  val addBook: Endpoint[AuthenticationToken, NewBook, (StatusCode, ErrorInfo), Unit, Any] = baseEndpoint.post
    .securityIn(auth.bearer[String]().mapTo[AuthenticationToken])
    .in("book")
    .in(jsonBody[NewBook])
}

object Database {

  var books: List[Book] = List(
    Book(
      UUID.randomUUID(),
      "The Sorrows of Young Werther",
      new Year(1774),
      Author("Johann Wolfgang von Goethe", Country("Germany"))
    ),
    Book(UUID.randomUUID(), "Iliad", new Year(-8000), Author("Homer", Country("Greece"))),
    Book(UUID.randomUUID(), "Nad Niemnem", new Year(1888), Author("Eliza Orzeszkowa", Country("Poland"))),
    Book(UUID.randomUUID(),
      "The Colour of Magic",
      new Year(1983),
      Author("Terry Pratchett", Country("United Kingdom"))),
    Book(UUID.randomUUID(), "The Art of Computer Programming", new Year(1968), Author("Donald Knuth", Country("USA"))),
    Book(UUID.randomUUID(), "Pharaoh", new Year(1897), Author("Boleslaw Prus", Country("Poland")))
  )
  var bookCovers: Map[UUID, String] = Map.empty

  case class AuthenticationToken(value: String)

  case class Year(year: Int) extends AnyVal

  case class Country(name: String)

  case class Author(name: String, country: Country)

  case class Book(id: UUID, title: String, year: Year, author: Author)

  case class NewBook(title: String, cover: Option[String], year: Year, authorName: String, authorCountry: String)

  case class BooksQuery(year: Option[Year], limit: Option[Int])

  case class ErrorInfo(error: String)
}
