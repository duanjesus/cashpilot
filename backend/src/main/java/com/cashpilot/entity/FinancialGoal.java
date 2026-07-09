package com.cashpilot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "metas_financeiras")
public class FinancialGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "valor_alvo", nullable = false)
    private BigDecimal valorAlvo;

    @Column(name = "data_alvo", nullable = false)
    private LocalDate dataAlvo;

    @Column(name = "valor_atual", nullable = false)
    @Builder.Default
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativa = true;

}
