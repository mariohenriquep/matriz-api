import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { ProvedorSessao } from './auth/ProvedorSessao'
import { RotaProtegida } from './auth/RotaProtegida'
import { Inicio } from './paginas/Inicio'
import { Login } from './paginas/Login'

function App() {
  return (
    <BrowserRouter>
      <ProvedorSessao>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <RotaProtegida>
                <Inicio />
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
