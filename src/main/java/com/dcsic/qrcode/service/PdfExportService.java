package com.dcsic.qrcode.service;

import com.dcsic.qrcode.model.entities.Event;
import com.dcsic.qrcode.model.entities.UserQuestion;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Exporte toutes les questions (version originale)
     */
    public byte[] exportQuestions(List<UserQuestion> questions) throws Exception {
        return exportQuestionsWithTitle(questions, "Liste des questions reçues", true);
    }

    /**
     * Exporte les questions pour un événement spécifique
     */
    public byte[] exportQuestionsByEvent(Event event, List<UserQuestion> questions) throws Exception {
        String title = "Questions reçues - " + event.getName();
        return exportQuestionsWithTitle(questions, title, false);
    }

    /**
     * Méthode commune pour générer le PDF
     */
    private byte[] exportQuestionsWithTitle(List<UserQuestion> questions, String title, boolean showEventName) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 50, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.HELVETICA, 11);
        Font dateFont = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

        // Titre principal
        Paragraph titleParagraph = new Paragraph(title, titleFont);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(titleParagraph);

        // Date de génération
        Paragraph dateParagraph = new Paragraph(
                "Généré le " + java.time.LocalDateTime.now().format(DATE_FORMATTER),
                dateFont
        );
        dateParagraph.setAlignment(Element.ALIGN_CENTER);
        dateParagraph.setSpacingAfter(10);
        document.add(dateParagraph);

        // Statistiques
        Paragraph stats = new Paragraph("Total : " + questions.size() + " question(s)", headerFont);
        stats.setSpacingAfter(15);
        document.add(stats);

        document.add(Chunk.NEWLINE);

        // Liste des questions
        if (questions.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("Aucune question reçue pour le moment.", textFont);
            emptyMessage.setAlignment(Element.ALIGN_CENTER);
            document.add(emptyMessage);
        } else {
            int index = 1;
            for (UserQuestion q : questions) {
                // En-tête de la question
                String headerText = "Question #" + index + " — " +
                        (q.getParticipantName() != null ? q.getParticipantName() : "Anonyme");

                // Ajouter le nom de l'événement si nécessaire
                if (showEventName && q.getEvent() != null) {
                    headerText += " (" + q.getEvent().getName() + ")";
                }

                Paragraph header = new Paragraph(headerText, headerFont);
                document.add(header);

                // Date de soumission
                if (q.getSubmittedAt() != null) {
                    Paragraph date = new Paragraph(
                            "Soumise le " + q.getSubmittedAt().format(DATE_FORMATTER),
                            dateFont
                    );
                    date.setSpacingBefore(2);
                    document.add(date);
                }

                // Contenu de la question
                Paragraph text = new Paragraph(q.getQuestion(), textFont);
                text.setIndentationLeft(15);
                text.setSpacingAfter(15);
                text.setSpacingBefore(5);
                document.add(text);

                // Ligne de séparation (sauf pour la dernière question)
                if (index < questions.size()) {
                    LineSeparator separator = new LineSeparator();
                    separator.setLineColor(Color.LIGHT_GRAY);
                    document.add(new Chunk(separator));
                    document.add(Chunk.NEWLINE);
                }

                index++;
            }
        }

        document.close();
        return out.toByteArray();
    }
}