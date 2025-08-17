package com.alura.challenge_literatura.model;

public enum Idioma {
    ES("es"),
    FR("fr"),
    EN("en"),
    PT("pt"),
    OTRO("otro");

    private final String idioma;

    Idioma(String idioma){
        this.idioma = idioma;
    }

    public String getIdioma() {
        return idioma;
    }

    public static Idioma fromString(String text){
        for (Idioma idiomas: Idioma.values()){
            if (idiomas.idioma.equalsIgnoreCase(text)){
                return idiomas;
            }
        }
        throw new IllegalArgumentException("No hay coincidencias con el texto " + text);
    }
}
