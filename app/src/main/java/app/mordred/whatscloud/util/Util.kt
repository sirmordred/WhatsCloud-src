package app.mordred.whatscloud.util

import com.mordred.wordcloud.WordFrequency
import java.util.*

class Util {
    companion object {
        val cal: Calendar = Calendar.getInstance()

        fun addToDateCountMap(givenHm: HashMap<Date,Int>, givenDate: Date) {
            val dateCount = givenHm[givenDate]
            if (dateCount != null) {
                givenHm[givenDate] = dateCount + 1
            } else {
                givenHm[givenDate] = 1
            }
        }

        fun addToDayCountMap(givenHm: HashMap<String,Int>, givenDate: Date) {
            cal.time = givenDate
            val nameOfDay = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            val dateCount = givenHm[nameOfDay]
            if (dateCount != null) {
                givenHm[nameOfDay] = dateCount + 1
            } else {
                givenHm[nameOfDay] = 1
            }
        }

        fun insertWordToWordFreq(word: String, wordFreq: WordFrequency/* TODO EMOJI, emojiFreq: WordFrequency*/): Int {
            val sentence = word.toLowerCase()
            val dirtyWordsArr = sentence.split(" ")
            val wordCount = dirtyWordsArr.size
            for (dirtyWord in dirtyWordsArr) {
                // TODO EMOJI check if emoji, if its add to emojiFreq
                val cleanWord = removeLeadAndTrailNonLetters(dirtyWord)
                if (cleanWord != null) {
                    wordFreq.insertWordAlreadyNormalized(dirtyWord)
                }
            }
            return wordCount
        }

        /* Developed by @sirmordred for replacement of classic-slow regex replaceAll() functions
         *
         * It removes leading and trailing non-letters (letters are unicode standart letters and
         * non-letters are complement of them)
         *
         *
         * Returns Null if given String or processed String has less than 2 chars
         */
        private fun removeLeadAndTrailNonLetters(s: String): String? {
            if (s.length < 2) {
                return null
            }
            var beginIndex = 0
            var endIndex = s.length - 1
            val loopCount = endIndex
            var beginIndexFound = false
            var endIndexFound = false
            var loopIndex = 0
            while (loopIndex < loopCount) {
                if (!beginIndexFound && Character.isLetter(s[beginIndex++])) {
                    beginIndexFound = true
                }
                if (!endIndexFound && Character.isLetter(s[endIndex--])) {
                    endIndexFound = true
                }
                if (beginIndexFound && endIndexFound) {
                    return if (endIndex - beginIndex == -2) {
                        null
                    } else {
                        s.substring(beginIndex - 1, endIndex + 2)
                    }
                }
                loopIndex++
            }
            return null
        }
    }
}