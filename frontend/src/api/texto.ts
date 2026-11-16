/**
 * Número de caracteres de um texto, como o contam o contrato (maxLength) e o PostgreSQL.
 * `texto.length` conta unidades UTF-16: um emoji como 📌 contaria 2 (defeito encontrado na Ficha 04, Ex. 2).
 */
export function caracteres(texto: string): number {
  return [...texto].length
}

/** Iniciais para o avatar: "Ana Silva" → "AS". */
export function iniciais(nome: string) {
  const partes = nome.trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) return '?'
  const primeira = partes[0][0]
  const ultima = partes.length > 1 ? partes[partes.length - 1][0] : ''
  return (primeira + ultima).toUpperCase()
}
