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
import io.circe.Decoder

class HttpClient(using ExecutionContext) extends NoteService[Future]:
  def getAllNotes(): Future[Seq[Note]] =
    for
      resp <- Fetch.fetch("./api/notes").toFuture
      json <- resp.jsonOrFailure
    yield decodeJs[Seq[Note]](json).valueOr(throw _)

  def deleteNote(id: String): Future[Seq[Note]] =
    postJson[Seq[Note]](DeleteNote(id))

  def createNote(title: String, content: String): Future[Note] =
    postJson[Note](CreateNote(title, content))

  extension (resp: Response)
    private def jsonOrFailure: Future[js.Any] =
      if resp.ok then resp.json.toFuture
      else Future.failed(new IOException(resp.statusText))

  private def postJson[R: Decoder](command: Command): Future[R] =
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
    yield decodeJs[R](json).valueOr(throw _)

  private val printer: Printer = Printer(
    dropNullValues = true,
    indent = ""
  )