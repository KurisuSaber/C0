package pers.thy.C0.analyser;

public class Symbol {
    int number;
    int value;
    String name;
    String kind;
    String type;
    int level;
    String other;

    public Symbol() {
    }

    public Symbol(int number, String name, String kind, String type, int level) {
        this.number = number;
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.level = level;
    }

    public Symbol(int number, String name, String kind, String type, int level, String other) {
        this.number = number;
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.level = level;
        this.other = other;
    }

    public Symbol(int number, int value, String name, String kind, String type, int level, String other) {
        this.number = number;
        this.value = value;
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.level = level;
        this.other = other;
    }
}
