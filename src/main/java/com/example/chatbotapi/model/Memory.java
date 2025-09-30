package com.example.chatbotapi.model;

public class Memory {
    private String key;
    private String value;

    public Memory() {}
    public Memory(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
