package com.dcsic.qrcode.web.rest;

import com.dcsic.qrcode.service.InstitutionService;
import com.dcsic.qrcode.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
//@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor // Génère le constructeur pour les champs 'final'
public class UserAdminController {

    private final UserService userService;
    private final InstitutionService institutionService;

    /**
     * UNIQUE méthode GET pour afficher la page des utilisateurs.
     * Elle charge à la fois la liste des utilisateurs et celle des institutions.
     */
    @GetMapping
    public String listAll(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("institutions", institutionService.findAll());
        return "admin/users";
    }

    @PostMapping("/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam(name = "institutionId", required = false) Long institutionId,
                             RedirectAttributes ra) {

        // 1. Sécurité : vérifier si l'utilisateur existe déjà avant d'essayer de l'insérer
        if (userService.exists(username)) {
            ra.addFlashAttribute("error", "L'utilisateur \"" + username + "\" existe déjà.");
            return "redirect:/admin/users";
        }

        try {
            // 2. Appel au service
            userService.createUser(username, password, institutionId);
            ra.addFlashAttribute("success", "Utilisateur \"" + username + "\" créé avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam String username, RedirectAttributes ra) {
        try {
            userService.deleteUser(username);
            ra.addFlashAttribute("success", "Utilisateur \"" + username + "\" supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la suppression.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String username,
                                 @RequestParam String newPassword,
                                 RedirectAttributes ra) {
        try {
            userService.changePassword(username, newPassword);
            ra.addFlashAttribute("success", "Mot de passe mis à jour pour " + username);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors du changement de mot de passe.");
        }
        return "redirect:/admin/users";
    }
}