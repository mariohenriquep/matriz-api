import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { ProvedorSessao } from './auth/ProvedorSessao'
import { RotaProtegida } from './auth/RotaProtegida'
import { useSessao } from './auth/useSessao'
import { Apresentacao } from './paginas/Apresentacao'
import { Inicio } from './paginas/Inicio'
import { Login } from './paginas/Login'
import { Registo } from './paginas/Registo'
import { ProjetoDetalhe } from './paginas/ProjetoDetalhe'
import { TarefaDetalhe } from './paginas/TarefaDetalhe'

/** Em "/": o painel para quem tem sessão; a apresentação pública para os visitantes. */
function PaginaInicial() {
  const { utilizador } = useSessao()
  return utilizador ? <Inicio /> : <Apresentacao />
}

function App() {
  return (
    <BrowserRouter>
      <ProvedorSessao>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/registo" element={<Registo />} />
          <Route path="/" element={<PaginaInicial />} />
          <Route
            path="/projetos/:projetoId"
            element={
              <RotaProtegida>
                <ProjetoDetalhe />
              </RotaProtegida>
            }
          />
          <Route
            path="/tarefas/:tarefaId"
            element={
              <RotaProtegida>
                <TarefaDetalhe />
              </RotaProtegida>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </ProvedorSessao>
    </BrowserRouter>
  )
}

export default App
