package example

import cats.syntax.either.*
import io.circe.Printer
import io.circe.parser.decode
import io.circe.syntax.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.UUID
import scala.jdk.CollectionConverters.*

final class FileRepository(directory: Path) extends NoteService[[A] =>> A]:
  if !Files.exists(directory) then Files.createDirectory(directory)

  private val printer: Printer = Printer(
    dropNullValues = true,
    indent = ""
  )

  def getAllNotes(): Seq[Note] =
    val files = Files.list(directory).iterator.asScala
    files
      .filter(_.toString.endsWith(".json"))
      .map { file =>
        val bytes = Files.readAllBytes(file)
        decode[Note](new String(bytes)).valueOr(throw _)
      }
      .toSeq

  def createNote(title: String, content: String): Note =
    val id = UUID.randomUUID().toString
    val note = Note(id, title, content)
    val file = directory.resolve(s"$id.json")
    val bytes = printer.print(note.asJson).getBytes
    Files.write(file, bytes, StandardOpenOption.CREATE)
    note

  def deleteNote(id: String): Seq[Note] =
    val file = directory.resolve(s"$id.json")
    Files.delete(file)
    getAllNotes()
