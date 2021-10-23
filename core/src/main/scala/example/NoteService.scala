package example

trait NoteService[F[_]]:
  def getAllNotes(): F[Seq[Note]]
  def createNote(title: String, content: String): F[Note]
  def deleteNote(id: String): F[Seq[Note]]
