package com.viajafacil.backend.controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.viajafacil.backend.model.Itinerario;
import com.viajafacil.backend.model.Pago;
import com.viajafacil.backend.model.Paquete;
import com.viajafacil.backend.model.Reserva;
import com.viajafacil.backend.model.Usuario;
import com.viajafacil.backend.repository.ItinerarioRepository;
import com.viajafacil.backend.repository.PagoRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @GetMapping("/boleta/{idPago}")
    public void generarBoleta(@PathVariable Long idPago,
                              HttpServletResponse response)
            throws DocumentException, IOException {

        // 1) Obtener entidades
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado: " + idPago));

        Reserva reserva = pago.getReserva();
        Usuario usuario = reserva.getUsuario();
        Paquete paquete = reserva.getPaquete();

        List<Itinerario> itinerarios =
                itinerarioRepository.listarPorPaqueteYDiaOrdenado(
                        paquete.getId_paquete(),
                        null
                );

        // 2) Configurar respuesta HTTP
        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=boleta_" + idPago + ".pdf"
        );

        // 3) Fonts
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);

        DateTimeFormatter formatterFechaHora =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter formatterFecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 4) Documento
        Document document = new Document(PageSize.A4, 40, 40, 40, 30);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        try {
            // Cargar la imagen desde el classpath (carpeta resources/static)
            ClassPathResource imgFile = new ClassPathResource("static/logo.png");

            byte[] imageBytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            Image logo = Image.getInstance(imageBytes);

            logo.scaleToFit(120, 120);
            logo.setAlignment(Element.ALIGN_CENTER);

            document.add(logo);
        } catch (IOException e) {
            // Si falla el logo, que igual se genere el PDF
            System.out.println("No se pudo cargar el logo: " + e.getMessage());
        }


        // TÍTULO CENTRADO
        Paragraph titulo = new Paragraph("VIAJAFÁCIL", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("COMPROBANTE DE PAGO", fontSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);

        document.add(new Paragraph(" ")); // espacio

        // LÍNEA SEPARADORA
        LineSeparator ls = new LineSeparator();
        document.add(new Chunk(ls));
        document.add(new Paragraph(" "));

        // DATOS DEL CLIENTE Y RESERVA
        Paragraph seccionCliente = new Paragraph("Datos del cliente y reserva", fontSubtitulo);
        seccionCliente.setAlignment(Element.ALIGN_LEFT);
        document.add(seccionCliente);
        document.add(new Paragraph(" "));

        PdfPTable tablaCliente = new PdfPTable(2);
        tablaCliente.setWidthPercentage(100);
        tablaCliente.setSpacingBefore(5);
        tablaCliente.setSpacingAfter(10);
        tablaCliente.setWidths(new float[]{1.2f, 2.0f});

        agregarCeldaEtiqueta(tablaCliente, "Cliente:", fontNegrita);
        agregarCeldaValor(tablaCliente,
                usuario.getNombre() + " " + usuario.getApellido(), fontNormal);

        agregarCeldaEtiqueta(tablaCliente, "Correo:", fontNegrita);
        agregarCeldaValor(tablaCliente, usuario.getCorreo(), fontNormal);

        agregarCeldaEtiqueta(tablaCliente, "N° Reserva:", fontNegrita);
        agregarCeldaValor(tablaCliente,
                String.valueOf(reserva.getId_reserva()), fontNormal);

        if (reserva.getFecha_reserva() != null) {
            agregarCeldaEtiqueta(tablaCliente, "Fecha de reserva:", fontNegrita);
            agregarCeldaValor(tablaCliente,
                    reserva.getFecha_reserva().format(formatterFecha), fontNormal);
        }

        agregarCeldaEtiqueta(tablaCliente, "Estado de reserva:", fontNegrita);
        agregarCeldaValor(tablaCliente, reserva.getEstado_reserva(), fontNormal);

        document.add(tablaCliente);

        //  DATOS DEL PAQUETE
        Paragraph seccionPaquete = new Paragraph("Datos del paquete", fontSubtitulo);
        seccionPaquete.setAlignment(Element.ALIGN_LEFT);
        document.add(seccionPaquete);
        document.add(new Paragraph(" "));

        PdfPTable tablaPaquete = new PdfPTable(2);
        tablaPaquete.setWidthPercentage(100);
        tablaPaquete.setSpacingBefore(5);
        tablaPaquete.setSpacingAfter(10);
        tablaPaquete.setWidths(new float[]{1.2f, 2.0f});

        agregarCeldaEtiqueta(tablaPaquete, "Paquete:", fontNegrita);
        agregarCeldaValor(tablaPaquete, paquete.getDescripcion(), fontNormal);

        agregarCeldaEtiqueta(tablaPaquete, "Categoría:", fontNegrita);
        agregarCeldaValor(tablaPaquete, paquete.getCategoria(), fontNormal);

        agregarCeldaEtiqueta(tablaPaquete, "Precio base:", fontNegrita);
        agregarCeldaValor(tablaPaquete,
                "S/ " + paquete.getPrecio_base(), fontNormal);

        document.add(tablaPaquete);

        // LÍNEA SEPARADORA
        document.add(new Chunk(ls));
        document.add(new Paragraph(" "));

        // ===================== DATOS DEL PAGO =====================
        Paragraph seccionPago = new Paragraph("Datos del pago", fontSubtitulo);
        seccionPago.setAlignment(Element.ALIGN_LEFT);
        document.add(seccionPago);
        document.add(new Paragraph(" "));

        PdfPTable tablaPago = new PdfPTable(2);
        tablaPago.setWidthPercentage(100);
        tablaPago.setSpacingBefore(5);
        tablaPago.setSpacingAfter(15);
        tablaPago.setWidths(new float[]{1.2f, 2.0f});

        agregarCeldaEtiqueta(tablaPago, "N° Pago:", fontNegrita);
        agregarCeldaValor(tablaPago,
                String.valueOf(pago.getId_pago()), fontNormal);

        if (pago.getFecha_pago() != null) {
            agregarCeldaEtiqueta(tablaPago, "Fecha de pago:", fontNegrita);
            agregarCeldaValor(tablaPago,
                    pago.getFecha_pago().format(formatterFechaHora), fontNormal);
        }

        agregarCeldaEtiqueta(tablaPago, "Método de pago:", fontNegrita);
        agregarCeldaValor(tablaPago, pago.getMetodo(), fontNormal);

        agregarCeldaEtiqueta(tablaPago, "Monto pagado:", fontNegrita);
        agregarCeldaValor(tablaPago,
                "S/ " + pago.getMonto(), fontNormal);

        agregarCeldaEtiqueta(tablaPago, "Estado del pago:", fontNegrita);
        agregarCeldaValor(tablaPago, pago.getEstado(), fontNormal);

        document.add(tablaPago);

        // LÍNEA SEPARADORA
        document.add(new Chunk(ls));
        document.add(new Paragraph(" "));

        //  ITINERARIO
        Paragraph seccionItinerario = new Paragraph("Itinerario del viaje", fontSubtitulo);
        seccionItinerario.setAlignment(Element.ALIGN_LEFT);
        document.add(seccionItinerario);
        document.add(new Paragraph(" "));

        PdfPTable tablaItinerario = new PdfPTable(4);
        tablaItinerario.setWidthPercentage(100);
        tablaItinerario.setSpacingBefore(5);
        tablaItinerario.setSpacingAfter(15);
        tablaItinerario.setWidths(new float[]{0.8f, 1.4f, 2.5f, 2.0f});

        // Encabezados
        agregarHeaderTabla(tablaItinerario, "Día", fontNegrita);
        agregarHeaderTabla(tablaItinerario, "Hora", fontNegrita);
        agregarHeaderTabla(tablaItinerario, "Actividad", fontNegrita);
        agregarHeaderTabla(tablaItinerario, "Lugar", fontNegrita);

        for (Itinerario it : itinerarios) {
            String hora = (it.getHora_inicio() != null ? it.getHora_inicio().toString() : "--:--")
                    + " - " +
                    (it.getHora_fin() != null ? it.getHora_fin().toString() : "--:--");

            tablaItinerario.addCell(new PdfPCell(new Phrase(
                    String.valueOf(it.getDia()), fontNormal)));
            tablaItinerario.addCell(new PdfPCell(new Phrase(hora, fontNormal)));
            tablaItinerario.addCell(new PdfPCell(new Phrase(it.getActividad(), fontNormal)));
            tablaItinerario.addCell(new PdfPCell(new Phrase(
                    it.getLugar() != null ? it.getLugar() : "-", fontNormal)));
        }

        document.add(tablaItinerario);

        // FOOTER
        document.add(new Chunk(ls));
        document.add(new Paragraph(" "));

        Paragraph footer = new Paragraph(
                "Gracias por viajar con ViajaFácil.",
                fontFooter
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }

    private void agregarCeldaEtiqueta(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cell);
    }

    private void agregarCeldaValor(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "-", font));
        cell.setBorder(Rectangle.NO_BORDER);
        tabla.addCell(cell);
    }

    private void agregarHeaderTabla(PdfPTable tabla, String texto, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(texto, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setBackgroundColor(new Color(230, 230, 230));
        tabla.addCell(header);
    }
}
