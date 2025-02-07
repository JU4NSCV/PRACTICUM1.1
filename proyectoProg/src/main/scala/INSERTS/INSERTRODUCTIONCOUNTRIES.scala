package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTRODUCTIONCOUNTRIES {
  val pathSQLProductionCountries = "src/main/INSERT_PRODUCTION_COUNTRIES.sql"

  def InsertIntoProductionCountries(data: List[(Int, String, String)]): Boolean =
    def generateINSERT(row: (Int, String, String)): String =
      s"INSERT INTO Production_Countries (id_movie, iso_3166_1, name) VALUE   (${row._1},'${row._2}','${row._3}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLProductionCountries))
      data.foreach { row =>
        file.write(generateINSERT(row))
        file.newLine()
      }
      file.close()
      true
    } catch {
      case e: Exception =>
        println(e.getMessage)
        false
    }
}
