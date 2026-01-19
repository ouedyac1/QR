package com.dcsic.qrcode.web.rest;

import com.dcsic.qrcode.dao.repository.EventRepo;
import com.dcsic.qrcode.dao.repository.UserQuestionRepo;
import com.dcsic.qrcode.model.entities.Event;
import com.dcsic.qrcode.model.entities.UserQuestion;
import com.dcsic.qrcode.service.PdfExportService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class QuestionController {

    private final EventRepo eventRepository;
    private final UserQuestionRepo userQuestionRepo;
    private final PdfExportService pdfExportService;

    public QuestionController(EventRepo e, UserQuestionRepo u, PdfExportService p) {
        this.eventRepository = e;
        this.userQuestionRepo = u;
        this.pdfExportService = p;
    }

    // Page d'accueil - affiche le QR code
    @GetMapping("/")
    public String home(Model model) {
        Event event = eventRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        if (event == null) {
            model.addAttribute("message", "Aucun événement créé. Créez-en un depuis l'admin.");
            return "no-event";
        }

        model.addAttribute("event", event);
        return "qrcode_display";
    }

    // Génération de l'image QR code
    @GetMapping("/qrcode/{slug}")
    public void generateQrCode(@PathVariable String slug, HttpServletResponse response)
            throws IOException, WriterException {
        String url = "https://qrcode.defense.bf/ask/" + slug;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 350, 350);
        response.setContentType("image/png");
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", response.getOutputStream());
    }

    // Page où l'utilisateur pose sa question
    @GetMapping("/ask/{slug}")
    public String askQuestion(@PathVariable String slug, Model model) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("event", event);
        model.addAttribute("userQuestion", new UserQuestion());
        return "ask-questions";
    }

    // Soumission de la question
    @PostMapping("/ask/{slug}")
    public String submitQuestion(@PathVariable String slug,
                                 @ModelAttribute UserQuestion userQuestion,
                                 RedirectAttributes redirectAttributes) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        userQuestion.setEvent(event);
        userQuestionRepo.save(userQuestion);

        redirectAttributes.addFlashAttribute("success", "Merci ! Votre question a été envoyée.");
        return "redirect:/ask/" + slug;
    }

    // Admin - Liste des questions
    @GetMapping("/admin/questions")
    public String listQuestions(Model model) {
        model.addAttribute("questions", userQuestionRepo.findAllByOrderBySubmittedAtDesc());
        model.addAttribute("events", eventRepository.findAll());
        return "admin/admin_questions_list";
    }

    // Admin - Questions par événement
    @GetMapping("/admin/questions/event/{eventId}")
    public String listQuestionsByEvent(@PathVariable Long eventId, Model model) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("event", event);
        model.addAttribute("questions", userQuestionRepo.findByEventIdOrderBySubmittedAtDesc(eventId));
        return "admin/questions-by-event";
    }

    // Admin - Supprimer une question
    @PostMapping("/admin/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        userQuestionRepo.deleteById(id);
        return "redirect:/admin/questions";
    }

    @GetMapping("/admin/questions/export/pdf")
    public ResponseEntity<byte[]> exportQuestionsPdf() throws Exception {

        List<UserQuestion> questions = userQuestionRepo.findAllByOrderBySubmittedAtDesc();

        byte[] pdf = pdfExportService.exportQuestions(questions);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=questions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}