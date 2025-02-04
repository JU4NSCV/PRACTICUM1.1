package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTCAST {
  val pathSQLCast = "src/main/INSERT_CAST.sql"

  def InsertIntoBelongToCollection(data: List[(Int, Int, String, Int, Int, String)]): Boolean =
    def generateINSERT(row: (Int, Int, String, Int, Int, String)): String =
      s"INSERT INTO _cast (id_movie, id, character, order, cast_id, credit_id) VALUE  (${row._1},${row._2},'${row._3}',${row._4},${row._5},'${row._6}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLCast))
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
