package example

import io.circe.generic.semiauto.*
import io.circe.Codec

case class Note(id: String, title: String, content: String) derives Codec.AsObject
