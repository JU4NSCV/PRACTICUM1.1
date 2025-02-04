package INSERTS

import java.io.{BufferedWriter, FileWriter}

object INSERTCREW {
  val pathSQLCrew = "src/main/INSERT_CREW.sql"

  def InsertIntoBelongToCollection(data: List[(Int, Int, Int, Int, String, String, String, String)]): Boolean =
    def generateINSERT(row: (Int, Int, Int, Int, String, String, String, String)): String =
      s"INSERT INTO Crew (id, id_movie, credit_id, gender, department, job, profile_path, name) VALUE  (${row._1},${row._2},${row._3},${row._4},'${row._5}','${row._6}','${row._7}','${row._8}');"

    try {
      val file = new BufferedWriter(new FileWriter(pathSQLCrew))
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
