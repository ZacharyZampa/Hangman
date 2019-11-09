import java.util.Scanner;
import errors as errors

val alpha:HashMap<Char,Boolean> = HashMap<Char,Boolean>(26)
var errors : Int = 0  // how many errors guessing player has made
const val CRITERRORS = 6  // 6 errors is game over

fun generateAlpha() {
    for (i in 65..90) {
        alpha[i.toChar()] = false
    }
}

fun isValidLetter(move: Char) : Boolean {
    // check if move is in the alphabet and not played yet
    var fmove = move.toUpperCase()
    var flag = (alpha.contains(fmove) && !alpha.getValue(fmove))
    if (flag) {
        // being played so set as played in map
        alpha[fmove] = true
    }

    return flag
}

fun playerMove(word: String, input: Scanner): Char {
    var move = '0'
    while (input.hasNextLine()) {
        var fmove = readLine()
        move = fmove!![0]
        if (isValidLetter(move)) {
            // valid letter move that has not been played yet
            break;
        }
    }


    if (move != '0' && word.contains(move)) {
        return move;
    }

    // word does not contain that move, increase number of errors
    errors++
    return move;
}

fun turnChoose(input: Scanner, word: String, playWord: String): String {
    var move = playerMove(word, input)
    var tmpPlay = playWord.toCharArray()

    if (word.contains(move)) {
        // add the correctly guessed letter to the word in progress
        for (i in 0 until word.length) {
            if (word[i].equals(move, true)) {
                tmpPlay[i] = move
            }
        }
    }

    return String(tmpPlay)
}

fun isWin(word : String, playWord : String) : Boolean{
    return (word.equals(playWord, ignoreCase = true))
}

fun checkEndConditions(winFlag : Boolean) {
    if (!winFlag) {
        // Person making the word wins
        println("The word-maker wins!")
        return
    }

    // Local Player won
    println("The word-guesser wins!")
}

fun gameLoop(input : Scanner, side : Int, lan : Online) {
    var turn = 0
    var word = ""
    var playWord = ""

    if (side == 1) {
        println("..Waiting for Challenger's word")
    }

    if (side == 0) {
        // host -- make word
        println("Enter the challenge word")
        word = readLine().toString()
        playWord = makePlayWord(word)
        lan.sendLANResponse(word)
        println("Word entered - Now waiting for opponent's response")
    } else {
        // client -- get word to beat
        word = lan.processLANResponse()

        playWord = makePlayWord(word);
        println(playWord)
    }
    var tword = playWord
    // loop while the game is in session
    while (!isWin(word, playWord)) {
        if (turn % 2 != 0 && side == 1) {
            // client turn
            playWord = turnChoose(input, word, playWord)
            lan.sendLANResponse(playWord)
            println("Current progress is: $playWord")
            println("Errors: $errors out of $CRITERRORS")
        } else if (side == 0) {
            playWord = lan.processLANResponse()
            if (playWord.equals(tword)) {
                errors++
            }
            tword = playWord
            println("$word -vs- $playWord")
            println("Errors: $errors out of $CRITERRORS")
        }

        // check what how many errors
        if (errors >= CRITERRORS) {
            // break out of game loop
            println("too many incorrect attempts")
            break;
        }
        turn++;
    }

    // Enter Final Game Conditions
    checkEndConditions(isWin(word, playWord))
}

fun makePlayWord(word: String): String {
    var playWord = ""
    for (i in 1..word.length) {
        playWord += "-"
    }

    return playWord
}


fun hostSetup(input: Scanner) {
    // Game setup
    println("Please enter the Port Number")
    while (!input.hasNextInt()) {
        val ent = input.next()
        println("$ent is not a valid number")
    }
    val portNumber = input.nextInt()
    val lan = Online(portNumber)
    println("Waiting for other player to join...")
    lan.hostServer()
    println("Connected")

    // run the actual game loop
    gameLoop(input, 0, lan)
}

fun host(input: Scanner) {
    hostSetup(input)
}

fun joinSetup(input : Scanner) {
    // Game setup
    println("Please enter the IP Address of the Host")
    val ip = input.nextLine()
    println("Please enter the Port Number")
    while (!input.hasNextInt()) {
        val ent = input.next()
        println("$ent is not a valid number")
    }
    val portNumber = input.nextInt()

    val lan = Online(portNumber, ip)

    try {
        lan.joinServer()  // connect to the host
        println("Connected")

        // run the actual game loop
        println("to loop")
        gameLoop(input, 1, lan)
    } catch (ex: Exception) {
        println("Connection to host failed")
    }
}


fun client(input: Scanner) {
    joinSetup(input)
}

fun playLan(input : Scanner) {
    println("Enter \"Host\" to host the game and \"Join\" to join one")
    var choice : String
    // loop until choice == "host" or "join"
    do {
        choice = input.nextLine()  // take player input
    } while (!(choice.equals("Host", ignoreCase = true) || choice.equals("Join", ignoreCase = true)))

    try {
        if (choice.equals("Host", ignoreCase = true)) {
            // Player is hosting
            host(input)
        } else {
            // Player is joining
            client(input)
        }
    } catch (ex: Exception) {
        println(ex.message)
    }

}



fun main() {
    println("Welcome to the Hangman Game!")
    generateAlpha()  // generate the alphabet to verify input
    val input = Scanner(System.`in`)
    playLan(input)
}
