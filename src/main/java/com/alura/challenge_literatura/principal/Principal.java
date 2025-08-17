package com.alura.challenge_literatura.principal;

import com.alura.challenge_literatura.model.*;
import com.alura.challenge_literatura.repository.AutorRepository;
import com.alura.challenge_literatura.repository.LibroRepository;
import com.alura.challenge_literatura.service.ConsumoAPI;
import com.alura.challenge_literatura.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final ConsumoAPI consumoAPI;
    private final ConvierteDatos conversor;
    private Scanner teclado = new Scanner(System.in);

    @Autowired
    public Principal(LibroRepository libroRepository, AutorRepository autorRepository, ConsumoAPI consumoAPI, ConvierteDatos conversor){
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.consumoAPI = consumoAPI;
        this.conversor = conversor;
    }

    public void muestraElMenu(){

        //Menu
        //1. Buscar libro por título
        //2. Listar libros registrados (BD)
        //3. Listar autores registrados (BD)
        //4. Listar autores vivos en un determinado año
        //5. Listar libros por idioma
        //6. Salir de la app

        var opcion = -1;
        while (opcion != 0){
            var menu = """
                    1 - Buscar Libro
                    2 - Buscar Autores por Nombre
                    3 - Lista Libros Registrados
                    4 - Lista Autores Registrados
                    5 - Lista Autores Vivos
                    6 - Lista Libros por Idioma
                    7 - Lista Autores por Año
                    8 - Top 10 de Libros más Buscados
                    9 - Generar Estadísticas
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion){
                case 1 -> buscarLibroPorTitulo();
                case 2 -> buscarAutor();
                case 3 -> listaLibrosRegistrados();
                case 4 -> listaAutoresRegistrados();
                case 5 -> listaAutoresVivos();
                case 6 -> listarLibrosPorIdioma();
                case 7 -> listaAutoresPorAnio();
                case 8 -> top10Libros();
                case 9 -> generarEstadisticas();
                case 0 -> System.out.println("Finalizando la aplicación");
                default -> System.out.println("Opción no válida");
            }
        }


        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json, Datos.class);
        System.out.println(datos);

    }

    public void buscarLibroPorTitulo() {
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        var nombre = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());

        if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
            var datos = conversor.obtenerDatos(json, Datos.class);

            Optional<DatosLibros> libroBuscado = datos.resultados().stream().findFirst();
            if (libroBuscado.isPresent()) {
                System.out.println(
                        "\n------------- LIBRO --------------" +
                                "\nTítulo: " + libroBuscado.get().titulo() +
                                "\nAutor: " + libroBuscado.get().autor().stream()
                                .map(DatosAutor::nombre).limit(1).collect(Collectors.joining()) +
                                "\nIdioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                                "\nNúmero de descargas: " + libroBuscado.get().numeroDeDescargas() +
                                "\n--------------------------------------\n"
                );

                try {
                    List<DatosLibros> datosLibro = datos.resultados();
                    for (DatosLibros datosLibros : datosLibro){
                        Libro libroEncontrado = convertirAEntidadLibro(datosLibros);
                    }
                } catch (Exception e) {
                    System.out.println("Warning! " + e.getMessage());
                }
            } else {
                System.out.println("Libro no encontrado :(");
            }
        }
    }

    private void buscarAutor() {
        System.out.println("Ingrese el nombre del autor a buscar");
        var autorIngresado = teclado.nextLine();
        Optional<Autor> autores = libroRepository.buscarAutorPorNombre(autorIngresado);
        if (autores.isPresent()) {
            System.out.println(
                    "\nAutor: " + autores.get().getNombre() +
                            "\nFecha de nacimiento: " + autores.get().getFechaNacimiento() +
                            "\nFecha de fallecimiento: " + autores.get().getFechaFallecimiento() +
                            "\nLibros: " + autores.get().getLibros().stream()
                            .map(Libro::getTitulo).collect(Collectors.toList()) + "\n"
            );
        } else {
            System.out.println("El autor que buscas no ha sido encontrado");
        }
    }

    public void listaLibrosRegistrados() {
        List<Libro> libros = libroRepository.buscarTodosLosLibros();
        libros.forEach(l -> System.out.println(
                "-------------- LIBRO ----------------" +
                        "\nTítulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutores().getNombre() +
                        "\nIdioma: " + l.getIdioma().getIdioma() +
                        "\nNúmero de descargas: " + l.getNumeroDescargas() +
                        "\n----------------------------------------\n"
        ));
    }

    public void listaAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        System.out.println();
        autores.forEach(l -> System.out.println(
                "Autor: " + l.getNombre() +
                        "\nFecha de Nacimiento: " + l.getFechaNacimiento() +
                        "\nFecha de Fallecimiento: " + l.getFechaFallecimiento() +
                        "\nLibros: " + l.getLibros().stream()
                        .map(Libro::getTitulo).collect(Collectors.toList()) + "\n"
        ));
    }

    public void listaAutoresVivos() {
        System.out.println("Ingrese el año a consultar:");
        try {
            var fecha = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = libroRepository.buscarAutoresVivos(fecha);
            if (!autores.isEmpty()) {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getFechaNacimiento() +
                                "\nFecha de Fallecimiento: " + a.getFechaFallecimiento() +
                                "\nLibros: " + a.getLibros().stream()
                                .map(Libro::getTitulo).collect(Collectors.toList()) + "\n"
                ));
            } else {
                System.out.println("Para el año ingresado no hay autores vivos");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un año válido " + e.getMessage());
        }
    }

    public void listarLibrosPorIdioma() {
        var menu = """
                ---------------------------------------------------
                Elija el idioma del libro que desea buscar:
                1 - Español
                2 - Francés
                3 - Inglés
                4 - Portugués
                ----------------------------------------------------
                """;
        System.out.println(menu);

        try {
            var opcion = Integer.parseInt(teclado.nextLine());

            switch (opcion) {
                case 1 -> buscarLibrosPorIdioma("es");
                case 2 -> buscarLibrosPorIdioma("fr");
                case 3 -> buscarLibrosPorIdioma("en");
                case 4 -> buscarLibrosPorIdioma("pt");
                default -> System.out.println("Opción no valida");
            }
        } catch (NumberFormatException e) {
            System.out.println("La opción no es valida: " + e.getMessage());
        }
    }

    private void buscarLibrosPorIdioma(String idiomas) {
        try {
            Idioma idiomasEnum = Idioma.valueOf(idiomas.toUpperCase());
            List<Libro> libros = libroRepository.buscarLibrosPorIdioma(idiomasEnum);
            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados en este idioma");
            } else {
                System.out.println();
                libros.forEach(l -> System.out.println(
                        "----------- LIBRO --------------" +
                                "\nTítulo: " + l.getTitulo() +
                                "\nAutor: " + l.getAutores().getNombre() +
                                "\nIdioma: " + l.getIdioma().getIdioma() +
                                "\nNúmero de descargas: " + l.getNumeroDescargas()
                ));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Lo sentimos, no se encuentra disponible en el idioma ingresado");
        }
    }

    public void listaAutoresPorAnio() {
        var menu = """
                ------------------------------------------
                Ingrese una opción para generar la lista de los autores:
                                    
                1 - Lista autores por Año de Nacimiento
                2 - Lista autores por año de Fallecimiento
                -------------------------------------------
                """;
        System.out.println(menu);
        try {
            var opcion = Integer.parseInt(teclado.nextLine());
            switch (opcion) {
                case 1 -> ListarAutoresPorNacimiento();
                case 2 -> ListarAutoresPorFallecimiento();
                default -> System.out.println("Opción no valida");
            }
        } catch (NumberFormatException e) {
            System.out.println("La opción no es valida: " + e.getMessage());
        }
    }

    public void ListarAutoresPorNacimiento() {
        System.out.println("Ingrese el año de nacimiento de los autores que desea buscar:");
        try {
            var nacimiento = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = libroRepository.listarAutoresPorNacimiento(nacimiento);
            if (autores.isEmpty()) {
                System.out.println("No existen autores con año de nacimiento igual a " + nacimiento);
            } else {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getFechaNacimiento() +
                                "\nFecha de Fallecimiento: " + a.getFechaFallecimiento() +
                                "\nLibros: " + a.getLibros().stream().map(Libro::getTitulo).collect(Collectors.toList()) + "\n"
                ));
            }
        } catch (NumberFormatException e) {
            System.out.println("El año ingresado no es válido: " + e.getMessage());
        }
    }

    public void ListarAutoresPorFallecimiento() {
        System.out.println("Ingrese el año de fallecimiento de los autores que desea buscar:");
        try {
            var fallecimiento = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = libroRepository.listarAutoresPorFallecimiento(fallecimiento);
            if (autores.isEmpty()) {
                System.out.println("No existen autores con año de fallecimiento igual a " + fallecimiento);
            } else {
                System.out.println();
                autores.forEach(a -> System.out.println(
                        "Autor: " + a.getNombre() +
                                "\nFecha de Nacimiento: " + a.getFechaNacimiento() +
                                "\nFecha de Fallecimeinto: " + a.getFechaFallecimiento() +
                                "\nLibros: " + a.getLibros().stream().map(Libro::getTitulo).collect(Collectors.toList()) + "\n"
                ));
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    public void top10Libros() {
        List<Libro> libros = libroRepository.top10Libros();
        System.out.println();
        libros.forEach(l -> System.out.println(
                "----------------- LIBRO ----------------" +
                        "\nTítulo: " + l.getTitulo() +
                        "\nAutor: " + l.getAutores().getNombre() +
                        "\nIdioma: " + l.getIdioma().getIdioma() +
                        "\nNúmero de descargas: " + l.getNumeroDescargas() +
                        "\n-------------------------------------------\n"
        ));
    }

    public void generarEstadisticas() {
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, Datos.class);
        IntSummaryStatistics est = datos.resultados().stream()
                .filter(l -> l.numeroDeDescargas() > 0)
                .collect(Collectors.summarizingInt(DatosLibros::numeroDeDescargas));
        int media = (int) est.getAverage();
        System.out.println("\n--------- ESTADÍSTICAS ------------");
        System.out.println("Media de descargas: " + media);
        System.out.println("Máxima de descargas: " + est.getMax());
        System.out.println("Mínima de descargas: " + est.getMin());
        System.out.println("Total registros para calcular las estadísticas: " + est.getCount());
    }

    public Libro convertirAEntidadLibro(DatosLibros datosLibro) {
        if (datosLibro == null) {
            return null; // Retorna null si datosLibro es nulo
        }

        Libro libro = new Libro();

        Optional<Libro> libroEncontrado = libroRepository.findById(datosLibro.id());
        if (libroEncontrado.isPresent()) {
            libro = libroEncontrado.get();
        } else {
            libro.setId(datosLibro.id());
            libro.setTitulo(datosLibro.titulo());

            // Establecer idiomas
            if (datosLibro.idiomas() != null && !datosLibro.idiomas().isEmpty()) {
                Idioma enumLenguaje = null;
                for (String lenguaje : datosLibro.idiomas()) {
                    enumLenguaje = Idioma.fromString(lenguaje);
                    libro.setIdioma(enumLenguaje);
                }
                if (enumLenguaje == null) {
                    libro.setIdioma(Idioma.OTRO);
                }
            }

            libro.setNumeroDescargas(datosLibro.numeroDeDescargas());

            List<DatosAutor> datosAutores = datosLibro.autor();
            for (DatosAutor datosAutor : datosAutores) {
                Autor autores = autorRepository.findByNombreAndFechaNacimientoAndFechaFallecimiento(
                        datosAutor.nombre(), datosAutor.fechaDeNacimiento(), datosAutor.fechaDeFallecimiento());

                if (autores == null) {
                    autores = new Autor();
                    autores.setNombre(datosAutor.nombre());
                    autores.setFechaNacimiento(datosAutor.fechaDeNacimiento());
                    autores.setFechaFallecimiento(datosAutor.fechaDeFallecimiento());
                    autorRepository.save(autores);
                }
                libro.setAutores(autores);
            }

            // Guardar el libro en el repositorio
            libroRepository.save(libro);
        }

        return libro;
    }
}
