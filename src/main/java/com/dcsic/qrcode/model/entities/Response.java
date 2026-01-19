package com.dcsic.qrcode.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Question question;

    private String participantId;
    private String answer;
    private LocalDateTime submittedAt = LocalDateTime.now();

    public void setQuestion(Question question) {
        this.question = question;
    }
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }



}

