package com.example.todolist;

import java.io.Serializable;

public class ToDoElement implements Serializable {

    private String title;
    private String note;
    private String remindTime;
    private boolean status;
    private boolean isRemindChecked;
    private int priority;
    private String dateOfCreation;
    private String deadline;
    private String imageTitle;

    public ToDoElement(String title, String note, String remindTime, boolean isRemindChecked, boolean status, int priority, String dateOfCreation, String deadline, String imageTitle) {
        this.title = title;
        this.note = note;
        this.remindTime = remindTime;
        this.isRemindChecked = isRemindChecked;
        this.status = status;
        this.priority = priority;
        this.dateOfCreation = dateOfCreation;
        this.deadline = deadline;
        this.imageTitle = imageTitle;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getRemindTime() {
        return remindTime;
    }

    public boolean isRemindChecked() {
        return isRemindChecked;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public boolean isStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public String getDateOfCreation() {
        return dateOfCreation;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setRemindTime(String remindTime) {
        this.remindTime = remindTime;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setRemindChecked(boolean remindChecked) {
        isRemindChecked = remindChecked;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
