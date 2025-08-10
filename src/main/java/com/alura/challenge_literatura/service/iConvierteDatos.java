package com.alura.challenge_literatura.service;

public interface iConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
