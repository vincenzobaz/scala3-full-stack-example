package example

import org.scalajs.dom.experimental.*
import scala.scalajs.js

import java.io.IOException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import io.circe.scalajs.*
import io.circe.syntax.*
import io.circe.Printer

import cats.syntax.either.*
import Command.*

class HttpClient(using ExecutionContext) extends NoteService[Future]:
  private val printer: Printer = Printer(
    dropNullValues = true,
    indent = ""
  )

  def deleteNote(id: String): Future[Seq[Note]] =
    val command = DeleteNote(id)
    val request = Request(
      "./api/notes",
      new:
        method = HttpMethod.POST
        headers = js.Dictionary("Content-Type" -> "application/json")
        body = printer.print(command.asJson)
    )
    for
      resp <- Fetch.fetch(request).toFuture
      json <- resp.jsonOrFailure
    yield decodeJs[Seq[Note]](json).valueOr(throw _)

  def getAllNotes(): Future[Seq[Note]] =
    for
      resp <- Fetch.fetch("./api/notes").toFuture
      json <- resp.jsonOrFailure
    yield decodeJs[Seq[Note]](json).valueOr(throw _)

  def createNote(title: String, content: String): Future[Note] =
    val command = CreateNote(title, content)
    val request = Request(
      "./api/notes",
      new:
        method = HttpMethod.POST
        headers = js.Dictionary("Content-Type" -> "application/json")
        body = printer.print(command.asJson)
    )
    for
      resp <- Fetch.fetch(request).toFuture
      json <- resp.jsonOrFailure
    yield decodeJs[Note](json).valueOr(throw _)

  extension (resp: Response)
    private def jsonOrFailure: Future[js.Any] =
      if resp.ok then resp.json.toFuture
      else Future.failed(new IOException(resp.statusText))
