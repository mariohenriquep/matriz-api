/**
 * Número de caracteres de um texto, como o contam o contrato (maxLength) e o PostgreSQL.
 * `texto.length` conta unidades UTF-16: um emoji como 📌 contaria 2 (defeito encontrado na Ficha 04, Ex. 2).
 */
export function caracteres(texto: string): number {
  return [...texto].length
}
