import java.io.File
import java.io.PrintWriter

const val MENU_PRINCIPAL = 100
const val MENU_DEFINIR_TABULEIRO = 101
const val MENU_DEFINIR_NAVIOS = 102
const val MENU_JOGAR = 103
const val MENU_LER_FICHEIRO = 104
const val MENU_GRAVAR_FICHEIRO = 105
const val SAIR = 106


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
    val colunaLetra = colunaCoordenada.firstOrNull()?.uppercaseChar()

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

                return menuDefinirNavios()
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
                } else println("!!! Posicionamento inválido, tente novamente")
            } else println("!!! Posicionamento inválido, tente novamente")
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

    var count = 0

    for ((linha, coluna) in coordenadas) {
        if (linha != 0 || coluna != 0) {
            count++
        }
    }

    val coordenadasNaoVazias = Array<Pair<Int, Int>>(count) { Pair(0, 0) }

    count = 0
    for ((linha, coluna) in coordenadas) {
        if (linha != 0 || coluna != 0) {
            coordenadasNaoVazias[count++] = Pair(linha, coluna)
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

    for (indice in 0..<dimensao) {
        when (orientacao) {
            "N" -> {
                val novaLinha = linha - indice
                if (coordenadaContida(tabuleiroVazio, novaLinha, coluna)) {
                    coordenadasNavio[indice] = Pair(novaLinha, coluna)
                } else {
                    return emptyArray()
                }
            }

            "S" -> {
                val novaLinha = linha + indice
                if (coordenadaContida(tabuleiroVazio, novaLinha, coluna)) {
                    coordenadasNavio[indice] = Pair(novaLinha, coluna)
                } else {
                    return emptyArray()
                }
            }

            "E" -> {
                val novaColuna = coluna + indice
                if (coordenadaContida(tabuleiroVazio, linha, novaColuna)) {
                    coordenadasNavio[indice] = Pair(linha, novaColuna)
                } else {
                    return emptyArray()
                }
            }

            "O" -> {
                val novaColuna = coluna - indice
                if (coordenadaContida(tabuleiroVazio, linha, novaColuna)) {
                    coordenadasNavio[indice] = Pair(linha, novaColuna)
                } else {
                    return emptyArray()
                }
            }
        }
    }

    return coordenadasNavio
}

//Funcao não está bem implementada
fun gerarCoordenadasFronteira(
    tabuleiro: Array<Array<Char?>>, numLinhas: Int,
    numColunas: Int, orientacao: String, dimensao: Int
): Array<Pair<Int, Int>> {
    var resultado = emptyArray<Pair<Int, Int>>()
    for (count in -1..1) {
        for (count1 in -1..dimensao) {
            val coordenada = when (orientacao) {
                "E" -> Pair(numLinhas + count, numColunas + count1)
                "O" -> Pair(numLinhas + count, numColunas - count1)
                "S" -> Pair(numLinhas + count1, numColunas + count)
                "N" -> Pair(numLinhas - count1, numColunas + count)
                else -> null
            }

            if (coordenada != null && coordenadaContida(tabuleiro, coordenada.first, coordenada.second)) {
                resultado = juntarCoordenadas(resultado, arrayOf(coordenada))
            }
        }
    }
    val navio = gerarCoordenadasNavio(tabuleiro, numLinhas, numColunas, orientacao, dimensao)
    if (navio.size > 0) {
        for (count in 0..navio.size - 1) {
            for (count1 in 0..resultado.size - 1) {
                when {
                    resultado[count1] == navio[count] -> resultado[count1] = Pair(0, 0)
                }
            }
        }
        return limparCoordenadasVazias(resultado)
    }
    return emptyArray()
}

fun estaLivre(tabuleiro: Array<Array<Char?>>, coordenadas: Array<Pair<Int, Int>>): Boolean {
    return coordenadas.all { (linha, coluna) ->
        coordenadaContida(tabuleiro, linha, coluna) &&
                tabuleiro[linha - 1][coluna - 1] == null
    }
}

// A sua utilização apenas serve para os submarinos
fun insereNavioSimples(tabuleiro: Array<Array<Char?>>, numLinhas: Int, numColunas: Int, dimensao: Int): Boolean {
    val navio = juntarCoordenadas(
        gerarCoordenadasNavio(tabuleiro, numLinhas, numColunas, "E", dimensao),
        gerarCoordenadasFronteira(tabuleiro, numLinhas, numColunas, "E", dimensao)
    )
    if (navio.size > 0) {
        when {
            estaLivre(tabuleiro, navio) -> {
                for (count in 0..dimensao - 1) {
                    tabuleiro[numLinhas - 1][numColunas + count - 1] = (dimensao + 48).toChar()
                }
                return true
            }

        }
    }
    return false
}

fun insereNavio(
    tabuleiro: Array<Array<Char?>>, numLinhas: Int, numColunas: Int, orientacao: String,
    dimensao: Int
): Boolean {
    val navio = juntarCoordenadas(
        gerarCoordenadasNavio(tabuleiro, numLinhas, numColunas, orientacao, dimensao),
        gerarCoordenadasFronteira(tabuleiro, numLinhas, numColunas, orientacao, dimensao)
    )
    if (navio.size > 0) {
        when {
            estaLivre(tabuleiro, navio) == true -> {
                for (count in 0..dimensao - 1) {
                    when (orientacao) {
                        "E" -> tabuleiro[numLinhas - 1][numColunas + count - 1] = (dimensao + 48).toChar()
                        "O" -> tabuleiro[numLinhas - 1][numColunas - count - 1] = (dimensao + 48).toChar()
                        "S" -> tabuleiro[numLinhas + count - 1][numColunas - 1] = (dimensao + 48).toChar()
                        "N" -> tabuleiro[numLinhas - count - 1][numColunas - 1] = (dimensao + 48).toChar()
                    }

                }
                return true
            }

        }
        return false
    }
    return false
}

fun preencheTabuleiroComputador(tabuleiro: Array<Array<Char?>>, numNavios: Array<Int>) {
    for (dimensao in 4 downTo 1) {
        for (i in 0 until numNavios[dimensao - 1]) {
            var sucesso = false
            while (!sucesso) {
                val numLinhas = (1..tabuleiro.size).random()
                val numColunas = (1..tabuleiro[numLinhas - 1].size).random()
                val orientacao = when ((0..3).random()) {
                    0 -> "E"
                    1 -> "O"
                    2 -> "S"
                    else -> "N"
                }

                sucesso = coordenadaContida(tabuleiro, numLinhas, numColunas) &&
                        insereNavio(tabuleiro, numLinhas, numColunas, orientacao, dimensao)
            }
        }
    }
}

fun navioCompleto(tabuleiroPalpites: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {

    var coordenadas = emptyArray<Pair<Int, Int>>()

    if (coordenadaContida(tabuleiroPalpites, linha, coluna)) {

        when (val posicaoAtingida = tabuleiroPalpites[linha - 1][coluna - 1]) {
            null, 'X' -> return false
            '1' -> return true
            '2', '3', '4' -> {
                val dimensao = posicaoAtingida.toString().toInt() - 1

                for (colunas in coluna - dimensao..coluna + dimensao) {
                    if (coordenadaContida(tabuleiroPalpites, linha, colunas)) {
                        if (tabuleiroPalpites[linha - 1][colunas - 1] == posicaoAtingida &&
                            Pair(linha, colunas) !in coordenadas
                        ) {
                            coordenadas += Pair(linha, colunas)
                        }
                    }
                }
                if (coordenadas.size == posicaoAtingida.toString().toInt()) return true

                for (linhas in linha - dimensao..linha + dimensao) {
                    if (coordenadaContida(tabuleiroPalpites, linhas, coluna)) {
                        if (tabuleiroPalpites[linhas - 1][coluna - 1] == posicaoAtingida &&
                            Pair(linhas, coluna) !in coordenadas
                        ) {
                            coordenadas += Pair(linhas, coluna)
                        }

                    }
                }
                return coordenadas.size == posicaoAtingida.toString().toInt()
            }
        }
    }
    return false
}

fun obtemMapa(tabuleiro: Array<Array<Char?>>, tabuleiroReal: Boolean): Array<String> {
    val legendaHorizontal = "| " + criaLegendaHorizontal(tabuleiro[0].size) + " |"
    val mapa = Array(tabuleiro.size + 1) { "" }
    mapa[0] = legendaHorizontal

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

                else -> when (elemento) {
                    '4' -> if (!navioCompleto(tabuleiro, i + 1, j + 1)) '\u2084' else '4'
                    '3' -> if (!navioCompleto(tabuleiro, i + 1, j + 1)) '\u2083' else '3'
                    '2' -> if (!navioCompleto(tabuleiro, i + 1, j + 1)) '\u2082' else '2'
                    else -> tabuleiro[i][j]?.toString() ?: '?'
                }
            }
            linhaMapa += "$caracter | "
        }
        linhaMapa += "${i + 1}"
        mapa[i + 1] = linhaMapa
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
        return "Água."
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
                return "Tiro num porta-aviões."
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

    lancarTiro(
        tabuleiroPalpiteHumano,
        criaTabuleiroVazio(tabuleiroPalpiteHumano.size, tabuleiroPalpiteHumano.size),
        Pair(linha, coluna)
    )
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
        println("Coordenadas? (ex: 6,G)")
        var coordenadas = readln()
        if (coordenadas.toIntOrNull() == -1) return MENU_PRINCIPAL
        while (processaCoordenadas(coordenadas, numLinhas, numColunas) == null) {
            println("!!! Coordenadas invalidas, tente novamente")
            println("Coordenadas? (ex: 6,G)")
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
                    println("Prima enter para continuar")
                    opcao = readln()
                }

                return MENU_PRINCIPAL
            }
            val tiroComputador = geraTiroComputador(tabuleiroHumano)
            print("Computador lancou tiro para a posição ")
            println(tiroComputador)
            print(">>> COMPUTADOR >>>")
            val resultadoTiro = lancarTiro(tabuleiroHumano, tabuleiroPalpitesDoComputador, tiroComputador)
            if (navioCompleto(tabuleiroPalpitesDoComputador, tiroComputador.first + 1, tiroComputador.second + 1)) {
                println("$resultadoTiro Navio ao fundo!")
            } else {
                println(resultadoTiro)
            }
            println("Prima enter para continuar")
            var opcao = readln()
            while (opcao.isNotEmpty()) {
                println("Prima enter para continuar")
                opcao = readln()
            }
        }
    }
    return MENU_PRINCIPAL
}

fun lerJogo(ficheiro: String, tipoTabuleiro: Int): Array<Array<Char?>> {
    val ficheiroGravado = File(ficheiro).readLines()

    numLinhas = ficheiroGravado[0][0].digitToInt()
    numColunas = ficheiroGravado[0][2].digitToInt()

    val linhasNoFicheiro =
        3 * tipoTabuleiro + 1 + numLinhas * (tipoTabuleiro - 1) until 3 * tipoTabuleiro + 1 + numLinhas * tipoTabuleiro
    val tabuleiro = criaTabuleiroVazio(numLinhas, numColunas)
    var linha = linhasNoFicheiro.min()
    while (linha in linhasNoFicheiro) {
        var coluna = 0
        var contaVirgulas = 0
        while (contaVirgulas < numColunas && coluna < ficheiroGravado[linha].length) {
            when (ficheiroGravado[linha][coluna]) {
                ',' -> contaVirgulas++
                '1', '2', '3', '4', 'X' -> tabuleiro[linha - linhasNoFicheiro.min()][contaVirgulas] =
                    ficheiroGravado[linha][coluna]
            }
            coluna++
        }
        linha++
    }
    when (tipoTabuleiro) {
        1 -> tabuleiroHumano = tabuleiro
        2 -> tabuleiroPalpitesDoHumano = tabuleiro
        3 -> tabuleiroComputador = tabuleiro
        4 -> tabuleiroPalpitesDoComputador = tabuleiro
    }
    return tabuleiro
}

fun imprimirTabuleiro(filePrinter: PrintWriter, tabuleiro: Array<Array<Char?>>) {
    for (linha in tabuleiro) {
        var count = 1
        for (coluna in linha) {
            if (count != tabuleiro.size) {
                filePrinter.print("${coluna ?: ""},")
                count++
            } else {
                filePrinter.print("${coluna ?: ""}")
            }
        }
        filePrinter.println()
    }
}

fun gravarJogo(
    ficheiro: String, tabuleiroRealHumano: Array<Array<Char?>>, tabuleiroPalpitesDoHumano: Array<Array<Char?>>,
    tabuleiroRealComputador: Array<Array<Char?>>, tabuleiroPalpitesDoComputador: Array<Array<Char?>>
) {
    val filePrinter = File(ficheiro).printWriter()

    filePrinter.println("${tabuleiroRealHumano.size},${tabuleiroRealHumano[0].size}\n")

    filePrinter.println("Jogador\nReal")
    imprimirTabuleiro(filePrinter, tabuleiroRealHumano)

    filePrinter.println("\nJogador\nPalpites")
    imprimirTabuleiro(filePrinter, tabuleiroPalpitesDoHumano)

    filePrinter.println("\nComputador\nReal")
    imprimirTabuleiro(filePrinter, tabuleiroRealComputador)

    filePrinter.println("\nComputador\nPalpites")
    imprimirTabuleiro(filePrinter, tabuleiroPalpitesDoComputador)

    filePrinter.close()
}

fun menuLerFicheiro(): Int {
    println("Introduza o nome do ficheiro (ex: jogo.txt)")
    val ficheiro = readln()
    lerJogo(ficheiro, 1)
    lerJogo(ficheiro, 2)
    lerJogo(ficheiro, 3)
    lerJogo(ficheiro, 4)
    println("Tabuleiro ${lerJogo(ficheiro, 1).size}x${lerJogo(ficheiro, 1).size} lido com sucesso")
    for (mapa in obtemMapa(tabuleiroHumano, true)) println(mapa)
    return MENU_PRINCIPAL
}

fun menuGravarFicheiro(): Int {
    if (tabuleiroHumano.isEmpty() || tabuleiroComputador.isEmpty()) {
        println("!!! Tem que primeiro definir o tabuleiro do jogo, tente novamente")
        return MENU_PRINCIPAL
    }
    println("Introduza o nome do ficheiro (ex: jogo.txt)")
    val ficheiro = readln()
    gravarJogo(ficheiro, tabuleiroHumano, tabuleiroPalpitesDoHumano, tabuleiroComputador, tabuleiroPalpitesDoComputador)
    println("Tabuleiro ${tabuleiroHumano.size}x${tabuleiroHumano[0].size} gravado com sucesso")
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
