package com.aitsolutions.crmclient.informe;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

final class InformePdfExporter {
    private InformePdfExporter() {
    }

    static void exportar(String html, File destino) throws IOException {
        try (FileOutputStream salida = new FileOutputStream(destino)) {
            new PdfRendererBuilder()
                    .withHtmlContent(html, null)
                    .toStream(salida)
                    .run();
        }
    }
}
