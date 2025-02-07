package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTPRODUCTIONCOMPANIES {
  val pathSQLProductionCompani = "src/main/INSERT_PRODUCTION_COMPANI.sql"

  def InsertIntoProductionCompanies(data: List[(Int,Int , String)]): Boolean =
    def generateINSERT(row: (Int, Int, String)): String =
      s"INSERT INTO production_companies (id_movie, ID_COMPANY, name) VALUE   (${row._1},${row._2},'${row._3}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLProductionCompani))
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
