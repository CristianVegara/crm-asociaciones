package com.aitsolutions.crmclient.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lee la URL del backend desde una configuración externa, sin incluirla en el jar.
 */
public class ConfiguracionCliente {

    private static final String ARCHIVO_CONFIG = "client.properties";
    private static final Path CONFIG_USUARIO = Path.of(
            System.getProperty("user.home"), ".crm-asociaciones", ARCHIVO_CONFIG);
    private static final String URL_POR_DEFECTO = "http://localhost:8080";

    private static ConfiguracionCliente instancia;

    private final String apiBaseUrl;

    private ConfiguracionCliente() {
        this.apiBaseUrl = cargarUrlBackend();
    }

    public static synchronized ConfiguracionCliente getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracionCliente();
        }
        return instancia;
    }

    private String cargarUrlBackend() {
        Properties propiedades = new Properties();
        for (Path ruta : new Path[]{CONFIG_USUARIO, Path.of(ARCHIVO_CONFIG)}) {
            if (!Files.isRegularFile(ruta)) {
                continue;
            }
            try (InputStream entrada = Files.newInputStream(ruta)) {
                propiedades.load(entrada);
                return propiedades.getProperty("api.base-url", URL_POR_DEFECTO);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer " + ruta, e);
            }
        }
        return URL_POR_DEFECTO;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
