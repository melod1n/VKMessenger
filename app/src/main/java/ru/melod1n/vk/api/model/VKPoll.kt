package ru.melod1n.vk.api.model

import android.graphics.Color
import org.json.JSONObject
import java.io.Serializable
import java.util.*

class VKPoll(o: JSONObject) : VKModel(), Serializable {
    var id = 0
    var ownerId = 0
    var created = 0
    var question: String? = null
    var votes = 0
    var answers = ArrayList<Answer>()
    var isAnonymous = false
    var isMultiple = false
    var answerIds = ArrayList<Int>()
    var endDate = 0
    var isClosed = false
    var isBoard = false
    var isCanEdit = false
    var isCanVote = false
    var isCanReport = false
    var isCanShare = false
    var authorId = 0
    var background = Color.WHITE

    inner class Answer(o: JSONObject) : Serializable {
        var id = 0
        var text: String? = null
        var votes = 0
        var rate = 0

        init {
            id = o.optInt("id", -1)
            text = o.optString("text")
            votes = o.optInt("votes")
            rate = o.optInt("rate")
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    //private ArrayList friends
    init {
        id = o.optInt("id", -1)
        ownerId = o.optInt("owner_id", -1)
        created = o.optInt("created")
        question = o.optString("question")
        votes = o.optInt("votes")
        val oAnswers = o.optJSONArray("answers")
        if (oAnswers != null) {
            val answers = ArrayList<Answer>()
            for (i in 0 until oAnswers.length()) {
                answers.add(Answer(oAnswers.optJSONObject(i)))
            }
            this.answers = answers
        }
        isAnonymous = o.optBoolean("anonymous")
        isMultiple = o.optBoolean("multiple")
        //setAnswerIds();
        endDate = o.optInt("end_date")
        isClosed = o.optBoolean("closed")
        isBoard = o.optBoolean("is_board")
        isCanEdit = o.optBoolean("can_edit")
        // ...
    }
}