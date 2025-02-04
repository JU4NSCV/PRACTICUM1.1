package utils

object LimpiezaGeneral {

  def escapeMySQLString(input: String): String = {
    if (input == null) "" // Si la entrada es nula, devuelve una cadena vacía
    else {
      input
        .replace("\\", "\\\\").trim // Escapar barra invertida
        .replace("'", "''").trim // Escapar comilla simple
        .replace("\"", "\\\"").trim // Escapar comilla doble (opcional, por seguridad)
        .replace("\n", "\\n").trim // Escapar salto de línea
        .replace("\r", "\\r").trim // Escapar retorno de carro
        .replace("\t", "\\t").trim // Escapar tabulador
    }
  }

  def limpiezaJsonsDobleComillaApostrofe(input: String): String =
    input.replace("\"","'").trim

  def limpiezaJsonsGeneralDeComillasApostrofes(input: String): String = {
      input
        .replace("\"", "'").trim
        .replace("{\'", "{\"").trim // Reemplazar comillas simples por dobles al inicio
        .replace("\': \'", "\": \"").trim // Reemplazar comillas simples por dobles en pares clave-valor
        .replace("\', \'", "\", \"").trim // Reemplazar comillas simples por dobles en listas
        .replace(", \'", ", \"").trim // Reemplazar comillas simples por dobles en listas
        .replace("\': ", "\": ").trim // Reemplazar comillas simples por dobles en valores no string
        .replace("\'}", "\"}").trim // Reemplazar comillas simples por dobles al final
  }

  def limpiezaJsonsNoneNull(input: String): String =
    input.replace("None", "null").trim

  def limpiarJsonCompleto(input: String): String = {
    if (input.trim == null || input.trim == "") "{}"
    else {
      val paso1 = EliminarEspacios(input)
      val paso2 = limpiezaJsonsDobleComillaApostrofe(paso1)
      val paso3 = limpiezaJsonsGeneralDeComillasApostrofes(paso2)
      val paso4 = limpiezaJsonsNoneNull(paso3)
      paso4
    }
  }

  def EliminarEspacios(input: String): String = input.trim


}