package com.cashpilot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Does not extend {@link BaseEntity}: the {@code projecao_preferencias} table
 * (V9 migration) only has an {@code updated_at} column, no {@code created_at}.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "projecao_preferencias")
public class ProjectionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private BigDecimal salario;

    @Column(name = "despesas_fixas")
    private BigDecimal despesasFixas;

    @Column(name = "despesas_variaveis")
    private BigDecimal despesasVariaveis;

    @Column(name = "investimento_mensal")
    private BigDecimal investimentoMensal;

    @Column(name = "valor_alvo")
    private BigDecimal valorAlvo;

    @Column(name = "patrimonio_atual")
    private BigDecimal patrimonioAtual;

    @Column(name = "taxa_retorno_mensal")
    private BigDecimal taxaRetornoMensal;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
