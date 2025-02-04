package controladores

import utils.LimpiezaJSON.limpiarJson
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import modelos.Rating
import play.api.libs.json._

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import INSERTS.*
object Ratings extends App {
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

    val userList = new scala.collection.mutable.HashSet[Int]()
    val movieUserList = new ListBuffer[(Int, Int, Double, Long)]()

    datosFiltrados.foreach { fila =>
      for {
        movieId <- Try(fila("id").trim.toInt).toOption
        jsonStr = fila.getOrElse("ratings", "").trim if jsonStr.nonEmpty && jsonStr != "\"\""
        jsonLimpio = limpiarJson(jsonStr).replaceAll("'", "\"")
        jsonArray <- Try(Json.parse(jsonLimpio).as[JsArray]).toOption
      } jsonArray.value.foreach { obj =>
        obj.validate[Rating] match {
          case JsSuccess(rating, _) =>
            userList += rating.userId
            movieUserList += ((movieId, rating.userId, rating.rating, rating.timestamp))
          case JsError(errors) =>
            println(s"Error en el JSON de ratings: $errors")
        }
      }
    }
    if (INSERTRATING.InsertIntoBelongToCollection(movieUserList.toList))
      println("CORRECTO")
    else
      println("ERROR")
      
    // Aquí puedes realizar las operaciones que necesites con userList y movieUserList
    // Ejemplo: imprimir el tamaño de los sets
    println(s"Total de usuarios encontrados: ${userList.size}")
    println(s"Total de ratings procesados: ${movieUserList.size}")

  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
