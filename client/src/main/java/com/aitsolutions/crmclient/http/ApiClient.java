package com.aitsolutions.crmclient.http;

import com.aitsolutions.crmclient.config.ConfiguracionCliente;
import com.aitsolutions.crmclient.dto.ErrorResponseDto;
import com.aitsolutions.crmclient.sesion.SesionActiva;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Unico punto de acceso al backend REST. No usa ninguna libreria externa de HTTP:
 * java.net.http.HttpClient viene incluido en el JDK desde la version 11.
 */
public class ApiClient {

    private static final ApiClient INSTANCIA = new ApiClient();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = ConfiguracionCliente.getInstance().getApiBaseUrl();
    }

    public static ApiClient getInstance() {
        return INSTANCIA;
    }

    public <T> T put(String path, Object body, Class<T> tipoRespuesta) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));

            aplicarAuthSiHaceFalta(builder, true);

            HttpResponse<String> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return procesarRespuesta(respuesta, tipoRespuesta);
        } catch (IOException | InterruptedException e) {
            throw errorDeConexion(e);
        }
    }

    public <T> T patch(String path, Object body, Class<T> tipoRespuesta) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));

            aplicarAuthSiHaceFalta(builder, true);

            HttpResponse<String> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return procesarRespuesta(respuesta, tipoRespuesta);
        } catch (IOException | InterruptedException e) {
            throw errorDeConexion(e);
        }
    }

    public void delete(String path) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .DELETE()
                    .timeout(Duration.ofSeconds(10));
            aplicarAuthSiHaceFalta(builder, true);
            HttpResponse<String> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
                throw new ApiException(extraerMensajeError(respuesta, respuesta.statusCode()), respuesta.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw errorDeConexion(e);
        }
    }

    public <T> T post(String path, Object body, Class<T> tipoRespuesta, boolean requiereAuth) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(10));

            aplicarAuthSiHaceFalta(builder, requiereAuth);

            HttpResponse<String> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return procesarRespuesta(respuesta, tipoRespuesta);
        } catch (IOException | InterruptedException e) {
            throw errorDeConexion(e);
        }
    }

    public <T> T get(String path, Class<T> tipoRespuesta) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .GET()
                    .timeout(Duration.ofSeconds(10));

            aplicarAuthSiHaceFalta(builder, true);

            HttpResponse<String> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return procesarRespuesta(respuesta, tipoRespuesta);
        } catch (IOException | InterruptedException e) {
            throw errorDeConexion(e);
        }
    }

    private void aplicarAuthSiHaceFalta(HttpRequest.Builder builder, boolean requiereAuth) {
        if (requiereAuth) {
            String token = SesionActiva.getInstance().getToken();
            builder.header("Authorization", "Bearer " + token);
        }
    }

    private <T> T procesarRespuesta(HttpResponse<String> respuesta, Class<T> tipoRespuesta) {
        int status = respuesta.statusCode();

        if (status >= 200 && status < 300) {
            try {
                return objectMapper.readValue(respuesta.body(), tipoRespuesta);
            } catch (IOException e) {
                throw new ApiException("Respuesta inesperada del servidor", e);
            }
        }

        throw new ApiException(extraerMensajeError(respuesta, status), status);
    }

    private String extraerMensajeError(HttpResponse<String> respuesta, int status) {
        if (status == 401) {
            return "Usuario o contraseña incorrectos";
        }
        if (status == 403) {
            return "No tienes permiso para realizar esta acción";
        }
        try {
            ErrorResponseDto error = objectMapper.readValue(respuesta.body(), ErrorResponseDto.class);
            if (error.getMessage() != null) {
                return error.getMessage();
            }
        } catch (IOException ignored) {
            // El cuerpo no era el ErrorResponse esperado (p.ej. 403 sin cuerpo); se usa el mensaje generico de abajo.
        }
        return "Error del servidor (código " + status + ")";
    }

    private ApiException errorDeConexion(Exception causa) {
        return new ApiException("No se pudo conectar con el servidor. Comprueba tu conexión.", causa);
    }

    /**
     * Deserializacion con tipo generico (p.ej. PaginaRespuesta&lt;PacienteResponse&gt;),
     * ya que Class&lt;T&gt; no puede expresar genericos. Usar junto a un TypeReference.
     */
    public <T> T getConTipoGenerico(String path, com.fasterxml.jackson.core.type.TypeReference<T> tipoRespuesta) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .GET()
                    .timeout(Duration.ofSeconds(10));

            aplicarAuthSiHaceFalta(builder, true);

            HttpResponse<String> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            int status = respuesta.statusCode();

            if (status >= 200 && status < 300) {
                return objectMapper.readValue(respuesta.body(), tipoRespuesta);
            }
            throw new ApiException(extraerMensajeError(respuesta, status), status);
        } catch (IOException | InterruptedException e) {
            throw errorDeConexion(e);
        }
    }
}
