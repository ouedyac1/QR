package com.dcsic.qrcode.web.rest;

import com.dcsic.qrcode.dao.repository.EventRepo;
import com.dcsic.qrcode.model.entities.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class EventController {

    @Autowired
    private EventRepo eventRepository;

    @GetMapping("/event")
    public String showEventPage(Model model) {
        // On récupère le premier événement s'il existe
        //Event event = eventRepository.findAll().stream().findFirst().orElse(null);

        // On récupère tout les événement qui existe ---Y O
        List<Event> events = eventRepository.findAll();
        for (Event e : events) { System.out.println(e.getName()); }

        model.addAttribute("event", events);
        // On prépare un objet vide pour le formulaire de création
        model.addAttribute("newEvent", new Event());

        return "event";
    }

    @PostMapping("/event/create")
    public String createEvent(@ModelAttribute Event newEvent) {
        // Logique simple pour générer le slug à partir du nom (en minuscules, sans espaces)
        if (newEvent.getName() != null) {
            String generatedSlug = newEvent.getName().toLowerCase()
                    .replaceAll("[^a-z0-9]", "-")
                    .replaceAll("-+", "-");
            newEvent.setSlug(generatedSlug);
        }

        eventRepository.save(newEvent);
        return "redirect:/event"; // Recharge la page pour afficher le nouvel event
    }


}