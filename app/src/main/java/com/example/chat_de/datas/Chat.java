package com.example.chat_de.datas;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Chat {
    public enum Type {TEXT, IMAGE, FILE, SYSTEM}
    private String text;
    private int index;
    private int date;
    private String from;
    private Type type;

    public Chat() { }
    public Chat(String text, int index, int date, String from, Type type) {
        setText(text);
        setIndex(index);
        setDate(date);
        setFrom(from);
        setType(type);
    }

    public String getText() { return text; }
    public int getIndex()   { return index; }
    public int getDate()    { return date; }
    public String getFrom() { return from; }
    public Type getType() { return type; }

    public void setText(String text)    { this.text = text; }
    public void setIndex(int index)     { this.index = index; }
    public void setType(Type type)    { this.type = type; }
    public void setDate(int date)       { this.date = date; }
    public void setFrom(String from)    { this.from = from; }
}