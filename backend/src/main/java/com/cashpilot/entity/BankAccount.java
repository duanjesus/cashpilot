package com.cashpilot.entity;

import com.cashpilot.entity.enums.BankAccountType;
import com.cashpilot.entity.enums.ContaOrigem;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "contas_bancarias")
public class BankAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankAccountType tipo;

    @Column(name = "saldo_inicial", nullable = false)
    private BigDecimal saldoInicial;

    @Column(name = "data_saldo_inicial", nullable = false)
    private LocalDate dataSaldoInicial;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativa = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContaOrigem origem = ContaOrigem.MANUAL;

    @Column(name = "instituicao_nome", length = 150)
    private String instituicaoNome;

    @Column(name = "ultima_sincronizacao")
    private LocalDateTime ultimaSincronizacao;

}
