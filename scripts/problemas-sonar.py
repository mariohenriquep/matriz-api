#!/usr/bin/env python3
"""Mostra no log da CI os problemas que o SonarQube Cloud encontrou no código novo (sem abrir o site).

Corre depois da análise: espera que o SonarQube Cloud acabe de processar o relatório (.scannerwork/report-task.txt)
e lista os problemas abertos do pull request (SONAR_PR) ou do ramo (SONAR_RAMO). O projeto é público: a API de
leitura não precisa do token. Nunca falha o job: só informa.
"""
import json
import os
import time
import urllib.parse
import urllib.request

API = 'https://sonarcloud.io/api'
PROJETO = 'mariohenriquep_matriz-api'


def obter(url):
    with urllib.request.urlopen(url, timeout=30) as resposta:
        return json.load(resposta)


def esperar_processamento():
    try:
        with open('.scannerwork/report-task.txt', encoding='utf-8') as ficheiro:
            tarefa = dict(linha.strip().split('=', 1) for linha in ficheiro if '=' in linha)
    except OSError:
        return
    for _ in range(36):  # até 3 minutos
        estado = obter(tarefa['ceTaskUrl'])['task']['status']
        if estado not in ('PENDING', 'IN_PROGRESS'):
            print(f'Processamento no SonarQube Cloud: {estado}')
            return
        time.sleep(5)


def main():
    esperar_processamento()
    parametros = {'componentKeys': PROJETO, 'resolved': 'false', 'ps': '100', 'inNewCodePeriod': 'true'}
    if os.environ.get('SONAR_PR'):
        parametros['pullRequest'] = os.environ['SONAR_PR']
    elif os.environ.get('SONAR_RAMO'):
        parametros['branch'] = os.environ['SONAR_RAMO']
    dados = obter(f'{API}/issues/search?{urllib.parse.urlencode(parametros)}')
    problemas = dados.get('issues', [])
    print(f'{len(problemas)} problema(s) aberto(s) no código novo')
    for p in problemas:
        impactos = ', '.join(f"{i['softwareQuality']}:{i['severity']}" for i in p.get('impacts', []))
        ficheiro = p['component'].split(':', 1)[-1]
        print(f"- [{impactos or p.get('severity')}] {p['rule']} {ficheiro}:{p.get('line', '-')} {p['message']}")


if __name__ == '__main__':
    try:
        main()
    except Exception as erro:  # só informa; nunca falha o job
        print(f'Não foi possível obter os problemas do SonarQube Cloud: {erro}')
