import { useLocation } from 'react-router'

/** Mostra o endereço atual, para os testes confirmarem os filtros e a vista no URL. */
export function Endereco() {
  const { search } = useLocation()
  return <output aria-label="endereço">{search}</output>
}
