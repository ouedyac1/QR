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
@RequestMapping("/questions")
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
//    @GetMapping("/")
//    public String home(Model model) {
//        Event event = eventRepository.findAll().stream()
//                .findFirst()
//                .orElse(null);
//
//        if (event == null) {
//            model.addAttribute("message", "Aucun événement créé. Créez-en un depuis l'admin.");
//            return "no-event";
//        }
//
//        model.addAttribute("event", event);
//        return "qrcode_display";
//    }

    // Génération de l'image QR code
    @GetMapping("/qrcode/{slug}")
    public void generateQrCode(@PathVariable String slug, HttpServletResponse response)
            throws IOException, WriterException {
        String url = "https://qrcode.defense.bf/questions/ask/" + slug;
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
        return "redirect:/questions/ask/" + slug;
    }

    // Admin - Liste des questions
/*    @GetMapping("/admin/questions")
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
    }*/

    // Admin - Supprimer une question
    @PostMapping("/admin/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        userQuestionRepo.deleteById(id);
        return "redirect:/questions/admin/questions";
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
    
        // NOUVEAU : Export PDF des questions par événement
    @GetMapping("/admin/questions/export/pdf/event/{eventId}")
    public ResponseEntity<byte[]> exportQuestionsByEventPdf(@PathVariable Long eventId) throws Exception {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement non trouvé"));

        List<UserQuestion> questions = userQuestionRepo.findByEventIdOrderBySubmittedAtDesc(eventId);
        byte[] pdf = pdfExportService.exportQuestionsByEvent(event, questions);

        // Nom de fichier avec le slug de l'événement
        String filename = "questions_" + event.getSlug() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
        // Page d'affichage du QR code pour UN événement spécifique
      @GetMapping("/event/{slug}/qrcode")
      public String showEventQRCode(@PathVariable String slug, Model model) {
          Event event = eventRepository.findBySlug(slug)
                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                          "Événement non trouvé"));

        // Compter les questions pour cet événement
        long questionCount = userQuestionRepo.findByEventIdOrderBySubmittedAtDesc(event.getId()).size();

        model.addAttribute("event", event);
        model.addAttribute("questionCount", questionCount);

        return "event-qrcode";
    }
    
        // Admin - Liste des questions by event
    @GetMapping("/admin/questions/event/{eventId}")
    public String listQuestionsByEvent(@PathVariable Long eventId, Model model) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("questions", userQuestionRepo.findByEventIdOrderBySubmittedAtAsc(eventId)); // ← Desc → Asc
        model.addAttribute("events", eventRepository.findAll());
        model.addAttribute("selectedEventId", eventId);
        return "admin/admin_questions_list";
    }

    // Toutes les questions —
    @GetMapping("/admin/questions")
    public String listQuestions(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        model.addAttribute("selectedEventId", null);
        model.addAttribute("questions", userQuestionRepo.findAllByOrderByEventNameAscSubmittedAtAsc());
        return "admin/admin_questions_list";
    }
}