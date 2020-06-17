package com.example.tahmidsnotes;

public class Note {
    private String noteDescription;
    private String issueDate;
    private String noteUrl;

    public Note(String noteDescription, String issueDate, String noteUrl) {
        this.noteDescription = noteDescription;
        this.issueDate = issueDate;
        this.noteUrl = noteUrl;
    }

    public String getNoteDescription() {
        return noteDescription;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getNoteUrl() {
        return noteUrl;
    }
}
