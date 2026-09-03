package com.aitsolutions.crmclient.informe;

import com.aitsolutions.crmclient.dto.InformeResumenResponse;
import com.aitsolutions.crmclient.dto.InformeHistorialResponse;
import com.aitsolutions.crmclient.http.ApiClient;
import com.aitsolutions.crmclient.http.ApiException;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Locale;

public class InformeScreenController {

    @FXML
    private DatePicker fechaDesde;

    @FXML
    private DatePicker fechaHasta;

    @FXML
    private ComboBox<String> comboPeriodo;

    @FXML
    private ComboBox<String> comboTipoInforme;

    @FXML
    private Label etiquetaEstado;

    @FXML
    private Button botonExportarPdf;

    @FXML
    private Button botonPrevisualizarPdf;

    @FXML
    private TableView<InformeHistorialResponse> tablaHistorial;
    @FXML
    private TableColumn<InformeHistorialResponse, String> columnaFecha;
    @FXML
    private TableColumn<InformeHistorialResponse, String> columnaDesde;
    @FXML
    private TableColumn<InformeHistorialResponse, String> columnaHasta;
    @FXML
    private TableColumn<InformeHistorialResponse, String> columnaPeriodo;
    @FXML
    private TableColumn<InformeHistorialResponse, String> columnaUsuario;

    private InformeResumenResponse ultimoInforme;

    @FXML
    private Label labelPacientesActivos;
    @FXML
    private Label labelPacientesNuevos;

    @FXML
    private Label labelSesionesTotal;
    @FXML
    private Label labelSesionesVerde;
    @FXML
    private Label labelSesionesNaranja;
    @FXML
    private Label labelSesionesRojo;
    @FXML
    private Label labelSesionesAmarillo;
    @FXML
    private Label labelSesionesPendiente;
    @FXML
    private Label labelPorcentajeAsistencia;

    @FXML
    private Label labelSancionesAutomaticas;
    @FXML
    private Label labelSancionesManuales;
    @FXML
    private Label labelSancionesTotal;

    @FXML
    private Label labelPlanesCreados;
    @FXML
    private Label labelPlanesFinalizados;

    @FXML
    private Label labelServiciosResumen;
    @FXML
    private Label labelServiciosPorSexo;
    @FXML
    private Label labelServiciosPorAsociacion;
    @FXML
    private Label labelCancelaciones;

    @FXML
    private void initialize() {
        // "periodo" es solo una etiqueta para el propio informe (decision con Cristian):
        // el calculo real siempre usa las fechas exactas de los DatePicker de arriba.
        comboPeriodo.setItems(FXCollections.observableArrayList(
                "mensual", "trimestral", "semestral", "anual", "personalizado"));

        // Por defecto, el mes natural actual: punto de partida razonable al abrir la pantalla.
        LocalDate hoy = LocalDate.now();
        fechaDesde.setValue(hoy.withDayOfMonth(1));
        fechaHasta.setValue(hoy);
        comboPeriodo.setValue("mensual");
        comboTipoInforme.setItems(FXCollections.observableArrayList(
                "Informe general", "Informe de servicios"));
        comboTipoInforme.setValue("Informe general");
        configurarHistorial();
        cargarHistorial();
    }

    @FXML
    private void onGenerarClick() {
        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();

        if (desde == null || hasta == null) {
            etiquetaEstado.setText("Selecciona las dos fechas");
            return;
        }
        if (hasta.isBefore(desde)) {
            etiquetaEstado.setText("'Hasta' no puede ser anterior a 'Desde'");
            return;
        }

        etiquetaEstado.setText("Generando...");
        String periodo = comboPeriodo.getValue() != null ? comboPeriodo.getValue() : "";
        String tipoInforme = comboTipoInforme.getValue().equals("Informe de servicios")
                ? "servicios" : "general";
        String ruta = "/informes/resumen?desde=" + desde + "&hasta=" + hasta
                + "&periodo=" + periodo + "&tipoInforme=" + tipoInforme;

        Task<InformeResumenResponse> tarea = new Task<>() {
            @Override
            protected InformeResumenResponse call() {
                return ApiClient.getInstance().get(ruta, InformeResumenResponse.class);
            }
        };

        tarea.setOnSucceeded(evento -> {
            ultimoInforme = tarea.getValue();
            botonExportarPdf.setDisable(false);
            botonPrevisualizarPdf.setDisable(false);
            etiquetaEstado.setText("Informe generado");
        });

        tarea.setOnFailed(evento -> {
            Throwable causa = tarea.getException();
            String mensaje = (causa instanceof ApiException apiException)
                    ? apiException.getMessage()
                    : causa == null || causa.getMessage() == null
                    ? "No se pudo generar el informe"
                    : causa.getMessage();
            etiquetaEstado.setText(mensaje);
        });

        new Thread(tarea).start();
    }

    private void configurarHistorial() {
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fechaGeneracion"));
        columnaDesde.setCellValueFactory(new PropertyValueFactory<>("desde"));
        columnaHasta.setCellValueFactory(new PropertyValueFactory<>("hasta"));
        columnaPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        columnaUsuario.setCellValueFactory(new PropertyValueFactory<>("generadoPorNombre"));
    }

    private void cargarHistorial() {
        Task<InformeHistorialResponse[]> tarea = new Task<>() {
            @Override
            protected InformeHistorialResponse[] call() {
                return ApiClient.getInstance().get("/informes/historial", InformeHistorialResponse[].class);
            }
        };
        tarea.setOnSucceeded(evento -> tablaHistorial.setItems(
                FXCollections.observableArrayList(tarea.getValue())));
        tarea.setOnFailed(evento -> etiquetaEstado.setText("No se pudo cargar el historial"));
        new Thread(tarea).start();
    }

    @FXML
    private void onRegenerarClick() {
        InformeHistorialResponse seleccionado = tablaHistorial.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            etiquetaEstado.setText("Selecciona un informe del historial");
            return;
        }
        Task<InformeResumenResponse> tarea = new Task<>() {
            @Override
            protected InformeResumenResponse call() {
                return ApiClient.getInstance().post(
                        "/informes/" + seleccionado.getId() + "/regenerar",
                        new Object(), InformeResumenResponse.class, true);
            }
        };
        tarea.setOnSucceeded(evento -> {
            ultimoInforme = tarea.getValue();
            botonExportarPdf.setDisable(false);
            botonPrevisualizarPdf.setDisable(false);
            etiquetaEstado.setText("Informe regenerado");
            cargarHistorial();
        });
        tarea.setOnFailed(evento -> {
            Throwable causa = tarea.getException();
            etiquetaEstado.setText(causa instanceof ApiException apiException
                    ? apiException.getMessage()
                    : "No se pudo regenerar el informe");
        });
        new Thread(tarea).start();
    }

    @FXML
    private void onPrevisualizarPdfClick() {
        if (ultimoInforme == null) {
            etiquetaEstado.setText("Genera primero el informe");
            return;
        }
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            etiquetaEstado.setText("No hay visor de PDF disponible en este equipo");
            return;
        }

        try {
            File temporal = Files.createTempFile(
                    "informe-" + ultimoInforme.getDesde() + "-" + ultimoInforme.getHasta() + "-", ".pdf")
                    .toFile();
            InformePdfExporter.exportar(
                    InformeHtmlBuilder.construir(ultimoInforme), temporal);
            Desktop.getDesktop().open(temporal);
            etiquetaEstado.setText("Previsualizando el PDF");
        } catch (IOException | RuntimeException e) {
            etiquetaEstado.setText("No se pudo previsualizar el PDF: " + e.getMessage());
        }
    }

    @FXML
    private void onExportarPdfClick() {
        if (ultimoInforme == null) {
            etiquetaEstado.setText("Genera primero el informe");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar informe PDF");
        selector.setInitialFileName("informe-" + ultimoInforme.getDesde()
                + "-" + ultimoInforme.getHasta() + ".pdf");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));

        Window ventana = botonExportarPdf.getScene().getWindow();
        File destino = selector.showSaveDialog(ventana);
        if (destino == null) {
            return;
        }
        if (!destino.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
        }

        try {
            InformePdfExporter.exportar(
                    InformeHtmlBuilder.construir(ultimoInforme), destino);
            etiquetaEstado.setText("PDF guardado: " + destino.getName());
        } catch (IOException | RuntimeException e) {
            etiquetaEstado.setText("No se pudo guardar el PDF: " + e.getMessage());
        }
    }

    private void mostrarResultado(InformeResumenResponse informe) {
        var pacientes = informe.getPacientes();
        labelPacientesActivos.setText("Activos: " + pacientes.getActivos());
        labelPacientesNuevos.setText("Nuevos en el periodo: " + pacientes.getNuevosEnPeriodo());

        var sesiones = informe.getSesiones();
        labelSesionesTotal.setText("Total: " + sesiones.getTotal());
        labelSesionesVerde.setText("Verde: " + sesiones.getVerde());
        labelSesionesNaranja.setText("Naranja: " + sesiones.getNaranja());
        labelSesionesRojo.setText("Rojo: " + sesiones.getRojo());
        labelSesionesAmarillo.setText("Amarillo (baja médica): " + sesiones.getAmarillo());
        labelSesionesPendiente.setText("Pendiente: " + sesiones.getPendiente());
        labelPorcentajeAsistencia.setText(
                String.format(Locale.forLanguageTag("es-ES"), "%% asistencia: %.1f%%", sesiones.getPorcentajeAsistencia()));

        var sanciones = informe.getSanciones();
        labelSancionesAutomaticas.setText("Automáticas: " + sanciones.getAutomaticas());
        labelSancionesManuales.setText("Manuales: " + sanciones.getManuales());
        labelSancionesTotal.setText("Total: " + sanciones.getTotal());

        var planes = informe.getPlanesServicio();
        labelPlanesCreados.setText("Creados en el periodo: " + planes.getCreadosEnPeriodo());
        labelPlanesFinalizados.setText("Finalizados en el periodo: " + planes.getFinalizadosEnPeriodo());
        var servicios = informe.getServicios();
        labelServiciosResumen.setText("Total servicios: " + servicios.getTotal()
            + " | Cancelaciones: " + servicios.getCancelaciones()
            + " (" + String.format(Locale.ROOT, "%.1f", servicios.getPorcentajeCancelaciones()) + "%)");
        labelServiciosPorSexo.setText("Por sexo: " + servicios.getPorSexo());
        labelServiciosPorAsociacion.setText("Por asociación: " + servicios.getPorAsociacion());
        labelCancelaciones.setText("Cancelaciones: " + sesiones.getCancelada());
    }
}
