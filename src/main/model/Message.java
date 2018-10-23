package model;

public interface Message {
	String encode();
	Message decode(String object);
}
