package example

import io.circe.generic.semiauto.*
import io.circe.Codec

case class Note(id: String, title: String, content: String)
    derives Codec.AsObject

enum Command derives Codec.AsObject:
  case CreateNote(title: String, content: String)
  case DeleteNote(id: String)
