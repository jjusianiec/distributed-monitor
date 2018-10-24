package model;

public interface SharedObjectSerialization<T> {

	String encode(T t);

	T decode(String object);
}
