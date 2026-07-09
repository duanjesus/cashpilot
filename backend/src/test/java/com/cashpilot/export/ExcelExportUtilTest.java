package com.cashpilot.export;

import com.cashpilot.entity.BankAccount;
import com.cashpilot.entity.Category;
import com.cashpilot.entity.CreditCard;
import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Income;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExcelExportUtil")
class ExcelExportUtilTest {

    private final ExcelExportUtil excelExportUtil = new ExcelExportUtil();

    @Test
    @DisplayName("Gera planilha de despesas com header + uma linha por despesa")
    void deveGerarPlanilhaDeDespesas() throws IOException {
        Category categoria = Category.builder().id(1L).nome("Moradia").build();
        BankAccount conta = BankAccount.builder().id(1L).nome("Conta Corrente").build();
        CreditCard cartao = CreditCard.builder().id(1L).nome("Cartão Nubank").build();

        Expense despesaPaga = Expense.builder().id(1L).categoria(categoria).contaBancaria(conta)
                .descricao("Aluguel").valor(BigDecimal.valueOf(1500)).data(LocalDate.of(2026, 1, 5)).paga(true).build();
        Expense despesaPendente = Expense.builder().id(2L).categoria(categoria).cartaoCredito(cartao)
                .descricao("Supermercado").valor(BigDecimal.valueOf(300)).data(LocalDate.of(2026, 1, 10)).paga(false).build();

        byte[] bytes = excelExportUtil.buildDespesasWorkbook(List.of(despesaPaga, despesaPendente));

        assertThat(bytes).isNotEmpty();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2); // header (row 0) + 2 data rows -> last row index 2

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Descrição");

            Row linha1 = sheet.getRow(1);
            assertThat(linha1.getCell(0).getStringCellValue()).isEqualTo("Aluguel");
            assertThat(linha1.getCell(5).getStringCellValue()).isEqualTo("Paga");

            Row linha2 = sheet.getRow(2);
            assertThat(linha2.getCell(2).getStringCellValue()).isEqualTo("Cartão: Cartão Nubank");
            assertThat(linha2.getCell(5).getStringCellValue()).isEqualTo("Pendente");
        }
    }

    @Test
    @DisplayName("Gera planilha de receitas com header + uma linha por receita")
    void deveGerarPlanilhaDeReceitas() throws IOException {
        Category categoria = Category.builder().id(2L).nome("Salário").build();
        BankAccount conta = BankAccount.builder().id(1L).nome("Conta Corrente").build();

        Income receitaRecebida = Income.builder().id(1L).categoria(categoria).contaBancaria(conta)
                .descricao("Salário Julho").valor(BigDecimal.valueOf(5000)).data(LocalDate.of(2026, 7, 5)).recebida(true).build();
        Income receitaPendente = Income.builder().id(2L).categoria(categoria).contaBancaria(conta)
                .descricao("Freelance").valor(BigDecimal.valueOf(800)).data(LocalDate.of(2026, 7, 20)).recebida(false).build();

        byte[] bytes = excelExportUtil.buildReceitasWorkbook(List.of(receitaRecebida, receitaPendente));

        assertThat(bytes).isNotEmpty();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);

            Row linha1 = sheet.getRow(1);
            assertThat(linha1.getCell(0).getStringCellValue()).isEqualTo("Salário Julho");
            assertThat(linha1.getCell(5).getStringCellValue()).isEqualTo("Recebida");

            Row linha2 = sheet.getRow(2);
            assertThat(linha2.getCell(5).getStringCellValue()).isEqualTo("Pendente");
        }
    }

}
