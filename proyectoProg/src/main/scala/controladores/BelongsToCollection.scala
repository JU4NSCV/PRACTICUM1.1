package controladores

import utils.LimpiezaJSON.{limpiarJson, limpiarJsonCrew}
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json, Reads}

import java.io.File
import scala.util.{Failure, Success, Try}
import INSERTS.*

object BelongsToCollection extends App {
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

    def parsearJson[T](jsonStr: String)(implicit reads: Reads[T]): Option[T] = {
      Try(Json.parse(jsonStr).as[T]).toOption
    }

    val belongsToCollectionData = datosFiltrados.flatMap { fila =>
      for {
        movieId <- Try(fila("id").trim.toInt).toOption
        jsonStr = fila.getOrElse("belongs_to_collection", "").trim if jsonStr.nonEmpty && jsonStr != "\"\""
        jsonLimpio = limpiarJsonCrew(jsonStr).replaceAll("'", "\"")
        jsonObj <- Try(Json.parse(jsonLimpio).validate[JsObject]).toOption.collect { case JsSuccess(obj, _) => obj }
        id <- (jsonObj \ "id").asOpt[Int]
        name = (jsonObj \ "name").asOpt[String].filter(_.nonEmpty).getOrElse("null")
        posterPath = (jsonObj \ "poster_path").asOpt[String].filter(_.nonEmpty).getOrElse("null")
        backdropPath = (jsonObj \ "backdrop_path").asOpt[String].filter(_.nonEmpty).getOrElse("null")
      } yield (movieId, id, name, posterPath, backdropPath)
    }
    
    if (INSERTBELONGTOCOLLECTION.InsertIntoBelongToCollection(belongsToCollectionData))
      println("CORRECTO")
    else
      println("ERROR")
      
    println(s"Total de registros procesados: ${belongsToCollectionData.size}")
  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
