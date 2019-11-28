package ru.melod1n.vk.api.model;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class VKPoll extends VKModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int ownerId;
    private int created;
    private String question;
    private int votes;
    private ArrayList<Answer> answers = new ArrayList<>();
    private boolean anonymous;
    private boolean multiple;
    private ArrayList<Integer> answerIds = new ArrayList<>();
    private int endDate;
    private boolean closed;
    private boolean isBoard;
    private boolean canEdit;
    private boolean canVote;
    private boolean canReport;
    private boolean canShare;
    private int authorId;
    private int background = Color.WHITE;
    //private ArrayList friends

    public VKPoll(JSONObject o) {
        setId(o.optInt("id", -1));
        setOwnerId(o.optInt("owner_id", -1));
        setCreated(o.optInt("created"));
        setQuestion(o.optString("question"));
        setVotes(o.optInt("votes"));

        JSONArray oAnswers = o.optJSONArray("answers");
        if (oAnswers != null) {
            ArrayList<Answer> answers = new ArrayList<>();
            for (int i = 0; i < oAnswers.length(); i++) {
                answers.add(new Answer(oAnswers.optJSONObject(i)));
            }

            setAnswers(answers);
        }

        setAnonymous(o.optBoolean("anonymous"));
        setMultiple(o.optBoolean("multiple"));
        //setAnswerIds();
        setEndDate(o.optInt("end_date"));
        setClosed(o.optBoolean("closed"));
        setBoard(o.optBoolean("is_board"));
        setCanEdit(o.optBoolean("can_edit"));
        // ...
    }

    private class Answer implements Serializable {

        private static final long serialVersionUID = 1L;

        private int id;
        private String text;
        private int votes;
        private int rate;

        public Answer(JSONObject o) {
            setId(o.optInt("id", -1));
            setText(o.optString("text"));
            setVotes(o.optInt("votes"));
            setRate(o.optInt("rate"));
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<Answer> answers) {
        this.answers = answers;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public ArrayList<Integer> getAnswerIds() {
        return answerIds;
    }

    public void setAnswerIds(ArrayList<Integer> answerIds) {
        this.answerIds = answerIds;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isBoard() {
        return isBoard;
    }

    public void setBoard(boolean board) {
        isBoard = board;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanVote() {
        return canVote;
    }

    public void setCanVote(boolean canVote) {
        this.canVote = canVote;
    }

    public boolean isCanReport() {
        return canReport;
    }

    public void setCanReport(boolean canReport) {
        this.canReport = canReport;
    }

    public boolean isCanShare() {
        return canShare;
    }

    public void setCanShare(boolean canShare) {
        this.canShare = canShare;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }
}
