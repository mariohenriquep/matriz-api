import { describe, expect, it } from 'vitest'
import { caracteres } from './texto'

describe('caracteres', () => {
  it('conta letras simples e acentuadas como um caracter cada', () => {
    expect(caracteres('')).toBe(0)
    expect(caracteres('Rever logótipo')).toBe(15) // FALHA CONTROLADA (Ficha 05, Ex. 4): a reverter no commit seguinte
  })

  it('conta um emoji como um só caracter (regressão da Ficha 04, Ex. 2)', () => {
    expect('📌'.length).toBe(2)
    expect(caracteres('📌')).toBe(1)
    expect(caracteres('📌'.repeat(200))).toBe(200)
  })
})
