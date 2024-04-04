package model;

public class StringMessage implements Message{
    String body;
    public StringMessage(String body){
        this.body = body;
    }

    public StringMessage(){}

    public String getBody() {
        return body;
    }
}
