package com.techzone.peru.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "conversaciones_chatbot")
@Data
public class ConversacionChatbot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String intencion;
    private String mensajeUsuario;
    private String respuestaBot;

    @Column(columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private OffsetDateTime fecha;
}