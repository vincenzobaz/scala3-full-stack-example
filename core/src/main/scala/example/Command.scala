package example

import io.circe.Codec

enum Command derives Codec.AsObject:
  case CreateNote(title: String, content: String)
  case DeleteNote(id: String)
