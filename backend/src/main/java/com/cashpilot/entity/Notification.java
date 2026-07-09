package com.cashpilot.entity;

import com.cashpilot.entity.enums.NotificationTipo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "notificacoes")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationTipo tipo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(name = "referencia_tipo", nullable = false, length = 30)
    private String referenciaTipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "ano_mes_referencia", length = 7)
    private String anoMesReferencia;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lida = false;

}
