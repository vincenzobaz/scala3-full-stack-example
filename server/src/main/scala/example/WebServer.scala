package example

import cask.*
import com.typesafe.config.ConfigFactory
import java.nio.file.Paths

object WebServer extends MainRoutes:
  @route("/api/notes", methods = Seq("get", "post"))
  def api(req: Request) =
    if req.exchange.getRequestMethod.equalToString("get") then
      encodeBody(repository.getAllNotes())
    else
      parseBody[Command](req) match
        case Command.CreateNote(title, content) =>
          encodeBody(repository.createNote(title, content))
        case Command.DeleteNote(id) => encodeBody(repository.deleteNote(id))

  @staticResources("/index.html")
  def index() = "index.html"

  @staticResources("/assets")
  def assets() = "assets/"

  val config = ConfigFactory.load()
  override def host = config.getString("http.interface")
  override def port = config.getInt("http.port")

  val repository = FileRepository(Paths.get(config.getString("example.directory")))

  initialize()
