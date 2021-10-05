package example

import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.*
import scala.concurrent.ExecutionContext

case class State(notes: Seq[Note], title: String, content: String)

class Backend($: BackendScope[Unit, State]):
  given ExecutionContext = ExecutionContext.global
  val client = HttpClient()

  private def deleteNote(id: String) = Callback {
    client
      .deleteNote(id)
      .map(remainingNotes => $.modState(_.copy(notes = remainingNotes)))
  }

  private val getAllNotes = Callback {
    client.getAllNotes().map(newNotes => $.modState(_.copy(notes = newNotes)))
  }

  private def updateContent(ev: ReactEventFromInput) = CallbackTo[Unit] {
    $.modState(_.copy(content = ev.target.value))
  }

  private def updateTitle(ev: ReactEventFromInput) = CallbackTo[Unit] {
    $.modState(_.copy(title = ev.target.value))
  }

  private def createNote(s: State) = CallbackTo[Unit] {
    client
      .createNote(s.title, s.content)
      .flatMap(_ => client.getAllNotes())
      .map(notes => $.modState(_ => State(notes, "", "")))
  }

  def render(s: State) =
    <.div(
      ^.id := "app-container",
      <.h1("My notepad"),
      <.form(
        ^.className := "note-form",
        <.input.text(^.onInput ==> updateTitle), //title
        <.textarea(^.onInput ==> updateContent), // content
        <.button(^.onClick --> createNote(s), "Create Note")
      ),
      s.notes.toTagMod(note =>
        <.div(
          ^.className := "note",
          <.h2(note.title),
          <.button(^.onClick --> deleteNote(note.id), "x"),
          <.p(note.content)
        )
      )
    )

@main def run = 
  println("hey")
  val topLevel = ScalaComponent.builder[Unit]
    .initialState(State(Nil, "", ""))
    .renderBackend[Backend]
    .build

  topLevel().renderIntoDOM(org.scalajs.dom.document.body)