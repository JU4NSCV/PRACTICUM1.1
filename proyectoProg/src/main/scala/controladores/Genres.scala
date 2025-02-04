package controladores
import utils.LimpiezaJSON.limpiarJson
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import modelos.Genre
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import INSERTS.*
object Genres extends App {
  val rutaCsv = "data/pi_movies_complete.csv"

  implicit val csvFormat: DefaultCSVFormat = new DefaultCSVFormat {
    override val delimiter = ';'
  }

  val reader = CSVReader.open(new File(rutaCsv))

  try {
    val datos = reader.allWithHeaders()

    val datosFiltrados = datos.filter { fila =>
      val columnasExcluyendoId = fila - "id"
      columnasExcluyendoId.values.exists(_.trim.nonEmpty) &&
        !columnasExcluyendoId.values.forall(_.trim.toLowerCase == "<unset>")
    }.distinct

    println(s"Total de filas después de filtrar: ${datosFiltrados.size}")

    val genresList = new ListBuffer[Genre]()

    datosFiltrados.foreach { fila =>
      val jsonStr = fila.getOrElse("genres", "").trim
      if (jsonStr.nonEmpty && jsonStr != "\"\"") {
        val jsonLimpio = limpiarJson(jsonStr).replaceAll("'", "\"")
        Try(Json.parse(jsonLimpio).as[List[JsObject]]) match {
          case Success(jsonArray) =>
            jsonArray.foreach { jsonObj =>
              jsonObj.validate[Genre] match {
                case JsSuccess(genre, _) =>
                  genresList += genre
                case JsError(errors) =>
                  println(s"Error en el JSON de género: $errors")
              }
            }
          case Failure(exception) =>
            println(s"Error al parsear JSON: ${exception.getMessage}\nJSON problemático: $jsonLimpio")
        }
      }
    }
    

  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
