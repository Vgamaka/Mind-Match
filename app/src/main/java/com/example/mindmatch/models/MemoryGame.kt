package com.example.mindmatch.models

import com.example.mindmatch.utils.DEFAULT_ICONS

class MemoryGame (private val boardSize: BoardSize){

    val cards: List<MemoryCard>
    var numMatchesFound = 0

    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null

    init {

        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumMatches())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int): Boolean {
        numCardFlips++
        val card = cards[position]

        var foundMatch = false

        if (indexOfSingleSelectedCard == null){
            //0 or 2 cards previously flipped over
            restoreCards()
            indexOfSingleSelectedCard = position
        }else{
            //one card flipped over prev
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!,position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position01: Int, position02: Int): Boolean {

        if (cards[position01].identifier != cards[position02].identifier){
            return false
        }
        cards[position01].isMatched = true
        cards[position02].isMatched = true
        numMatchesFound++
        return true

    }

    private fun restoreCards() {
        for (card in cards){
            if(!card.isMatched){
            card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {

        return numMatchesFound == boardSize.getNumMatches()

    }

    fun isCardFaceUP(position: Int): Boolean {

        return cards[position].isFaceUp

    }

    fun getNumMoves(): Int {
        return numCardFlips / 2
    }
}
