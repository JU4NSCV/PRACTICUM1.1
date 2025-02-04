package controladores

import utils.LimpiezaJSON.limpiarJson
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import play.api.libs.json.{JsObject, Json}
import modelos.SpokenLanguage

import java.io.{File, PrintWriter}
import scala.util.{Failure, Success, Try}
import INSERTS.*
object SpokenLanguages extends App {
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

    val spokenLanguagesList = scala.collection.mutable.Set[(String, String)]()
    val movieSpokenLanguagesList = scala.collection.mutable.ListBuffer[(Int, String, String)]()

    datosFiltrados.foreach { fila =>
      for {
        movieId <- Try(fila("id").trim.toInt).toOption
        jsonStr = fila.getOrElse("spoken_languages", "").trim if jsonStr.nonEmpty && jsonStr != "\"\""
        jsonLimpio = limpiarJson(jsonStr).replaceAll("'", "\"")
        jsonArray <- Try(Json.parse(jsonLimpio).as[List[JsObject]]).toOption
      } jsonArray.foreach { langObj =>
        val spokenLanguage = langObj.asOpt[SpokenLanguage].getOrElse(SpokenLanguage("null", "null"))
        spokenLanguagesList.add((spokenLanguage.iso_639_1, spokenLanguage.name))
        movieSpokenLanguagesList.append((movieId, spokenLanguage.iso_639_1, spokenLanguage.name))
      }
    }
    if (INSERTSPOKENLANGUAGES.InsertIntoBelongToCollection(movieSpokenLanguagesList.toList))
      println("CORRECTO")
    else
      println("ERROR")
      
    // Aquí puedes hacer algo con spokenLanguagesList y movieSpokenLanguagesList, por ejemplo, imprimirlos
    println("Idiomas procesados:")
    spokenLanguagesList.foreach { case (isoCode, name) =>
      println(s"$isoCode - $name")
    }

  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
