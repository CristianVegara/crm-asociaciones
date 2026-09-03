package com.aitsolutions.crmclient.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Lee la URL del backend desde un archivo "client.properties" situado junto al ejecutable
 * (no dentro del jar), tal y como pide el apartado 10 del plan: la IP del servidor debe
 * poder cambiar sin recompilar el cliente. Si el archivo no existe, usa localhost:8080
 * (comodo para desarrollo).
 */
public class ConfiguracionCliente {

    private static final String ARCHIVO_CONFIG = "client.properties";
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
        try (FileInputStream entrada = new FileInputStream(ARCHIVO_CONFIG)) {
            propiedades.load(entrada);
            return propiedades.getProperty("api.base-url", URL_POR_DEFECTO);
        } catch (IOException e) {
            // No existe client.properties junto al ejecutable: se usa el valor por defecto.
            return URL_POR_DEFECTO;
        }
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
}
