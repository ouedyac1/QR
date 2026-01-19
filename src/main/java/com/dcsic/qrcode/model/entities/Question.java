package com.dcsic.qrcode.model.entities;

import com.dcsic.qrcode.model.enumeration.EQuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    private EQuestionType type;
    //private EQuestionType type;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    // utile si type = CHOICE_*
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionChoice> choices = new ArrayList<>();

    @Transient
    private Long eventId;

    public Long getEventId() { return eventId; }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

}

