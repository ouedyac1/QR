package com.dcsic.qrcode.service;

import com.dcsic.qrcode.model.entities.UserQuestion;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportQuestions(List<UserQuestion> questions) throws Exception {

        Document document = new Document(PageSize.A4, 40, 40, 50, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.HELVETICA, 11);

        document.add(new Paragraph("Liste des questions reçues", titleFont));
        document.add(Chunk.NEWLINE);

        int index = 1;
        for (UserQuestion q : questions) {

            Paragraph header = new Paragraph(
                    "Question #" + index +
                            " — " + (q.getParticipantName() != null ? q.getParticipantName() : "Anonyme") +
                            " (" + q.getEvent().getName() + ")", headerFont);

            document.add(header);

            Paragraph text = new Paragraph(q.getQuestion(), textFont);
            text.setIndentationLeft(15);
            text.setSpacingAfter(10);
            document.add(text);

            index++;
        }

        document.close();
        return out.toByteArray();
    }
}
