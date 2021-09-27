package example

import org.scalajs.dom.html.Element
import org.scalajs.dom.document
import org.scalajs.dom.html.*

import DomHelper.*

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

given ExecutionContext = ExecutionContext.global
val service = new HttpClient()

val addNoteForm: Div =
  val titleInput = input()
  val contentTextArea = textarea()

  val saveButton = button("Create Note") { _ =>
    service
      .createNote(titleInput.value, contentTextArea.value)
      .map(addNote)
  }
  val form = div(
    titleInput,
    contentTextArea,
    saveButton
  )
  form.className = "note-form"
  form

val appContainer: Div =
  val container = div(h1("My Notepad"), addNoteForm)
  container.id = "app-container"
  container

def addNote(note: Note): Unit =
  val elem = div(
    h2(note.title),
    button("x") { _ =>
      service
        .deleteNote(note.id)
        .foreach(drawNotes(_))
    },
    p(note.content)
  )
  elem.className = "note"
  appContainer.appendChild(elem)

def drawNotes(notes: Seq[Note]) =
  // 1. delete current notes
  val oldNotes = document.body.getElementsByClassName("note")
  for i <- 0 until oldNotes.length do
    val note = oldNotes.item(i)
    appContainer.removeChild(note)

  // 2 redraw notes
  notes.foreach(addNote(_))

@main def start: Unit =
  document.body.appendChild(appContainer)
  service.getAllNotes().foreach(drawNotes(_))
