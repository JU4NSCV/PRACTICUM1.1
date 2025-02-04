package utils

object LimpiezaJSON {
  def limpiarJson(jsonStr: String): String = {
    val trimmed = jsonStr.trim

    if (trimmed.isEmpty || trimmed.equalsIgnoreCase("null")) "null"
    else {
      trimmed
        // Eliminar comillas dobles mal ubicadas antes de corchetes/llaves
        .replaceAll("\"\\s*\\}", "}")
        .replaceAll("\"\\s*\\]", "]")
        // Corregir casos específicos del error mostrado
        .replaceAll("\"}]", "}]")
        .replaceAll("\"\\)", ")") // Para casos con paréntesis
        // Limpieza general
        .replace("'", "\"")
        .replace(";;", ";")
        .replace(",}", "}")
        .replace(",]", "]")
        .replace(";", "")
        .replace("\"s", "'s")
        .replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("None", "null")
        // Asegurar formato array/objeto válido
        .replaceAll("^\\[?\\s*\\{", "[{")
        .replaceAll("\\}\\s*\\]?$", "}]")
    }
  }


  def limpiarJsonCrew(jsonStr: String): String = {
    val trimmed = jsonStr.trim

    // Si la cadena está vacía o es "null", devolvemos "null"
    if (trimmed.isEmpty || trimmed.toLowerCase == "null") return "null"

    // Eliminar caracteres no válidos como ; al final de las cadenas
    val sinEscape = trimmed
      .replace("'", "\"")
      .replace(";;", ";") // Eliminar dobles puntos y coma
      .replace(",}", "}") // Corregir coma antes de cerrar objetos
      .replace(",]", "]") // Corregir coma antes de cerrar listas
      .replace("},]", "}]")
      .replace("\"\"}", "\"}")
      .replace(";", "") // Eliminar punto y coma en cualquier lugar del JSON
      .replace("\\\"", "\"") // Corregir las comillas dobles escapadas
      .replace("\\n", "\n") // Cambio de \n por salto de línea
      .replace("\\t", "\t") // Cambio de \t por tabulador
      .replace("\\'", "\"") // Cambio de \' por comillas simples
      .replaceAll("(?<!\\w)'(?!\\w)", "\"") // Reemplazar comillas simples por dobles solo si no están dentro de una palabra
      .replaceAll("\\\\", "") // Elimina barras invertidas dobles
      .replaceAll("\\s*:\\s*", ":") // Elimina espacios alrededor de los dos puntos
      .replaceAll("\\s*,\\s*", ",") // Elimina espacios alrededor de las comas
      .replaceAll("\\s*\\{\\s*", "{") // Elimina espacios después de llaves de apertura
      .replaceAll("\\s*\\}\\s*", "}") // Elimina espacios antes de llaves de cierre
      .replaceAll("\\s*\\[\\s*", "[") // Elimina espacios después de corchetes de apertura
      .replaceAll("\\s*\\]\\s*", "]") // Elimina espacios antes de corchetes de cierre
      .replace("None", "null")

    if (sinEscape.startsWith("{") && !sinEscape.endsWith("\"}")) {
      sinEscape + "\"}"
    } else if (sinEscape.startsWith("{") && !sinEscape.endsWith("}")) {
      sinEscape + "}"
    } else if (sinEscape.startsWith("[") && sinEscape.endsWith("]")) {
      sinEscape
    } else if (sinEscape.startsWith("[") && sinEscape.endsWith(",{\"iso_31]")) {
      sinEscape.replaceAll(",\\{\"iso_31.*?]$", "]")
    } else if (sinEscape.startsWith("[") && sinEscape.endsWith(",{\"id\":")) {
      sinEscape.replaceAll(",\\{\"id\".*?$", "]") // Reemplaza desde ,{"id": hasta el final
    } else if (sinEscape.startsWith("[") && !sinEscape.endsWith("]")) {
      sinEscape + "]" // Añadir el cierre de corchete si falta
    } else {
      "null"
    }
  }

  def limpiarJsonRatings(jsonStr: String): String = {
    val trimmed = jsonStr.trim

    // Si la cadena está vacía o es "null", devolvemos "null"
    if (trimmed.isEmpty || trimmed.toLowerCase == "null") return "null"

    // Eliminar caracteres no válidos como ; al final de las cadenas
    val sinEscape = trimmed
      .replace("'", "\"")
      .replace(";;", ";") // Eliminar dobles puntos y coma
      .replace(",}", "}") // Corregir coma antes de cerrar objetos
      .replace(",]", "]") // Corregir coma antes de cerrar listas
      .replace("},]", "}]")
      .replace("\"\"}", "\"}")
      .replace(";", "") // Eliminar punto y coma en cualquier lugar del JSON
      .replace("\\\"", "\"") // Corregir las comillas dobles escapadas
      .replace("\\n", "\n") // Cambio de \n por salto de línea
      .replace("\\t", "\t") // Cambio de \t por tabulador
      .replace("\\'", "\"") // Cambio de \' por comillas simples
      .replace("None", "null")

    if (sinEscape.startsWith("{") && !sinEscape.endsWith("}")) {
      sinEscape + "}"
    } else if (sinEscape.startsWith("[") && sinEscape.endsWith("]")) {
      sinEscape
    } else if (sinEscape.startsWith("[") && !sinEscape.endsWith("]")) {
      sinEscape + "]" // Añadir el cierre de corchete si falta
    } else {
      "null"
    }
  }
}



