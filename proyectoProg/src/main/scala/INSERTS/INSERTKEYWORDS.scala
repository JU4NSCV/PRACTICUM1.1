package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTKEYWORDS {
  val pathSQLKeywords = "src/main/INSERT_KEYWORDS.sql"

  def InsertIntoKewords(data: List[(Int, Int, String)]): Boolean =
    def generateINSERT(row: (Int, Int, String)): String =
      s"INSERT INTO keywords (id_movie, Int, name) VALUE (${row._1},${row._2},'${row._3}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLKeywords))
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
