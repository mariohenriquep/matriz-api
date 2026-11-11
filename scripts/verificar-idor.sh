#!/bin/bash
# Security review IDOR: Ana (Gestora do Website), Rui (membro), Joana (sem acesso)
# Uso (backend a correr sobre o seed): MATRIZ_SEED_PASSWORD=<valor do .env> bash scripts/verificar-idor.sh
CORPO=$(mktemp); trap 'rm -f "$CORPO"' EXIT
API=${MATRIZ_API:-http://localhost:8080/api/v1}
PW=${MATRIZ_SEED_PASSWORD:?Define MATRIZ_SEED_PASSWORD (a password do seed no .env)}
tok() { curl -s -X POST $API/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"$1\",\"password\":\"$PW\"}" | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])'; }
ANA=$(tok ana@matriz.local); RUI=$(tok rui@matriz.local); JOANA=$(tok joana@matriz.local)
r() { # quem metodo caminho [corpo]
  local t=$1 m=$2 p=$3 b=$4; local args=(-s -o "$CORPO" -w '%{http_code}' -X $m "$API$p" -H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-H 'Content-Type: application/json' -d "$b")
  local c=$(curl "${args[@]}"); echo "$m $p ${b:+$b }-> $c $(head -c 110 "$CORPO")"; }
WEB=$(curl -s $API/projetos -H "Authorization: Bearer $ANA" | python3 -c 'import sys,json;print([p["id"] for p in json.load(sys.stdin)["itens"] if p["nome"]=="Website institucional"][0])')
T=$(curl -s $API/projetos/$WEB/tarefas -H "Authorization: Bearer $ANA" | python3 -c 'import sys,json;d=json.load(sys.stdin)["itens"];print(" ".join(t["id"]+":"+t["criador"]["nome"].split()[0] for t in d))')
echo "Website=$WEB tarefas=$T"
TA=$(echo $T | tr ' ' '\n' | grep Ana | head -1 | cut -d: -f1)
RUIID=$(curl -s $API/auth/eu -H "Authorization: Bearer $RUI" | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])')
echo "== Joana (sem acesso) contra recursos da Ana =="
r $JOANA GET /projetos/$WEB
r $JOANA GET /projetos/999999
r $JOANA PATCH /projetos/$WEB '{"nome":"invadido"}'
r $JOANA DELETE /projetos/$WEB
r $JOANA GET /projetos/$WEB/membros
r $JOANA POST /projetos/$WEB/membros '{"email":"joana@matriz.local"}'
r $JOANA GET /projetos/$WEB/tarefas
r $JOANA POST /projetos/$WEB/tarefas '{"titulo":"intrusa"}'
r $JOANA GET /tarefas/$TA
r $JOANA GET /tarefas/999999
r $JOANA PATCH /tarefas/$TA '{"estado":"CONCLUIDA"}'
r $JOANA DELETE /tarefas/$TA
r $JOANA GET /tarefas/$TA/comentarios
r $JOANA POST /tarefas/$TA/comentarios '{"conteudo":"olá"}'
r $JOANA POST /tarefas/$TA/responsaveis "{\"utilizadorId\":\"$RUIID\"}"
r $JOANA DELETE /tarefas/$TA/responsaveis/$RUIID
r $JOANA GET "/projetos?pesquisa=Website"
echo "== Rui (membro, não Gestor) =="
r $RUI PATCH /projetos/$WEB '{"nome":"Rui tenta"}'
r $RUI DELETE /projetos/$WEB
r $RUI POST /projetos/$WEB/membros '{"email":"joana@matriz.local"}'
r $RUI DELETE /tarefas/$TA
echo "== Manipulação de campos (mass assignment) =="
r $RUI POST /projetos/$WEB/tarefas '{"titulo":"x","criadorId":"1"}'
r $RUI PATCH /tarefas/$TA '{"projetoId":"3"}'
r $RUI POST /projetos '{"nome":"P","gestorId":"1"}'
echo "== Estado depois: nada mudou para a Ana =="
r $ANA GET /projetos/$WEB
r $ANA GET /tarefas/$TA
echo "== Token: logout revoga; token adulterado =="
T2=$(tok joana@matriz.local); r $T2 POST /auth/logout; r $T2 GET /auth/eu
r "${JOANA%?}x" GET /auth/eu
