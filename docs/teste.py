from pathlib import Path
from datetime import date
from calendar import monthrange

from docx import Document
from docx.table import _Cell
from docx.shared import Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn


# ============================================================
# ARQUIVOS
# ============================================================

# Modelo oficial.
#
# IMPORTANTE:
# O arquivo original NÃO será sobrescrito.
ARQUIVO_MODELO = Path(
    "folha-de-ponto-modelo.docx"
)

# Arquivo que será gerado pelo teste.
ARQUIVO_SAIDA = Path(
    "teste_grade_frequencia.docx"
)


# ============================================================
# PERÍODO DO TESTE
# ============================================================

# Setembro de 2026.
#
# O modelo oficial está como:
#
# FOLHA DE FREQUÊNCIA – 09/2026

MES = 8
ANO = 2026

# ============================================================
# DADOS DO DOCENTE - TESTE
# ============================================================
# Simulam os dados que futuramente virão do banco.
# Campos vazios continuam sem informação no documento.

DADOS_DOCENTE = {
    "nome": "Katia Barbosa Martines",
    "codigo": "265",
    "matricula": "123456",
    "regime_juridico": "CLT",
    "categoria": "",
    "hora_aula_semanal": "",
    "hora_atividade": "",
    "hae_projeto": "",
    "hae_coordenacao": "",
}

# ============================================================
# CALENDÁRIO ACADÊMICO - DIAS SEM AULA
# ============================================================
#
# Datas que não terão aula no teste.
# Reposição e alterações excepcionais ficam fora desta etapa.
# ============================================================

DIAS_SEM_AULA = {
    "2026-09-07": "Feriado - Independência do Brasil",
    "2026-10-12": "Feriado - Nossa Senhora Aparecida",
    "2026-10-15": "Dia do Professor",
    "2026-10-28": "Dia do Servidor Público",
    "2026-11-02": "Feriado - Dia de Finados",
    "2026-11-15": "Feriado - Proclamação da República",
    "2026-11-20": "Feriado - Dia da Consciência Negra",
    "2026-11-21": "Emenda - Dia da Consciência Negra",
    "2026-12-25": "Natal",
}

# ============================================================
# VERIFICAR DIA SEM AULA
# ============================================================


def dia_esta_sem_aula(data_atual):
    """Retorna True quando a data está marcada no Calendário Acadêmico como sem aula."""
    return data_atual.isoformat() in DIAS_SEM_AULA


# ============================================================
# DIAS DA SEMANA
# ============================================================
# O método weekday() do Python retorna:
# 0 = Segunda | 1 = Terça | 2 = Quarta | 3 = Quinta
# 4 = Sexta | 5 = Sábado | 6 = Domingo
#
# Esses valores serão usados para atualizar automaticamente
# a coluna "Semana" da Folha de Frequência.
# ============================================================

DIAS_SEMANA = {
    0: "Segunda",
    1: "Terça",
    2: "Quarta",
    3: "Quinta",
    4: "Sexta",
    5: "Sábado",
    6: "Domingo",
}


# ============================================================
# AULAS
# ============================================================
#
# Aqui simulamos os dados que futuramente virão do banco.
#
# IMPORTANTE:
#
# Agora NÃO cadastramos cada horário individualmente.
#
# Cada aula possui:
#
# dia_semana
# periodo
# hora_inicio
# hora_fim
# sigla
#
# Exemplo:
#
# 08:00 -> 11:30
#
# O sistema calcula automaticamente quais posições da
# tabela precisam ser preenchidas.
#
# ============================================================

AULAS = [

    # ========================================================
    # SEGUNDA-FEIRA
    # ========================================================
    #
    # Banco de Dados:
    #
    # As aulas são cadastradas individualmente:
    #
    # 08:00 -> 08:50
    # 08:50 -> 09:40
    # 09:50 -> 10:40
    # 10:40 -> 11:30
    #
    # O sistema fará o mapeamento para 2º, 3º, 4º e 5º.
    #
    # ========================================================

    # Cada registro representa UMA aula de 50 minutos.
    # O sistema usa a hora de início para descobrir a posição
    # mais adequada na Grade Horária.
    {
        "dia_semana": 0,
        "periodo": "MANHA",
        "hora_inicio": "08:00",
        "hora_fim": "08:50",
        "sigla": "BD",
    },

    {
        "dia_semana": 0,
        "periodo": "MANHA",
        "hora_inicio": "08:50",
        "hora_fim": "09:40",
        "sigla": "BD",
    },

    {
        "dia_semana": 0,
        "periodo": "MANHA",
        "hora_inicio": "09:50",
        "hora_fim": "10:40",
        "sigla": "BD",
    },

    {
        "dia_semana": 0,
        "periodo": "MANHA",
        "hora_inicio": "10:40",
        "hora_fim": "11:30",
        "sigla": "BD",
    },


    # ========================================================
    # TERÇA-FEIRA
    # ========================================================
    #
    # Desenvolvimento Web (DW).
    #
    # A disciplina ocupa todo o turno da tarde: 1º ao 6º
    # horário. Cada registro representa uma aula de 50 minutos.
    #
    # 1º -> 13:00 -> 13:50
    # 2º -> 13:50 -> 14:40
    # 3º -> 14:50 -> 15:40
    # 4º -> 15:40 -> 16:30
    # 5º -> 16:40 -> 17:30
    # 6º -> 17:30 -> 18:20
    #
    # ========================================================

    {
        "dia_semana": 1,
        "periodo": "TARDE",
        "hora_inicio": "13:00",
        "hora_fim": "13:50",
        "sigla": "DW",
    },

    {
        "dia_semana": 1,
        "periodo": "TARDE",
        "hora_inicio": "13:50",
        "hora_fim": "14:40",
        "sigla": "DW",
    },

    {
        "dia_semana": 1,
        "periodo": "TARDE",
        "hora_inicio": "14:50",
        "hora_fim": "15:40",
        "sigla": "DW",
    },

    {
        "dia_semana": 1,
        "periodo": "TARDE",
        "hora_inicio": "15:40",
        "hora_fim": "16:30",
        "sigla": "DW",
    },

    {
        "dia_semana": 1,
        "periodo": "TARDE",
        "hora_inicio": "16:40",
        "hora_fim": "17:30",
        "sigla": "DW",
    },

    {
        "dia_semana": 1,
        "periodo": "TARDE",
        "hora_inicio": "17:30",
        "hora_fim": "18:20",
        "sigla": "DW",
    },


    # ========================================================
    # QUARTA-FEIRA
    # ========================================================
    #
    # Engenharia de Software:
    #
    # 09:20 -> 10:10
    #
    # ========================================================

    {
        "dia_semana": 2,
        "periodo": "MANHA",
        "hora_inicio": "09:20",
        "hora_fim": "10:10",
        "sigla": "ES",
    },

    {
        "dia_semana": 2,
        "periodo": "NOITE",
        "hora_inicio": "19:20",
        "hora_fim": "21:00",
        "sigla": "ED",
    },


    # ========================================================
    # QUINTA-FEIRA
    # ========================================================

    {
        "dia_semana": 3,
        "periodo": "TARDE",
        "hora_inicio": "14:50",
        "hora_fim": "15:40",
        "sigla": "PI",
    },

    {
        "dia_semana": 3,
        "periodo": "NOITE",
        "hora_inicio": "20:10",
        "hora_fim": "21:00",
        "sigla": "PI",
    },


    # ========================================================
    # SEXTA-FEIRA
    # ========================================================

    {
        "dia_semana": 4,
        "periodo": "MANHA",
        "hora_inicio": "10:00",
        "hora_fim": "10:50",
        "sigla": "ALG",
    },


    # ========================================================
    # SÁBADO
    # ========================================================

    {
        "dia_semana": 5,
        "periodo": "MANHA",
        "hora_inicio": "11:10",
        "hora_fim": "12:00",
        "sigla": "BD",
    },

]


# ============================================================
# HORÁRIOS DO MODELO OFICIAL
# ============================================================
#
# Estes são os horários que aparecem na Grade do documento.
#
# Eles são usados como referência para descobrir quais
# posição que cada aula individual deve ocupar.
#
# ============================================================

HORARIOS = {

    "MANHA": [
        "07:30",
        "08:20",
        "09:20",
        "10:10",
        "11:10",
        "12:00",
    ],

    "TARDE": [
        "13:00",
        "13:50",
        "14:50",
        "15:40",
        "16:40",
        "17:30",
    ],

    "NOITE": [
        "19:20",
        "20:10",
        "21:10",
        "22:00",
    ],

}


# ============================================================
# COLUNAS DA GRADE
# ============================================================
#
# A tabela da Grade possui:
#
# 0 = Dia
#
# MANHÃ:
# 1 a 6
#
# TARDE:
# 7 a 12
#
# NOITE:
# 13 a 16
#
# ============================================================

COLUNAS_GRADE = {

    "MANHA": [
        1,
        2,
        3,
        4,
        5,
        6,
    ],

    "TARDE": [
        7,
        8,
        9,
        10,
        11,
        12,
    ],

    "NOITE": [
        13,
        14,
        15,
        16,
    ],

}


# ============================================================
# LINHAS DA GRADE
# ============================================================
#
# Na tabela oficial:
#
# 3 = Segunda
# 4 = Terça
# 5 = Quarta
# 6 = Quinta
# 7 = Sexta
# 8 = Sábado
#
# ============================================================

LINHAS_GRADE = {

    0: 3,
    1: 4,
    2: 5,
    3: 6,
    4: 7,
    5: 8,

}


# ============================================================
# COLUNAS DA FOLHA DE FREQUÊNCIA
# ============================================================
#
# ATENÇÃO:
#
# A Folha de Frequência possui células MESCLADAS no
# cabeçalho.
#
# Por isso não podemos usar os números obtidos do
# cabeçalho diretamente.
#
# Nas linhas dos dias existem 21 células REAIS:
#
# 0  = Dia
# 1  = Semana
#
# 2  = Assinatura manhã
# 3  = Manhã 1º
# 4  = Manhã 2º
# 5  = Manhã 3º
# 6  = Manhã 4º
# 7  = Manhã 5º
# 8  = Manhã 6º
#
# 9  = Assinatura tarde
# 10 = Tarde 1º
# 11 = Tarde 2º
# 12 = Tarde 3º
# 13 = Tarde 4º
# 14 = Tarde 5º
# 15 = Tarde 6º
#
# 16 = Assinatura noite
# 17 = Noite 1º
# 18 = Noite 2º
# 19 = Noite 3º
# 20 = Noite 4º
#
# As colunas de assinatura NUNCA são preenchidas.
#
# ============================================================

COLUNAS_FREQUENCIA = {

    "MANHA": [
        3,
        4,
        5,
        6,
        7,
        8,
    ],

    "TARDE": [
        10,
        11,
        12,
        13,
        14,
        15,
    ],

    "NOITE": [
        17,
        18,
        19,
        20,
    ],

}


# ============================================================
# PEGAR CÉLULA NORMAL
# ============================================================

def pegar_celula(
    tabela,
    linha,
    coluna
):
    """
    Pega uma célula usando a estrutura normal do python-docx.

    É usada principalmente na Grade Horária.
    """

    if linha < 0:
        return None

    if linha >= len(tabela.rows):
        return None

    celulas = tabela.rows[
        linha
    ].cells

    if coluna < 0:
        return None

    if coluna >= len(celulas):
        return None

    return celulas[
        coluna
    ]


# ============================================================
# PEGAR CÉLULAS REAIS DE UMA LINHA
# ============================================================

def pegar_celulas_reais(
    linha
):
    """
    Retorna somente as células XML reais da linha.

    Isso é importante na Folha de Frequência porque
    o cabeçalho possui células mescladas.

    Exemplo:

    python-docx pode mostrar 26 células.

    Mas a linha real possui 21 <w:tc>.

    É nessas 21 células que trabalhamos.
    """

    return [

        _Cell(
            tc,
            linha
        )

        for tc in linha._tr.tc_lst

    ]


# ============================================================
# PEGAR CÉLULA REAL DA FOLHA DE FREQUÊNCIA
# ============================================================

def pegar_celula_real_frequencia(
    tabela,
    linha,
    coluna
):
    """
    Pega uma célula da linha dos dias usando a estrutura
    real da tabela.

    Isso evita o problema causado pelas células mescladas.
    """

    if linha < 0:
        return None

    if linha >= len(tabela.rows):
        return None

    celulas = pegar_celulas_reais(
        tabela.rows[
            linha
        ]
    )

    if coluna < 0:
        return None

    if coluna >= len(celulas):
        return None

    return celulas[
        coluna
    ]


# ============================================================
# LIMPAR CÉLULA
# ============================================================

def limpar_celula(
    celula
):
    """
    Remove somente os textos/runs da célula.

    Não altera:

    - bordas
    - largura
    - altura
    - mesclagem
    - estrutura da tabela
    """

    if celula is None:
        return

    for paragrafo in celula.paragraphs:

        for run in list(
            paragrafo.runs
        ):

            run._element.getparent().remove(
                run._element
            )


# ============================================================
# ESCREVER SIGLA NA GRADE
# ============================================================

def escrever_sigla(
    celula,
    sigla
):
    """
    Escreve a sigla da disciplina na Grade.

    Exemplo:

    BD
    DW
    PI

    Fonte tamanho 9 e centralizada.
    """

    if celula is None:
        return

    limpar_celula(
        celula
    )

    paragrafo = celula.paragraphs[
        0
    ]

    paragrafo.alignment = (
        WD_ALIGN_PARAGRAPH.CENTER
    )

    run = paragrafo.add_run(
        sigla
    )

    run.font.size = Pt(
        9
    )


# ============================================================
# PREENCHER CABEÇALHO DO DOCENTE
# ============================================================

def substituir_placeholder_em_celula(celula, placeholder, valor):
    """Substitui um placeholder mantendo a formatação básica da célula."""
    texto = celula.text

    if placeholder not in texto:
        return False

    novo_texto = texto.replace(placeholder, valor)

    paragrafo = celula.paragraphs[0]

    if paragrafo.runs:
        paragrafo.runs[0].text = novo_texto
        for run in paragrafo.runs[1:]:
            run.text = ""
    else:
        paragrafo.add_run(novo_texto)

    return True


# ============================================================
# PREENCHER CABEÇALHO DO DOCENTE
# ============================================================

def preencher_cabecalho(documento):
    """Preenche os placeholders do cabeçalho com os dados do docente."""
    if not documento.tables:
        return

    tabela = documento.tables[0]

    placeholders = {
        "{{PROFESSOR}}": DADOS_DOCENTE["nome"],
        "{{COD}}": DADOS_DOCENTE["codigo"],
        "{{MAT}}": DADOS_DOCENTE["matricula"],
        "{{RJ}}": DADOS_DOCENTE["regime_juridico"],
    }

    for linha in tabela.rows:
        for celula in linha.cells:
            for placeholder, valor in placeholders.items():
                substituir_placeholder_em_celula(
                    celula,
                    placeholder,
                    valor
                )


# ============================================================
# CONVERTER HORÁRIO PARA MINUTOS
# ============================================================

def horario_em_minutos(
    horario
):
    """
    Converte:

    08:00

    para:

    480

    Isso facilita as comparações de horários.
    """

    partes = horario.split(
        ":"
    )

    hora = int(
        partes[0]
    )

    minuto = int(
        partes[1]
    )

    return (
        hora * 60
        + minuto
    )


# ============================================================
# CALCULAR SLOTS OCUPADOS
# ============================================================

def calcular_slots_ocupados(
    periodo,
    hora_inicio,
    hora_fim
):
    """
    Descobre a posição da Grade Horária para UMA aula.

    REGRA NOVA:

    Cada registro de AULA_DA_GRADE representa uma aula individual,
    normalmente com 50 minutos.

    O sistema usa a HORA DE INÍCIO REAL da aula e procura o horário
    do modelo que estiver mais próximo.

    Exemplo:

        Aula real: 08:00 -> 08:50

        Modelo:
            1º = 07:30
            2º = 08:20

        08:00 está a:
            30 minutos de 07:30
            20 minutos de 08:20

        Resultado: 2º horário.

    Outro exemplo:

        09:50 -> 10:40

        09:50 está mais próximo de 10:10.

        Resultado: 4º horário.

    Se houver empate, usamos o horário seguinte do modelo.
    Assim:

        08:50

        08:20 = 30 minutos
        09:20 = 30 minutos

        Resultado: 3º horário.

    O horário final é validado para garantir que a aula seja válida,
    mas não é usado para escolher outra posição. Isso é importante
    porque cada registro já representa UMA aula individual.
    """

    horarios = HORARIOS[periodo]

    inicio = horario_em_minutos(hora_inicio)
    fim = horario_em_minutos(hora_fim)

    if fim <= inicio:
        raise ValueError(
            f"Horário inválido: {hora_inicio} -> {hora_fim}"
        )

    horarios_minutos = [
        horario_em_minutos(horario)
        for horario in horarios
    ]

    # --------------------------------------------------------
    # Procurar o horário mais próximo do início real.
    # --------------------------------------------------------
    #
    # Em caso de empate, o código escolhe o horário posterior.
    # Para isso usamos <= na comparação.
    #
    # Exemplo:
    #
    # 08:50
    # 08:20 -> diferença 30
    # 09:20 -> diferença 30
    #
    # O segundo horário é escolhido.
    # --------------------------------------------------------

    indice_melhor = 0
    menor_distancia = abs(
        horarios_minutos[0] - inicio
    )

    for indice in range(1, len(horarios_minutos)):

        distancia = abs(
            horarios_minutos[indice] - inicio
        )

        if distancia <= menor_distancia:
            menor_distancia = distancia
            indice_melhor = indice

    return [indice_melhor]


# ============================================================
# ENCONTRAR GRADE
# ============================================================

def encontrar_grade(
    documento
):
    """
    Procura a tabela da Grade Horária.
    """

    for tabela in documento.tables:

        texto = " ".join(

            celula.text.upper()

            for linha in tabela.rows

            for celula in linha.cells

        )

        if (
            "MANHÃ" in texto
            and "TARDE" in texto
            and "NOITE" in texto
            and "TEMPO DE AULA 50 MIN" in texto
        ):

            return tabela

    raise RuntimeError(
        "Grade Horária não encontrada."
    )


# ============================================================
# ENCONTRAR FOLHA DE FREQUÊNCIA
# ============================================================

def encontrar_frequencia(
    documento
):
    """
    Procura a tabela da Folha de Frequência.
    """

    for tabela in documento.tables:

        texto = " ".join(

            celula.text.upper()

            for linha in tabela.rows

            for celula in linha.cells

        )

        if (
            "DIA" in texto
            and "SEMANA" in texto
            and "MANHÃ" in texto
            and "TARDE" in texto
            and "NOITE" in texto
            and "ASSINATURA" in texto
        ):

            if len(tabela.rows) >= 30:

                return tabela

    raise RuntimeError(
        "Folha de Frequência não encontrada."
    )


# ============================================================
# ENCONTRAR LINHA DO DIA
# ============================================================

def encontrar_linha_do_dia(tabela, dia):
    """Procura a linha da Folha de Frequência correspondente ao dia."""
    numero = f"{dia:02d}"

    for indice_linha, linha in enumerate(tabela.rows):
        celulas = pegar_celulas_reais(linha)

        for celula in celulas:
            if celula.text.strip() == numero:
                return indice_linha

    return None


# ============================================================
# PREENCHER GRADE
# ============================================================

def preencher_grade(
    documento
):
    """
    Preenche a Grade Horária.

    A posição é calculada a partir do horário inicial real de cada aula.

    Assim a Grade e a Folha de Frequência usam a mesma regra.
    """

    tabela = encontrar_grade(
        documento
    )

    print(
        "Calculando horários da Grade..."
    )

    # --------------------------------------------------------
    # Limpar somente as posições de horário.
    #
    # Não mexemos no cabeçalho.
    # --------------------------------------------------------

    for linha in range(
        3,
        9
    ):

        for periodo in COLUNAS_GRADE:

            for coluna in COLUNAS_GRADE[
                periodo
            ]:

                celula = pegar_celula(
                    tabela,
                    linha,
                    coluna
                )

                if celula is not None:

                    limpar_celula(
                        celula
                    )

    # --------------------------------------------------------
    # Preencher as aulas.
    # --------------------------------------------------------

    for aula in AULAS:

        dia = aula[
            "dia_semana"
        ]

        periodo = aula[
            "periodo"
        ]

        hora_inicio = aula[
            "hora_inicio"
        ]

        hora_fim = aula[
            "hora_fim"
        ]

        sigla = aula[
            "sigla"
        ]

        # Domingo não recebe aula.
        if dia == 6:
            continue

        linha = LINHAS_GRADE.get(
            dia
        )

        if linha is None:
            continue

        # ----------------------------------------------------
        # Descobrir todas as posições ocupadas.
        # ----------------------------------------------------

        slots = calcular_slots_ocupados(
            periodo,
            hora_inicio,
            hora_fim
        )

        print(
            f"  {sigla}: "
            f"{hora_inicio} -> {hora_fim} "
            f"({periodo}) "
            f"slots {[
                s + 1 for s in slots
            ]}"
        )

        # ----------------------------------------------------
        # Preencher cada posição.
        # ----------------------------------------------------

        for slot in slots:

            coluna = COLUNAS_GRADE[
                periodo
            ][
                slot
            ]

            celula = pegar_celula(
                tabela,
                linha,
                coluna
            )

            if celula is None:
                continue

            escrever_sigla(
                celula,
                sigla
            )


# ============================================================
# ESCREVER TEXTO PRESERVANDO A FORMATAÇÃO
# ============================================================
def escrever_texto_preservando_formatacao(celula, texto):
    """
    Altera somente o texto da célula.

    Não recria a célula, portanto preserva a estrutura
    da tabela, como bordas, tamanho e mesclagens.
    """

    if celula is None or not celula.paragraphs:
        return

    paragrafo = celula.paragraphs[0]

    if paragrafo.runs:
        # Aproveita o primeiro run para preservar sua formatação.
        paragrafo.runs[0].text = texto

        # Remove possíveis pedaços de texto antigos.
        for run in list(paragrafo.runs[1:]):
            run._element.getparent().remove(run._element)

    else:
        paragrafo.add_run(texto)


# ============================================================
# COPIAR SOMBREAMENTO DA CÉLULA
# ============================================================
def copiar_sombreamento(celula_origem, celula_destino):
    """
    Copia somente a cor de fundo da célula de origem.

    Usamos isso para:
    - deixar domingos cinza;
    - devolver para branco uma linha que deixou de ser domingo.

    Bordas, tamanho e estrutura não são alterados.
    """

    if celula_origem is None or celula_destino is None:
        return

    tc_pr_origem = celula_origem._tc.get_or_add_tcPr()

    shd_origem = tc_pr_origem.find(
        qn("w:shd")
    )

    tc_pr_destino = celula_destino._tc.get_or_add_tcPr()

    shd_destino = tc_pr_destino.find(
        qn("w:shd")
    )

    # Remove a cor anterior da célula destino.
    if shd_destino is not None:
        tc_pr_destino.remove(
            shd_destino
        )

    # Copia a configuração de fundo da célula origem.
    if shd_origem is not None:
        shd_novo = OxmlElement("w:shd")

        for atributo, valor in shd_origem.attrib.items():
            shd_novo.set(
                atributo,
                valor
            )

        tc_pr_destino.append(
            shd_novo
        )


def aplicar_sombreamento(celula, cor):
    """
    Aplica uma cor de fundo EXPLÍCITA na célula.

    IMPORTANTE:
    O modelo original usa atributos de tema do Word, como
    ``themeFill`` e ``themeFillShade`` nas linhas cinzas.

    Se apenas alterarmos o atributo ``fill``, esses atributos
    antigos podem continuar prevalecendo e o Word mantém a
    célula cinza mesmo quando queremos deixá-la branca.

    Por isso, esta função remove completamente o sombreamento
    antigo e cria um novo ``w:shd`` somente com a cor desejada.

    Assim:
        FFFFFF = branco real
        D9D9D9 = cinza real
    """

    if celula is None:
        return

    tc_pr = celula._tc.get_or_add_tcPr()

    # Remove QUALQUER sombreamento antigo da célula.
    # Isso também elimina themeFill/themeFillShade do modelo.
    for shd_antigo in tc_pr.findall(qn("w:shd")):
        tc_pr.remove(shd_antigo)

    # Cria um sombreamento novo, sem atributos de tema.
    shd_novo = OxmlElement("w:shd")
    shd_novo.set(qn("w:val"), "clear")
    shd_novo.set(qn("w:color"), "auto")
    shd_novo.set(qn("w:fill"), cor)

    tc_pr.append(shd_novo)


# ============================================================
# ATUALIZAR CALENDÁRIO DA FOLHA DE FREQUÊNCIA
# ============================================================
def atualizar_calendario_frequencia(documento):
    """Atualiza mês, datas e dias da semana, sem registrar ponto."""
    tabela = encontrar_frequencia(documento)
    quantidade_dias = monthrange(ANO, MES)[1]

    for dia in range(1, 32):
        linha = encontrar_linha_do_dia(tabela, dia)
        if linha is None:
            continue

        celulas = pegar_celulas_reais(tabela.rows[linha])
        if len(celulas) < 2:
            continue

        if dia > quantidade_dias:
            for celula in celulas:
                limpar_celula(celula)
                aplicar_sombreamento(celula, "FFFFFF")
            continue

        data_atual = date(ANO, MES, dia)
        numero_semana = data_atual.weekday()
        nome_semana = DIAS_SEMANA[numero_semana]

        escrever_texto_preservando_formatacao(celulas[0], f"{dia:02d}")
        escrever_texto_preservando_formatacao(celulas[1], nome_semana)

        # Domingo fica cinza; os demais dias ficam brancos.
        cor_linha = "D9D9D9" if numero_semana == 6 else "FFFFFF"
        for celula in celulas:
            aplicar_sombreamento(celula, cor_linha)

        # O calendário acadêmico é consultado apenas para identificar
        # datas sem aula. Não transformamos isso em presença ou falta.
        if dia_esta_sem_aula(data_atual):
            motivo = DIAS_SEM_AULA[data_atual.isoformat()]
            print(f"  Dia {dia:02d}: sem aula no calendário - {motivo}")


# ============================================================
# ATUALIZAR TÍTULO DA FOLHA
# ============================================================
def substituir_placeholder_em_runs(paragrafo, placeholder, valor):
    """
    Substitui um placeholder mesmo quando o Word o divide em vários runs.

    Exemplo real do modelo:
        run 4 = "{{MES"
        run 5 = "}}"

    O texto visual é "{{MES}}", mas nenhum run sozinho contém o
    placeholder completo. Por isso precisamos trabalhar com a sequência
    de runs sem alterar a formatação geral do documento.
    """

    runs = paragrafo.runs
    if not runs:
        return False

    # Procuramos o placeholder na sequência completa de textos dos runs.
    texto_completo = "".join(run.text for run in runs)
    posicao = texto_completo.find(placeholder)

    if posicao == -1:
        return False

    inicio = posicao
    fim = posicao + len(placeholder)

    # Descobre quais runs contêm os caracteres do placeholder.
    acumulado = 0
    run_inicio = None
    run_fim = None
    indice_inicio_no_run = 0
    indice_fim_no_run = 0

    for indice, run in enumerate(runs):
        texto = run.text
        proximo = acumulado + len(texto)

        if run_inicio is None and inicio < proximo:
            run_inicio = indice
            indice_inicio_no_run = inicio - acumulado

        if fim <= proximo:
            run_fim = indice
            indice_fim_no_run = fim - acumulado
            break

        acumulado = proximo

    if run_inicio is None or run_fim is None:
        return False

    # O placeholder está dentro de um único run.
    if run_inicio == run_fim:
        texto = runs[run_inicio].text
        runs[run_inicio].text = (
            texto[:indice_inicio_no_run]
            + valor
            + texto[indice_fim_no_run:]
        )
        return True

    # O placeholder atravessa vários runs.
    primeiro = runs[run_inicio]
    ultimo = runs[run_fim]

    prefixo = primeiro.text[:indice_inicio_no_run]
    sufixo = ultimo.text[indice_fim_no_run:]

    # Mantemos a formatação do primeiro run e colocamos o valor nele.
    primeiro.text = prefixo + valor

    # Os runs intermediários faziam parte somente do placeholder.
    for indice in range(run_inicio + 1, run_fim):
        runs[indice].text = ""

    # Mantemos qualquer texto que venha depois do placeholder no último run.
    ultimo.text = sufixo

    return True


def substituir_mes_ano_no_paragrafo(paragrafo):
    """
    Substitui {{MES}} e {{ANO}} no parágrafo, inclusive quando o Word
    divide os placeholders em vários runs.
    """

    substituiu = False

    if substituir_placeholder_em_runs(
        paragrafo,
        "{{MES}}",
        f"{MES:02d}"
    ):
        substituiu = True

    if substituir_placeholder_em_runs(
        paragrafo,
        "{{ANO}}",
        str(ANO)
    ):
        substituiu = True

    return substituiu


# ============================================================
# ATUALIZAR TÍTULO DA FOLHA DE FREQUÊNCIA
# ============================================================
def atualizar_titulo_frequencia(documento):
    """
    Substitui {{MES}} e {{ANO}} no título da Folha de Frequência.

    O Word pode dividir {{MES}} em vários runs. Por isso usamos
    substituir_placeholder_em_runs(), que procura o placeholder
    considerando a sequência completa dos runs.

    Nenhuma formatação, tabela ou estrutura do modelo é recriada.
    """

    substituicoes = 0

    # O título fica dentro de uma célula da tabela da folha.
    # Percorremos todos os parágrafos das células para garantir que
    # o placeholder seja encontrado mesmo quando estiver em uma tabela.
    for tabela in documento.tables:
        for linha in tabela.rows:
            for celula in linha.cells:
                for paragrafo in celula.paragraphs:
                    if substituir_mes_ano_no_paragrafo(paragrafo):
                        substituicoes += 1

    # Também verificamos parágrafos que estejam diretamente no documento.
    for paragrafo in documento.paragraphs:
        if substituir_mes_ano_no_paragrafo(paragrafo):
            substituicoes += 1

    if substituicoes == 0:
        raise RuntimeError(
            "Não foi possível encontrar os placeholders {{MES}} e {{ANO}} "
            "no título da Folha de Frequência."
        )

    print(f"  Título atualizado para: {MES:02d}/{ANO}")


# ============================================================
# VALIDAR ESTRUTURA DA FREQUÊNCIA
# ============================================================

def validar_estrutura_frequencia(tabela, linha_teste):
    """Confirma se a linha dos dias possui as 21 células reais esperadas."""
    celulas = pegar_celulas_reais(tabela.rows[linha_teste])
    quantidade = len(celulas)

    if quantidade != 21:
        raise RuntimeError(
            "\nErro na estrutura da Folha de Frequência.\n\n"
            f"Esperado: 21 células reais por linha.\n"
            f"Encontrado: {quantidade}.\n\n"
            "A estrutura do modelo pode ter sido alterada."
        )

    print("Estrutura da Folha de Frequência: OK (21 células reais por linha)")


# ============================================================
# PREPARAR FOLHA DE FREQUÊNCIA
# ============================================================
def preparar_folha_frequencia(documento):
    """Prepara somente o calendário mensal da folha."""
    tabela = encontrar_frequencia(documento)
    linha_teste = encontrar_linha_do_dia(tabela, 3)
    if linha_teste is None:
        raise RuntimeError(
            "Não foi possível encontrar uma linha de dia para validar a Folha de Frequência."
        )

    validar_estrutura_frequencia(tabela, linha_teste)
    atualizar_calendario_frequencia(documento)


# ============================================================
# GERAR DOCUMENTO
# ============================================================

def gerar_documento():
    """
    Abre o modelo, preenche Grade e Frequência e salva
    um novo arquivo.
    """

    print()
    print(
        "=============================================="
    )
    print(
        "Abrindo modelo..."
    )
    print(
        "=============================================="
    )

    print(
        f"Modelo: {ARQUIVO_MODELO}"
    )

    # --------------------------------------------------------
    # Abrir o modelo.
    # --------------------------------------------------------

    documento = Document(
        ARQUIVO_MODELO
    )

    # --------------------------------------------------------
    # Cabeçalho do docente.
    # --------------------------------------------------------

    print()
    print(
        "Preenchendo cabeçalho do docente..."
    )

    preencher_cabecalho(
        documento
    )

    # --------------------------------------------------------
    # Grade.
    # --------------------------------------------------------

    print()
    print(
        "Preenchendo Grade Horária..."
    )

    preencher_grade(
        documento
    )

    # --------------------------------------------------------
    # Atualizar o título e o calendário da Folha de Frequência.
    # --------------------------------------------------------

    print()
    print(
        "Atualizando calendário..."
    )

    atualizar_titulo_frequencia(
        documento
    )

    atualizar_calendario_frequencia(
        documento
    )

    # --------------------------------------------------------
    # Folha de Frequência / calendário.
    # --------------------------------------------------------

    print()
    print(
        "Atualizando calendário da Folha de Frequência..."
    )

    preparar_folha_frequencia(
        documento
    )

    # --------------------------------------------------------
    # Salvar.
    # --------------------------------------------------------

    print()
    print(
        "Salvando documento..."
    )

    documento.save(
        ARQUIVO_SAIDA
    )

    print()
    print(
        "=============================================="
    )

    print(
        "DOCUMENTO GERADO COM SUCESSO!"
    )

    print(
        f"Arquivo: {ARQUIVO_SAIDA}"
    )

    print(
        "=============================================="
    )


# ============================================================
# MAIN
# ============================================================

def main():
    """
    Função principal do programa.
    """

    # --------------------------------------------------------
    # Verificar se o modelo existe.
    # --------------------------------------------------------

    if not ARQUIVO_MODELO.exists():

        print()
        print(
            "ERRO!"
        )

        print()
        print(
            "Não encontrei o arquivo:"
        )

        print(
            ARQUIVO_MODELO
        )

        print()
        print(
            "Coloque o arquivo "
            "'Folha de Ponto Exemplo.docx' "
            "na mesma pasta deste script."
        )

        return

    # --------------------------------------------------------
    # Gerar documento.
    # --------------------------------------------------------

    gerar_documento()


# ============================================================
# EXECUTAR PROGRAMA
# ============================================================

if __name__ == "__main__":

    main()
