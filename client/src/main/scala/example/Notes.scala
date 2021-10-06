package example

import japgolly.scalajs.react.vdom.html_<^.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.extra.StateSnapshot
import scala.language.implicitConversions

import scala.concurrent.ExecutionContext

given ExecutionContext = ExecutionContext.global
val client = HttpClient()

val TitleForm = ScalaComponent
  .builder[StateSnapshot[String]]
  .render_P { stateSnapshot =>
    <.input.text(
      ^.value := stateSnapshot.value,
      ^.onChange ==> ((e: ReactEventFromInput) =>
        stateSnapshot.setState(e.target.value)
      )
    )
  }
  .build

val ContentForm = ScalaComponent
  .builder[StateSnapshot[String]]
  .render_P { stateSnapshot =>
    <.textarea(
      ^.value := stateSnapshot.value,
      ^.onChange ==> ((e: ReactEventFromInput) =>
        stateSnapshot.setState(e.target.value)
      )
    )
  }
  .build


case class State(notes: Seq[Note], title: String, content: String)

def Main(init: Seq[Note]) = ScalaComponent
  .builder[Unit]
  .initialState[State](State(init, "", ""))
  .render { $ =>
    val state = $.state
    val titleV = StateSnapshot
      .zoom[State, String](_.title)(title => state => state.copy(title = title))
      .of($)
    val contentV = StateSnapshot
      .zoom[State, String](_.content)(content =>
        state => state.copy(content = content)
      )
      .of($)

    def createNote(e: ReactEventFromInput) =
      e.preventDefaultCB >> 
        Callback.future(client.createNote(state.title, state.content).map(CallbackTo(_))) >>
        Callback.future(client.getAllNotes().map(notes => $.setState(State(notes, "", ""))))

    def deleteNote(id: String) = Callback.future {
      client
        .deleteNote(id)
        .map(remainingNotes => $.modState(_.copy(notes = remainingNotes)))
    }

    <.div(
      ^.id := "app-container",
      <.h1("My notepad"),
      <.form(
        ^.className := "note-form",
        ^.onSubmit ==> createNote,
        TitleForm(titleV),
        ContentForm(contentV),
        <.button("Create Note")
      ),
      state.notes.toTagMod(note =>
        <.div(
          ^.className := "note",
          <.h2(note.title),
          <.button(^.onClick --> deleteNote(note.id), "x"),
          <.p(note.content)
        )
      )
    )
  }
  .build

@main def run =
  client.getAllNotes().map(Main(_)().renderIntoDOM(org.scalajs.dom.document.body))
