import java.io.File

const val MENU_PRINCIPAL = 100
const val MENU_DEFINIR_TABULEIRO = 101
const val MENU_DEFINIR_NAVIOS = 102
const val MENU_JOGAR = 103
const val MENU_LER_FICHEIRO = 104
const val MENU_GRAVAR_FICHEIRO = 105
const val SAIR = 106

const val POSINV = "!!! Posicionamento inválido, tente novamente"
const val CORDS = "Coordenadas? (ex: 6,G)"
const val PRESS = "Prima enter para continuar"


var numLinhas = -1

var numColunas = -1

var tabuleiroHumano: Array<Array<Char?>> = emptyArray()

var tabuleiroComputador: Array<Array<Char?>> = emptyArray()

var tabuleiroPalpitesDoHumano: Array<Array<Char?>> = emptyArray()

var tabuleiroPalpitesDoComputador: Array<Array<Char?>> = emptyArray()

fun menuPrincipal(): Int {

    println("\n> > Batalha Naval < <\n")
    println("1 - Definir Tabuleiro e Navios")
    println("2 - Jogar")
    println("3 - Gravar")
    println("4 - Ler")
    println("0 - Sair\n")
    while (true) {
        val opcao = readlnOrNull()?.toIntOrNull() ?: -1
        when (opcao) {
            1 -> return MENU_DEFINIR_TABULEIRO
            2 -> {
                return if (numLinhas == -1) {
                    println("!!! Tem que primeiro definir o tabuleiro do jogo, tente novamente")
                    MENU_PRINCIPAL
                } else {
                    MENU_JOGAR
                }
            }

            3 -> return MENU_GRAVAR_FICHEIRO
            4 -> return MENU_LER_FICHEIRO
            0 -> return SAIR
            else -> println("!!! Opcao invalida, tente novamente")
        }
    }
}

fun tamanhoTabuleiroValido(numLinhas: Int, numColunas: Int): Boolean {

    return (numLinhas == 4 && numColunas == 4) ||
            (numLinhas == 5 && numColunas == 5) ||
            (numLinhas == 7 && numColunas == 7) ||
            (numLinhas == 8 && numColunas == 8) ||
            (numLinhas == 10 && numColunas == 10)
}

fun processaCoordenadas(coordenadas: String, numLinhas: Int, numColunas: Int): Pair<Int, Int>? {

    if (coordenadas.length <= 2 || !coordenadas[0].isDigit()) {
        return null
    }

    val linhaCoordenada = coordenadas.substringBefore(',')
    val linhaNumero = linhaCoordenada.toIntOrNull()

    val colunaCoordenada = coordenadas.substringAfter(',').trim()
    val colunaLetra = colunaCoordenada.firstOrNull()?.toUpperCase()

    if (linhaNumero != null && colunaLetra != null && colunaLetra in 'A'..'Z' &&
        linhaNumero in 1..numLinhas && colunaLetra - 'A' + 1 in 1..numColunas
    ) {

        return Pair(linhaNumero, colunaLetra - 'A' + 1)
    }
    return null
}

fun criaLegendaHorizontal(colunas: Int): String {

    val alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    var legenda = ""
    for (i in 0 until colunas) {
        val letra = alfabeto[i]
        if (i == 0) {
            legenda += "$letra"
        } else {
            legenda += " | $letra"
        }
    }

    return legenda
}

fun menuDefinirTabuleiro(): Int {

    println("\n> > Batalha Naval < <\n")
    println("Defina o tamanho do tabuleiro:")

    while (true) {
        println("Quantas linhas?")
        val linhas = readlnOrNull()?.toIntOrNull()

        if (linhas == -1) {
            return MENU_PRINCIPAL
        } else if (linhas == null || linhas <= 0) {
            println("!!! Numero de linhas invalidas, tente novamente")
        } else {
            println("Quantas colunas?")
            val colunas = readlnOrNull()?.toIntOrNull()

            if (colunas == -1) {
                return MENU_PRINCIPAL
            } else if (colunas == null || colunas <= 0) {
                println("!!! Numero de colunas invalidas, tente novamente")
            } else if (!tamanhoTabuleiroValido(linhas, colunas)) {
                println("!!! Tamanho do tabuleiro invalido, tente novamente")
            } else {

                numLinhas = linhas
                numColunas = colunas


                tabuleiroHumano = criaTabuleiroVazio(numLinhas, numColunas)
                tabuleiroPalpitesDoHumano = criaTabuleiroVazio(numLinhas, numColunas)
                tabuleiroComputador = criaTabuleiroVazio(numLinhas, numColunas)
                tabuleiroPalpitesDoComputador = criaTabuleiroVazio(numLinhas, numColunas)

                val terrenoTabuleiroHumano = obtemMapa(tabuleiroHumano, true)
                println(terrenoTabuleiroHumano.joinToString("\n"))

                val resultadoMenuNavios = menuDefinirNavios()

                return resultadoMenuNavios
            }
        }
    }
}

fun menuDefinirNavios(): Int {

    val navios = calculaNumNavios(tabuleiroHumano.size, tabuleiroHumano[0].size)
    val tiposNavio = arrayOf("submarino", "contra-torpedeiro", "navio-tanque", "porta-aviões")
    val dimensoesNavio = arrayOf(1, 2, 3, 4)

    for ((indice, tipoNavio) in tiposNavio.withIndex()) {
        var quantidadeNavios = navios[indice]
        while (quantidadeNavios > 0) {
            println("Insira as coordenadas de um $tipoNavio:")
            println("Coordenadas? (ex: 6,G)")
            val coordenadasStr = readLine()?.trim()
            val coordenadasPair =
                processaCoordenadas(coordenadasStr ?: "", tabuleiroHumano.size, tabuleiroHumano[0].size)
            if (coordenadasPair != null) {
                val orientacao = if (dimensoesNavio[indice] > 1) {
                    println("Insira a orientacao do navio:")
                    println("Orientacao? (N, S, E, O)")
                    readLine()?.trim() ?: ""
                } else "E"
                val inseridoComSucesso = insereNavio(
                    tabuleiroHumano, coordenadasPair.first, coordenadasPair.second,
                    orientacao, dimensoesNavio[indice]
                )
                if (inseridoComSucesso) {
                    println(obtemMapa(tabuleiroHumano, true).joinToString("\n"))
                    quantidadeNavios--
                } else println(POSINV)
            } else println(POSINV)
        }
    }

    println("Pretende ver o mapa gerado para o Computador? (S/N)")
    val opcaoVerTabuleiro = readlnOrNull()?.toUpperCase() ?: ""
    tabuleiroComputador = criaTabuleiroVazio(numLinhas, numColunas)
    preencheTabuleiroComputador(tabuleiroComputador, calculaNumNavios(numLinhas, numColunas))
    if (opcaoVerTabuleiro == "S") println(obtemMapa(tabuleiroComputador, true).joinToString("\n"))

    return MENU_PRINCIPAL
}

fun calculaNumNavios(numLinhas: Int, numColunas: Int): Array<Int> {

    return when {
        numLinhas == 4 && numColunas == 4 -> arrayOf(2, 0, 0, 0)
        numLinhas == 5 && numColunas == 5 -> arrayOf(1, 1, 1, 0)
        numLinhas == 7 && numColunas == 7 -> arrayOf(2, 1, 1, 1)
        numLinhas == 8 && numColunas == 8 -> arrayOf(2, 2, 1, 1)
        numLinhas == 10 && numColunas == 10 -> arrayOf(3, 2, 1, 1)
        else -> arrayOf()
    }
}

fun criaTabuleiroVazio(numLinhas: Int, numColunas: Int): Array<Array<Char?>> {
    return Array(numLinhas) { Array(numColunas) { null } }
}

fun coordenadaContida(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {
    val linhasValidas = linha in 1 until tabuleiro.size + 1
    val colunasValidas = coluna in 1 until tabuleiro[0].size + 1

    return linhasValidas && colunasValidas
}

fun limparCoordenadasVazias(coordenadas: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {

    var contadorNaoVazias = 0

    for ((linha, coluna) in coordenadas) {
        if (linha != 0 || coluna != 0) {
            contadorNaoVazias++
        }
    }

    val coordenadasNaoVazias = Array<Pair<Int, Int>>(contadorNaoVazias) { Pair(0, 0) }

    var indice = 0
    for ((linha, coluna) in coordenadas) {
        if (linha != 0 || coluna != 0) {
            coordenadasNaoVazias[indice++] = Pair(linha, coluna)
        }
    }
    return coordenadasNaoVazias
}

fun juntarCoordenadas(coordenadas1: Array<Pair<Int, Int>>, coordenadas2: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {

    val tamanhoTotal = coordenadas1.size + coordenadas2.size

    val resultado = Array<Pair<Int, Int>>(tamanhoTotal) { Pair(0, 0) }


    for (indice in coordenadas1.indices) {
        resultado[indice] = coordenadas1[indice]
    }


    for (indice in coordenadas2.indices) {
        resultado[coordenadas1.size + indice] = coordenadas2[indice]
    }
    return resultado
}

fun gerarCoordenadasNavio(
    tabuleiroVazio: Array<Array<Char?>>,
    linha: Int,
    coluna: Int,
    orientacao: String,
    dimensao: Int
): Array<Pair<Int, Int>> {
    val coordenadasNavio = Array(dimensao) { Pair(0, 0) }

    for (indice in 0 until dimensao) {
        when (orientacao) {
            "N" -> {
                val novaLinha = linha - indice
                if (coordenadaContida(tabuleiroVazio, novaLinha, coluna)) {
                    coordenadasNavio[indice] = Pair(novaLinha, coluna)
                } else {
                    return arrayOf()
                }
            }

            "S" -> {
                val novaLinha = linha + indice
                if (coordenadaContida(tabuleiroVazio, novaLinha, coluna)) {
                    coordenadasNavio[indice] = Pair(novaLinha, coluna)
                } else {
                    return arrayOf()
                }
            }

            "E" -> {
                val novaColuna = coluna + indice
                if (coordenadaContida(tabuleiroVazio, linha, novaColuna)) {
                    coordenadasNavio[indice] = Pair(linha, novaColuna)
                } else {
                    return arrayOf()
                }
            }

            "O" -> {
                val novaColuna = coluna - indice
                if (coordenadaContida(tabuleiroVazio, linha, novaColuna)) {
                    coordenadasNavio[indice] = Pair(linha, novaColuna)
                } else {
                    return arrayOf()
                }
            }
        }
    }

    return coordenadasNavio
}

//Funcao não está bem implementada
fun gerarCoordenadasFronteira(
    tabuleiro: Array<Array<Char?>>,
    linha: Int,
    coluna: Int,
    orientacao: String,
    dimensao: Int
): Array<Pair<Int, Int>> {
    var tamanhoArray = 8

    if (dimensao > 1) {
        tamanhoArray += (dimensao - 1) * 2
    }
    val coordenadasNavio = Array(tamanhoArray) { Pair(0, 0) }
    if (coordenadaContida(tabuleiro, linha + 1, coluna)) {
        coordenadasNavio[0] = Pair(linha + 1, coluna)
    }
    if (coordenadaContida(tabuleiro, linha - 1, coluna)) {
        coordenadasNavio[1] = Pair(linha - 1, coluna)
    }
    if (coordenadaContida(tabuleiro, linha, coluna + 1)) {
        coordenadasNavio[2] = Pair(linha, coluna + 1)
    }
    if (coordenadaContida(tabuleiro, linha, coluna - 1)) {
        coordenadasNavio[3] = Pair(linha, coluna - 1)
    }
    if (coordenadaContida(tabuleiro, linha + 1, coluna + 1)) {
        coordenadasNavio[4] = Pair(linha + 1, coluna + 1)
    }
    if (coordenadaContida(tabuleiro, linha - 1, coluna - 1)) {
        coordenadasNavio[5] = Pair(linha - 1, coluna - 1)
    }
    if (coordenadaContida(tabuleiro, linha + 1, coluna - 1)) {
        coordenadasNavio[6] = Pair(linha + 1, coluna - 1)
    }
    if (coordenadaContida(tabuleiro, linha - 1, coluna + 1)) {
        coordenadasNavio[7] = Pair(linha - 1, coluna + 1)
    }

    return limparCoordenadasVazias(coordenadasNavio)

}

fun estaLivre(tabuleiro: Array<Array<Char?>>, coordenadas: Array<Pair<Int, Int>>): Boolean {
    return coordenadas.all { (linha, coluna) ->
        coordenadaContida(tabuleiro, linha, coluna) &&
                tabuleiro[linha - 1][coluna - 1] == null
    }
}

fun insereNavioSimples(tabuleiroVazio: Array<Array<Char?>>, linha: Int, coluna: Int, dimensao: Int) =
    insereNavio(tabuleiroVazio, linha, coluna, "E", dimensao)

fun insereNavio(
    tabuleiroVazio: Array<Array<Char?>>,
    linha: Int,
    coluna: Int,
    orientacao: String,
    dimensao: Int
): Boolean {
    if (!coordenadaContida(tabuleiroVazio, linha, coluna)) {
        return false
    }

    val coordenadasNavio = gerarCoordenadasNavio(tabuleiroVazio, linha, coluna, orientacao, dimensao)
    val coordenadasFronteira = gerarCoordenadasFronteira(tabuleiroVazio, linha, coluna, orientacao, dimensao)
    val coordenadasTotais = juntarCoordenadas(coordenadasNavio, coordenadasFronteira)

    if (estaLivre(tabuleiroVazio, coordenadasTotais)) {

        for ((index, coordenada) in coordenadasTotais.withIndex()) {
            val linhaNavio = coordenada.first - 1
            val colunaNavio = coordenada.second - 1


            if (index < dimensao) {
                tabuleiroVazio[linhaNavio][colunaNavio] = (dimensao + '0'.toInt()).toChar()
            }
        }

        return true
    }

    return false
}

fun preencheTabuleiroComputador(tabuleiro: Array<Array<Char?>>, navios: Array<Int>) {
    val direcoes = arrayOf("N", "S", "E", "O")
    val random = java.util.Random()

    var dimensao = 0

    while (dimensao < navios.size) {
        var quantidadeNavios = navios[dimensao]

        while (quantidadeNavios > 0) {
            var navioInserido = false

            while (!navioInserido) {
                val linha = random.nextInt(tabuleiro.size)
                val coluna = random.nextInt(tabuleiro[0].size)
                val orientacao = direcoes.random()

                navioInserido = insereNavio(tabuleiro, linha, coluna, orientacao, dimensao + 1)
            }

            quantidadeNavios--
        }

        dimensao++
    }
}

fun navioCompleto(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {
    if (!coordenadaContida(tabuleiro, linha, coluna)) {
        return false
    }
    if (tabuleiro[linha - 1][coluna - 1] == '1') {
        return true
    }

    val navio = tabuleiro[linha - 1][coluna - 1]
    val tamanhoNavio = when (navio) {
        '1' -> 1
        '2' -> 2
        '3' -> 3
        '4' -> 4
        else -> return false
    }


    var navioHorizontal = true
    for (i in 0 until tamanhoNavio) {
        navioHorizontal = navioHorizontal && coordenadaContida(
            tabuleiro,
            linha,
            coluna + i
        ) && tabuleiro[linha - 1][coluna - 1 + i] == navio
    }


    var navioVertical = true
    for (i in 0 until tamanhoNavio) {
        navioVertical = navioVertical && coordenadaContida(
            tabuleiro,
            linha + i,
            coluna
        ) && tabuleiro[linha - 1 + i][coluna - 1] == navio
    }


    return navioHorizontal || navioVertical
}

fun obtemMapa(tabuleiro: Array<Array<Char?>>, tabuleiroReal: Boolean): Array<String> {

    val legendaHorizontal = "| " + criaLegendaHorizontal(tabuleiro[0].size) + " |"


    var mapa = arrayOf(legendaHorizontal)

    for (i in 0 until tabuleiro.size) {
        var linhaMapa = "| "

        for (j in 0 until tabuleiro[i].size) {
            val elemento = tabuleiro[i][j]
            val caracter = when {
                tabuleiroReal -> when (elemento) {
                    null -> "~"
                    'S' -> "~"
                    else -> elemento.toString()
                }

                else -> when {
                    elemento == 'X' -> "X"
                    elemento in arrayOf('1', '2', '3', '4') -> elemento.toString()
                    else -> "?"
                }
            }
            linhaMapa += "$caracter | "
        }
        linhaMapa += "${i + 1}"

        val novoMapa = Array(mapa.size + 1) { "" }

        mapa.copyInto(novoMapa)

        novoMapa[mapa.size] = linhaMapa

        mapa = novoMapa
    }

    return mapa
}

fun lancarTiro(
    tabuleiroAdversario: Array<Array<Char?>>,
    tabuleiroPalpite: Array<Array<Char?>>,
    coordenadas: Pair<Int, Int>
): String {

    val linha = coordenadas.first
    val coluna = coordenadas.second

    if (tabuleiroAdversario[linha - 1][coluna - 1] == null) {
        tabuleiroPalpite[linha - 1][coluna - 1] = 'X'
        return "Agua."
    } else {
        when (tabuleiroAdversario[linha - 1][coluna - 1]) {
            '1' -> {
                tabuleiroPalpite[linha - 1][coluna - 1] = '1'
                return "Tiro num submarino."
            }

            '2' -> {
                tabuleiroPalpite[linha - 1][coluna - 1] = '2'
                return "Tiro num contra-torpedeiro."
            }

            '3' -> {
                tabuleiroPalpite[linha - 1][coluna - 1] = '3'
                return "Tiro num navio-tanque."
            }

            '4' -> {
                tabuleiroPalpite[linha - 1][coluna - 1] = '4'
                return "Tiro num porta-avioes."
            }
        }
    }
    return ""
}

fun geraTiroComputador(tabuleiroPalpiteHumano: Array<Array<Char?>>): Pair<Int, Int> {

    var linha = 1
    var coluna = 1

    do {
        linha = (1..tabuleiroPalpiteHumano.size).random()
        coluna = (1..tabuleiroPalpiteHumano[0].size).random()

    } while (tabuleiroPalpiteHumano[linha - 1][coluna - 1] != null)

    return Pair(linha, coluna)
}

fun contarNaviosDeDimensao(tabuleiroPalpite: Array<Array<Char?>>, dimensao: Int): Int {
    var count = 0
    val navio = dimensao.toString()[0]
    for (linha in 0 until tabuleiroPalpite.size) {
        for (coluna in 0 until tabuleiroPalpite[linha].size) {
            if (tabuleiroPalpite[linha][coluna] == navio) {
                var navioCompletoHorizontal = true
                var navioCompletoVertical = true
                for (i in 0 until dimensao) {
                    navioCompletoHorizontal = navioCompletoHorizontal &&
                            coordenadaContida(tabuleiroPalpite, linha + 1 + i, coluna + 1) &&
                            tabuleiroPalpite[linha + i][coluna] == navio
                    navioCompletoVertical = navioCompletoVertical &&
                            coordenadaContida(tabuleiroPalpite, linha + 1, coluna + 1 + i) &&
                            tabuleiroPalpite[linha][coluna + i] == navio
                }
                if (navioCompletoHorizontal || navioCompletoVertical) {
                    count++
                }
            }
        }
    }
    return count
}

fun venceu(tabuleiroPalpite: Array<Array<Char?>>): Boolean {
    val navios = calculaNumNavios(tabuleiroPalpite.size, tabuleiroPalpite[0].size)
    var count = 0
    for (posicao in navios) {
        count += posicao
    }
    var numNavios = 0
    for (dimensao in 1..4) {
        numNavios += contarNaviosDeDimensao(tabuleiroPalpite, dimensao)
    }
    return count == numNavios
}

fun menuJogar(): Int {
    while (!venceu(tabuleiroPalpitesDoHumano)) {
        val mapa = obtemMapa(tabuleiroPalpitesDoHumano, false)
        for (string in mapa) {
            println(string)
        }
        println("Indique a posição que pretende atingir")
        println(CORDS)
        var coordenadas = readln()
        if (coordenadas.toIntOrNull() == -1) return MENU_PRINCIPAL
        while (processaCoordenadas(coordenadas, numLinhas, numColunas) == null) {
            println("!!! Coordenadas invalidas, tente novamente")
            println(CORDS)
            coordenadas = readln()
        }
        val info = processaCoordenadas(coordenadas, numLinhas, numColunas)
        if (info != null) {
            val tiro = lancarTiro(tabuleiroComputador, tabuleiroPalpitesDoHumano, info)
            print(">>> HUMANO >>>")
            var navioAoFundo = false
            var dimensao = 1
            while (!navioAoFundo && dimensao <= 4) {
                navioAoFundo = navioCompleto(tabuleiroPalpitesDoHumano, info.first, info.second)
                dimensao++
            }
            if (navioAoFundo) {
                println("$tiro Navio ao fundo!")
            } else {
                println(tiro)
            }
            if (venceu(tabuleiroPalpitesDoHumano)) {
                println("PARABENS! Venceu o jogo!")
                println("Prima enter para voltar ao menu principal")
                var opcao = readln()
                while (opcao.isNotEmpty()) {
                    println(PRESS)
                    opcao = readln()
                }

                return MENU_PRINCIPAL
            }
            val tiroComputador = geraTiroComputador(tabuleiroHumano)
            print("Computador lancou tiro para a posicao ")
            println(tiroComputador)
            print(">>> COMPUTADOR >>>")
            val resultadoTiro = lancarTiro(tabuleiroHumano, tabuleiroPalpitesDoComputador, tiroComputador)
            if (navioCompleto(tabuleiroPalpitesDoComputador, tiroComputador.first + 1, tiroComputador.second + 1)) {
                println("$resultadoTiro Navio ao fundo!")
            } else {
                println(resultadoTiro)
            }
            println(PRESS)
            var opcao = readln()
            while (opcao.isNotEmpty()) {
                println(PRESS)
                opcao = readln()
            }
        }
    }
    return MENU_PRINCIPAL
}

fun gravarJogo(
    nomeArquivo: String,
    tabuleiroRealHumano: Array<Array<Char?>>,
    tabuleiroPalpitesHumano: Array<Array<Char?>>,
    tabuleiroRealComputador: Array<Array<Char?>>,
    tabuleiroPalpitesComputador: Array<Array<Char?>>
) {
    val file = File(nomeArquivo)
    var texto = ""


    texto += "${tabuleiroRealHumano.size},${tabuleiroRealHumano[0].size}\n"


    texto += "Jogador\n"
    texto += "Real\n"
    for (linha in tabuleiroRealHumano) {
        texto += linha.map { it ?: "" }.joinToString(",") + "\n"
    }

    texto += "Palpites\n"
    for (linha in tabuleiroPalpitesHumano) {
        texto += linha.map { it ?: "" }.joinToString(",") + "\n"
    }

    texto += "Computador\n"
    texto += "Real\n"
    for (linha in tabuleiroRealComputador) {
        texto += linha.map { it ?: "" }.joinToString(",") + "\n"
    }

    texto += "Palpites\n"
    for (linha in tabuleiroPalpitesComputador) {
        texto += linha.map { it ?: "" }.joinToString(",") + "\n"
    }

    file.writeText(texto)
}


fun menuGravarFicheiro(): Int {
    println("Introduza o nome do ficheiro (ex: jogo.txt)")
    val nomeArquivo = readlnOrNull() ?: return MENU_PRINCIPAL
    gravarJogo(
        nomeArquivo,
        tabuleiroHumano,
        tabuleiroPalpitesDoHumano,
        tabuleiroComputador,
        tabuleiroPalpitesDoComputador
    )
    println("Tabuleiro ${tabuleiroHumano.size}x${tabuleiroHumano[0].size} gravado com sucesso")
    return MENU_PRINCIPAL
}

fun lerJogo(nomeArquivo: String, tipoTabuleiro: Int): Array<Array<Char?>> {
    val file = File(nomeArquivo)
    val linhas = file.readLines()
    val tamanhoTabuleiro = linhas[0].split(",").map { it.toInt() }
    val inicioTabuleiro = 2 + (tipoTabuleiro - 1) * (tamanhoTabuleiro[0] + 2)
    val tabuleiro = Array(tamanhoTabuleiro[0]) { Array<Char?>(tamanhoTabuleiro[1]) { null } }
    for (i in 0 until tamanhoTabuleiro[0]) {
        val linha = linhas[inicioTabuleiro + i].split(",")
        for (j in 0 until tamanhoTabuleiro[1]) {
            tabuleiro[i][j] = linha[j].firstOrNull()
        }
    }
    return tabuleiro
}

fun menuLerFicheiro(): Int {
    println("Introduza o nome do ficheiro (ex: jogo.txt)")
    val nomeArquivo = readlnOrNull() ?: return MENU_PRINCIPAL
    tabuleiroHumano = lerJogo(nomeArquivo, 1)
    tabuleiroPalpitesDoHumano = lerJogo(nomeArquivo, 2)
    tabuleiroComputador = lerJogo(nomeArquivo, 3)
    tabuleiroPalpitesDoComputador = lerJogo(nomeArquivo, 4)
    println("Tabuleiro ${tabuleiroHumano.size}x${tabuleiroHumano[0].size} lido com sucesso")
    return MENU_PRINCIPAL
}

fun main() {
    var menuAtual = MENU_PRINCIPAL

    while (true) {

        menuAtual = when (menuAtual) {
            MENU_PRINCIPAL -> menuPrincipal()
            MENU_DEFINIR_TABULEIRO -> menuDefinirTabuleiro()
            MENU_DEFINIR_NAVIOS -> menuDefinirNavios()
            MENU_JOGAR -> menuJogar()
            MENU_LER_FICHEIRO -> menuLerFicheiro()
            MENU_GRAVAR_FICHEIRO -> menuGravarFicheiro()
            SAIR -> return
            else -> return
        }
    }
}
