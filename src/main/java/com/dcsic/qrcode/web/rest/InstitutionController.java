package com.dcsic.qrcode.web.rest;

import com.dcsic.qrcode.dao.repository.InstitutionRepo;
import com.dcsic.qrcode.service.InstitutionService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@AllArgsConstructor
@Controller
@RequestMapping("/admin/institutions")
@PreAuthorize("isAuthenticated()")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final InstitutionRepo institutionRepo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("institutions", institutionRepo.findAll());
        return "admin/institutions";
    }

    @PostMapping("/create")
    public String create(@RequestParam String code,
                         @RequestParam String title,
                         @RequestParam String email,
                         RedirectAttributes ra) {
        if (institutionService.existsByCode(code.toUpperCase())) {
            ra.addFlashAttribute("error", "Le code \"" + code.toUpperCase() + "\" existe déjà.");
            return "redirect:/admin/institutions";
        }
        institutionService.create(code, title, email);
        ra.addFlashAttribute("success", "Institution \"" + title + "\" créée.");
        return "redirect:/admin/institutions";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long id,
                         @RequestParam String code,
                         @RequestParam String title,
                         @RequestParam String email,
                         RedirectAttributes ra) {
        institutionService.update(id, code, title, email);
        ra.addFlashAttribute("success", "Institution mise à jour.");
        return "redirect:/admin/institutions";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        institutionService.delete(id);
        ra.addFlashAttribute("success", "Institution supprimée.");
        return "redirect:/admin/institutions";
    }
}