package com.alura.challenge_literatura.principal;

import com.alura.challenge_literatura.model.Datos;
import com.alura.challenge_literatura.model.DatosLibros;
import com.alura.challenge_literatura.service.ConsumoAPI;
import com.alura.challenge_literatura.service.ConvierteDatos;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";

    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);

    public void muestraElMenu(){

        //Menu
        //1. Buscar libro por título                        Listo
        //2. Listar libros registrados (BD)
        //3. Listar autores registrados (BD)
        //4. Listar autores vivos en un determinado año     Pendiente
        //5. Listar libros por idioma                       Pendiente
        //6. Salir de la app

        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json, Datos.class);
        System.out.println(datos);

        //Top 10 libros más descargados
        System.out.println("Top 10 libros más descargados");
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed())
                .limit(10)
                .map(l -> l.titulo().toUpperCase())
                .forEach(System.out::println);

        //Búsqueda de libros por nombre
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();

        json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()){
            System.out.println("Libro encontrado ");
            System.out.println(libroBuscado.get());
        } else {
            System.out.println("Libro no encontrado ");
        }

        //Trabajando con estadisticas
        DoubleSummaryStatistics est = datosBusqueda.resultados().stream()
                .filter(d -> d.numeroDeDescargas() > 0)
                .collect(Collectors.summarizingDouble(DatosLibros::numeroDeDescargas));
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad máxima de descargas: " + est.getMax());
        System.out.println("Cantidad mínima de descragas: " + est.getMin());
        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: " + est.getCount());

        //Busqueda de libros por idioma
        System.out.println("Ingrese el idioma en el que le gustaría buscar");
    }
}
