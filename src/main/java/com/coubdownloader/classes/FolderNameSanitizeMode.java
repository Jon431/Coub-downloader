package com.coubdownloader.classes;

public enum FolderNameSanitizeMode {
    LATIN_ONLY("[^A-z0-9. ]"),
    LATIN_AND_CYRILLIC("[A-zА-я0-9. ]"),
    ALL_LETTERS("[^\\p{L}0-9. ]");

    private final String regex;

    FolderNameSanitizeMode(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }


}
