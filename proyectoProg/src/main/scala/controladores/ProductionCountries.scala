package controladores
import utils.LimpiezaJSON.limpiarJson
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat}
import modelos.ProductionCountry
import play.api.libs.json.{JsArray, JsObject, Json}

import java.io.File
import scala.collection.mutable.ListBuffer
import scala.util.Try
import INSERTS.*
object ProductionCountries extends App {
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

    val countriesList = new ListBuffer[ProductionCountry]()
    val movieCountriesList = new ListBuffer[(Int, String, String)]()

    datosFiltrados.foreach { fila =>
      for {
        movieId <- Try(fila("id").trim.toInt).toOption
        jsonStr = fila.getOrElse("production_countries", "").trim if jsonStr.nonEmpty && jsonStr != "\"\""
        jsonLimpio = limpiarJson(jsonStr).replaceAll("'", "\"")
        jsonArray <- Try(Json.parse(jsonLimpio).as[JsArray]).toOption
      } jsonArray.value.foreach {
        case jsObj: JsObject =>
          Json.fromJson[ProductionCountry](jsObj).asOpt.foreach { country =>
            countriesList += country
            movieCountriesList += ((movieId, country.name, country.iso_3166_1))
          }
        case _ => println(s"Formato inesperado en JSON de production_countries: $jsonLimpio")
      }
    }
    
    if (INSERTRODUCTIONCOUNTRIES.InsertIntoProductionCountries(movieCountriesList.toList))
      println("CORRECTO")
    else
      println("ERROR")
      
  } catch {
    case e: Exception => println(s"Error crítico: ${e.getMessage}")
  } finally {
    reader.close()
  }
}
