package controladores

import utils.LimpiezaJSON.{limpiarJson, limpiarJsonCrew}
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import modelos.Cast
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import INSERTS.*
object Cast extends App {
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

    // Lista de tuplas que almacenará los datos procesados
    val movieActorsList = new ListBuffer[(Int, Int, String, Int, Int, String)]()

    datosFiltrados.foreach { fila =>
      for {
        movieId <- Try(fila("id").trim.toInt).toOption
        jsonStr = fila.getOrElse("cast", "").trim if jsonStr.nonEmpty && jsonStr != "\"\""
        jsonLimpio = limpiarJsonCrew(jsonStr).replaceAll("'", "\"")
        jsonArray <- Try(Json.parse(jsonLimpio).as[List[JsObject]]).toOption
      } jsonArray.foreach { jsonObj =>
        jsonObj.validate[Cast] match {
          case JsSuccess(cast, _) =>
            // Agregar la tupla con los datos del actor y película a la lista
            movieActorsList += ((movieId, cast.id, cast.character, cast.order, cast.cast_id, cast.credit_id))
          case JsError(errors) =>
            println(s"Error en el cast JSON: $errors")
        }
      }
    }

    if (INSERTCAST.InsertIntoBelongToCollection(movieActorsList.toList))
      println("CORRECTO")
    else
      println("ERROR")
      
    println("Total de relaciones de actores con películas: " + movieActorsList.size)

  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
