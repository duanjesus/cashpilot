package com.cashpilot.export;

import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PdfExportUtil")
class PdfExportUtilTest {

    private final PdfExportUtil pdfExportUtil = new PdfExportUtil();

    @Test
    @DisplayName("Gera PDF válido (assinatura %PDF) para despesas")
    void deveGerarPdfDeDespesas() {
        Category categoria = Category.builder().id(1L).nome("Moradia").build();
        BankAccount conta = BankAccount.builder().id(1L).nome("Conta Corrente").build();
        Expense despesa = Expense.builder().id(1L).categoria(categoria).contaBancaria(conta)
                .descricao("Aluguel").valor(BigDecimal.valueOf(1500)).data(LocalDate.of(2026, 1, 5)).paga(true).build();

        byte[] bytes = pdfExportUtil.buildDespesasPdf(List.of(despesa));

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("Gera PDF válido para receitas, inclusive com lista vazia")
    void deveGerarPdfDeReceitasComListaVazia() {
        byte[] bytes = pdfExportUtil.buildReceitasPdf(List.of());

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

}
