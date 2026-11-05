#!/usr/bin/env python3
"""Verifica que a API em execução cumpre o contrato (openapi.yaml).

Percorre as 20 operações do contrato com as contas de demonstração, provocando cada código de resposta
documentado (2xx, 400, 401, 403, 404, 409, 422), e confirma que:
  - o código devolvido está documentado na operação;
  - o corpo da resposta valida contra o schema do contrato (JSON Schema 2020-12).
No fim lista os códigos documentados que não foram exercitados e as divergências encontradas.

Uso (com o backend a correr numa base de dados com o seed de demonstração):
    pip install pyyaml jsonschema
    MATRIZ_SEED_PASSWORD=<password do .env> python3 scripts/verificar-contrato.py [URL_DA_API]

Cria e elimina no fim um projeto de teste; não altera os dados do seed (exceto terminar a sessão da Joana).
Sai com código 1 se houver divergências.
"""
import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

import yaml
from jsonschema import Draft202012Validator
from referencing import Registry, Resource
from referencing.jsonschema import DRAFT202012

spec = yaml.safe_load(open(Path(__file__).resolve().parent.parent / 'openapi.yaml', encoding='utf-8'))
reg = Registry().with_resource('urn:spec', Resource.from_contents(spec, default_specification=DRAFT202012))
B = sys.argv[1].rstrip('/') if len(sys.argv) > 1 else 'http://localhost:8080/api/v1'
def call(m, p, tok=None, body=None):
    h = {'Content-Type': 'application/json'} if body is not None else {}
    if tok: h['Authorization'] = 'Bearer ' + tok
    r = urllib.request.Request(B + p, method=m, headers=h, data=None if body is None else json.dumps(body).encode())
    try:
        with urllib.request.urlopen(r) as s: return s.status, s.read(), dict(s.headers)
    except urllib.error.HTTPError as e: return e.code, e.read(), dict(e.headers)
def op_for(m, p):
    for tpl, ops in spec['paths'].items():
        rx = '^' + re.sub(r'\{[^}]+\}', '[^/]+', tpl) + '$'
        if re.match(rx, p.split('?')[0]) and m.lower() in ops: return tpl, ops[m.lower()]
    return None, None
def resolve(o):
    while isinstance(o, dict) and '$ref' in o:
        path = o['$ref'][2:].split('/'); o = spec
        for k in path: o = o[k]
    return o
falhas = []; vistos = set()
def check(m, p, tok=None, body=None, esperado=None):
    st, raw, hd = call(m, p, tok, body)
    tpl, op = op_for(m, p)
    vistos.add((m, tpl, str(st)))
    msg = f'{m} {p} -> {st}'
    if esperado and st != esperado: falhas.append(msg + f' (esperado {esperado})')
    if op is None: falhas.append(msg + ' operação não documentada'); return st, raw
    resps = op['responses']
    if str(st) not in resps: falhas.append(msg + f' código não documentado ({list(resps)})'); return st, raw
    r = resolve(resps[str(st)])
    content = r.get('content')
    if not content:
        if raw: falhas.append(msg + ' corpo inesperado')
        return st, raw
    ctype = hd.get('Content-Type', '').split(';')[0]
    if ctype not in content: falhas.append(msg + f' content-type {ctype} não documentado ({list(content)})')
    schema = list(content.values())[0]['schema']
    fix = json.loads(json.dumps(schema).replace('"#/', '"urn:spec#/'))
    v = Draft202012Validator(fix, registry=reg)
    data = json.loads(raw)
    for e in v.iter_errors(data):
        falhas.append(msg + f' schema: {"/".join(map(str, e.absolute_path))}: {e.message[:150]}')
    return st, raw
P = os.environ.get('MATRIZ_SEED_PASSWORD') or sys.exit('Define MATRIZ_SEED_PASSWORD (a password das contas de demonstração).')
def login(e): return json.loads(call('POST', '/auth/login', body={'email': e, 'password': P})[1])['token']
check('POST', '/auth/login', body={'email': 'ana@matriz.local', 'password': P}, esperado=200)
check('POST', '/auth/login', body={'email': 'ana@matriz.local', 'password': 'x'}, esperado=401)
check('POST', '/auth/login', body={'email': '', 'password': ''}, esperado=400)
ana, rui, joana = login('ana@matriz.local'), login('rui@matriz.local'), login('joana@matriz.local')
check('GET', '/auth/eu', ana, esperado=200); check('GET', '/auth/eu', esperado=401)
check('GET', '/projetos', ana, esperado=200); check('GET', '/projetos?pesquisa=web', ana, esperado=200)
check('GET', '/projetos?pesquisa=' + 'x' * 101, ana, esperado=400); check('GET', '/projetos', esperado=401)
st, raw = check('POST', '/projetos', ana, {'nome': 'Contrato', 'descricao': 'd'}, esperado=201); pid = json.loads(raw)['id']
check('POST', '/projetos', ana, {'nome': ' '}, esperado=400)
check('GET', f'/projetos/{pid}', ana, esperado=200); check('GET', f'/projetos/{pid}', joana, esperado=404)
check('PATCH', f'/projetos/{pid}', ana, {'descricao': None}, esperado=200); check('PATCH', f'/projetos/{pid}', ana, {}, esperado=400)
check('GET', f'/projetos/{pid}/membros', ana, esperado=200)
check('POST', f'/projetos/{pid}/membros', ana, {'email': 'rui@matriz.local'}, esperado=201)
check('POST', f'/projetos/{pid}/membros', ana, {'email': 'rui@matriz.local'}, esperado=409)
check('POST', f'/projetos/{pid}/membros', ana, {'email': 'ninguem@matriz.local'}, esperado=422)
check('POST', f'/projetos/{pid}/membros', ana, {'email': 'x'}, esperado=400)
check('POST', f'/projetos/{pid}/membros', rui, {'email': 'joana@matriz.local'}, esperado=403)
check('PATCH', f'/projetos/{pid}', rui, {'nome': 'R'}, esperado=403)
check('DELETE', f'/projetos/{pid}/membros/1', ana, esperado=409)
check('GET', f'/projetos/{pid}/tarefas', ana, esperado=200); check('GET', f'/projetos/{pid}/tarefas?estado=X', ana, esperado=400)
check('GET', f'/projetos/{pid}/tarefas?estado=POR_FAZER&prioridade=ALTA&responsavelId=2', ana, esperado=200)
st, raw = check('POST', f'/projetos/{pid}/tarefas', ana, {'titulo': 'T', 'descricao': 'd', 'prioridade': 'ALTA', 'dataLimite': '2030-01-01', 'responsavelIds': ['2']}, esperado=201); tid = json.loads(raw)['id']
check('POST', f'/projetos/{pid}/tarefas', ana, {'titulo': 'T', 'responsavelIds': ['3']}, esperado=422)
check('POST', f'/projetos/{pid}/tarefas', ana, {'titulo': ''}, esperado=400)
check('GET', f'/projetos/{pid}/tarefas', ana, esperado=200)
check('GET', f'/tarefas/{tid}', ana, esperado=200); check('GET', f'/tarefas/{tid}', joana, esperado=404)
check('PATCH', f'/tarefas/{tid}', rui, {'estado': 'EM_CURSO', 'dataLimite': None}, esperado=200)
check('PATCH', f'/tarefas/{tid}', ana, {'estado': 'X'}, esperado=400)
check('POST', f'/tarefas/{tid}/responsaveis', ana, {'utilizadorId': '1'}, esperado=201)
check('POST', f'/tarefas/{tid}/responsaveis', ana, {'utilizadorId': '1'}, esperado=409)
check('POST', f'/tarefas/{tid}/responsaveis', ana, {'utilizadorId': '3'}, esperado=422)
check('POST', f'/tarefas/{tid}/responsaveis', ana, {}, esperado=400)
check('DELETE', f'/tarefas/{tid}/responsaveis/1', ana, esperado=204); check('DELETE', f'/tarefas/{tid}/responsaveis/1', ana, esperado=404)
check('POST', f'/tarefas/{tid}/comentarios', rui, {'conteudo': 'Olá'}, esperado=201)
check('POST', f'/tarefas/{tid}/comentarios', rui, {'conteudo': ' '}, esperado=400)
check('GET', f'/tarefas/{tid}/comentarios', ana, esperado=200)
check('DELETE', f'/tarefas/{tid}', rui, esperado=403)
check('DELETE', f'/projetos/{pid}', rui, esperado=403)
check('DELETE', f'/tarefas/{tid}', ana, esperado=204)
check('DELETE', f'/projetos/{pid}/membros/2', ana, esperado=204)
check('DELETE', f'/projetos/{pid}', ana, esperado=204)

# 401 sem token e 404 para quem não é membro, em todas as operações (projeto 1 / tarefa 1 são da Ana; Joana não é membro)
corpos = {'POST': {'titulo': 'x', 'nome': 'x', 'email': 'rui@matriz.local', 'utilizadorId': '2', 'conteudo': 'x'}, 'PATCH': {'nome': 'x'}}
for tpl, ops in spec['paths'].items():
    if tpl.startswith('/auth/login'): continue
    for m in ops:
        if m == 'parameters': continue
        p = tpl.replace('{projetoId}', '1').replace('{tarefaId}', '1').replace('{utilizadorId}', '2')
        check(m.upper(), p, None, None, esperado=401)
        if '{' in tpl:
            b = None
            if m.upper() == 'POST':
                b = {'conteudo': 'x'} if 'comentarios' in tpl else {'utilizadorId': '2'} if 'responsaveis' in tpl else {'email': 'rui@matriz.local'} if 'membros' in tpl else {'titulo': 'x'}
            if m.upper() == 'PATCH': b = {'nome': 'x'} if 'projetos' in tpl else {'titulo': 'x'}
            check(m.upper(), p, joana, b, esperado=404)
check('DELETE', '/projetos/1/membros/2', rui, esperado=403)
check('POST', '/auth/logout', joana, esperado=204); check('GET', '/auth/eu', joana, esperado=401)
# operações sem nenhum código exercitado
for tpl, ops in spec['paths'].items():
    for m, o in ops.items():
        if m == 'parameters': continue
        for c in o['responses']:
            if (m.upper(), tpl, c) not in vistos: print('não exercitado:', m.upper(), tpl, c)
print(f'\n{len(vistos)} combinações operação/código exercitadas.')
print('\nDIVERGÊNCIAS:' if falhas else 'Sem divergências entre a API e o contrato.')
for f in falhas:
    print(' -', f)
sys.exit(1 if falhas else 0)
