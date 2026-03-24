package com.dcsic.qrcode.web.rest;

import com.dcsic.qrcode.dao.repository.InstitutionRepo;
import com.dcsic.qrcode.dao.repository.PersonRepo;
import com.dcsic.qrcode.dao.repository.UserRepository;
import com.dcsic.qrcode.model.entities.Institution;
import com.dcsic.qrcode.model.entities.Person;
import com.dcsic.qrcode.model.entities.User;
import com.dcsic.qrcode.service.DiplomaService;
import com.google.zxing.WriterException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Controller
//@RequestMapping("/diplomas")
public class DiplomaController {

    private final DiplomaService diplomaService;
    private final PersonRepo personRepo;
    private final UserRepository userRepository;
    private final InstitutionRepo institutionRepo;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String institutionCode,
            Model model,
            Authentication auth) {

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Person> persons;

        // ✅ Récupérer l’utilisateur connecté via repository
        String currentUsername = auth.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (isSuperAdmin) {
            // ✅ Super admin : voit tout, avec possibilité de filtrer par code institution
            if (institutionCode != null && !institutionCode.isBlank()) {
                persons = personRepo.findByInstitutionCodeIgnoreCase(institutionCode.trim());
                model.addAttribute("selectedInstitution", institutionCode.trim());
            } else {
                persons = personRepo.findAll();
            }
            model.addAttribute("institutions", personRepo.findDistinctInstitutionCodes());
        } else {
            // ✅ Utilisateur normal : voit uniquement les personnes de son institution
            Institution institution = user.getInstitution();
            persons = personRepo.findByInstitution(institution);
            model.addAttribute("selectedInstitution", institution.getCode());
        }

        long validCount = persons.stream().filter(Person::isVerified).count();
        long revokedCount = persons.stream().filter(p -> !p.isVerified()).count();

        model.addAttribute("persons", persons);
        model.addAttribute("validCount", validCount);
        model.addAttribute("revokedCount", revokedCount);
        model.addAttribute("isSuperAdmin", isSuperAdmin);

        return "index";
    }

    // ==============================
    // PAGE UPLOAD
    // ==============================
    @GetMapping("/upload")
    public String uploadPage(Model model, Authentication auth) {
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isSuperAdmin", isSuperAdmin);

        if (isSuperAdmin) {
            model.addAttribute("institutions", institutionRepo.findAll());
        }

        return "diplomas/upload";
    }

    // ==============================
    //  import fichier excel
    // ==============================
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long institutionId,
            RedirectAttributes ra) {
        try {
            List<Person> imported = diplomaService.importFromExcel(file, institutionId);
            ra.addFlashAttribute("success",
                    imported.size() + " diplômes importés avec succès !");
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Erreur d'import : " + e.getMessage());
        }
        return "redirect:/";
    }

    // ==============================
    // Model fichier excel
    // ==============================
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {

        byte[] template = diplomaService.generateExcelTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=modele_import.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(template);
    }

    // Télécharger tous les QR codes en ZIP ou par structure
    @GetMapping("/qrcode/download-all")
    public ResponseEntity<byte[]> downloadAllQRCodes(
            @RequestParam(required = false) String institutionCode,
            Authentication auth) {

        try {
            // Logique de filtrage identique à celle de la page d'index
            boolean isSuperAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            String targetInstitution;
            if (isSuperAdmin) {
                // Si Admin, on prend l'institution choisie dans le filtre (ou null pour "Toutes")
                targetInstitution = (institutionCode != null && !institutionCode.isBlank()) ? institutionCode : null;
            } else {
                // Si utilisateur école, on force son institution uniquement
                String role = auth.getAuthorities().iterator().next().getAuthority();
                targetInstitution = role.replace("ROLE_", "").toUpperCase();
            }

            // On appelle le service en passant l'institution cible
            byte[] zipFile = diplomaService.generateQRCodesZip(targetInstitution);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            String filename = (targetInstitution != null) ?
                    "qrcodes_" + targetInstitution + ".zip" : "tous_les_qrcodes.zip";

            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);

        } catch (WriterException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur génération ZIP");
        }
    }


    // Télécharger tous les QR codes en ZIP
//    @GetMapping("/qrcode/download-all")
//    public ResponseEntity<byte[]> downloadAllQRCodes() {
//        try {
//            byte[] zipFile = diplomaService.generateAllQRCodes();
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", "qrcodes_diplomas.zip");
//
//            return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);
//        } catch (WriterException | IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Erreur lors de la génération des QR codes");
//        }
//    }

    // ==============================
    // VOIR QR CODE
    // ==============================
    @GetMapping("/qrcode/{id}")
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable Long id) {

        Person person = personRepo.findById(id).orElseThrow();

        try {
            byte[] qr = diplomaService.generateQRCode(person.getUniqueCode());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qr);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Vérification d'un diplôme (depuis le scan du QR code)
    @GetMapping("/verify/{uniqueCode}")
    public String verifyDiploma(@PathVariable String uniqueCode, Model model) {
        Optional<Person> personOpt = diplomaService.verifyDiploma(uniqueCode);

        if (personOpt.isPresent()) {
            Person person = personOpt.get();
            if (person.isVerified()) {
                model.addAttribute("person", person);
                model.addAttribute("verified", true);
            } else {
                model.addAttribute("verified", false);
                model.addAttribute("message", "Ce diplôme a été révoqué");
            }
        } else {
            model.addAttribute("verified", false);
            model.addAttribute("message", "Code invalide - Diplôme non authentique");
        }

        return "diplomas/verify";
    }
    // ==============================
    // TOGGLE VERIFICATION
    // ==============================
    @PostMapping("/toggle-verification/{id}")
    public String toggle(@PathVariable Long id) {

        Person p = personRepo.findById(id).orElseThrow();
        p.setVerified(!p.isVerified());
        personRepo.save(p);

        return "redirect:/diplomas";
    }

    // ==============================
    // DELETE
    // ==============================
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        personRepo.deleteById(id);
        return "redirect:/diplomas";
    }
}
