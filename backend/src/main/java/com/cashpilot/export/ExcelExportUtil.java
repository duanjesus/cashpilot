package com.cashpilot.export;

import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Income;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Builds simple tabular .xlsx exports for despesas/receitas. Plain strings in every
 * cell — no native Excel date/number types, no persistence dependency.
 */
@Component
public class ExcelExportUtil {

    private static final String[] HEADERS = {"Descrição", "Categoria", "Conta/Cartão", "Data", "Valor", "Status"};

    public byte[] buildDespesasWorkbook(List<Expense> despesas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Despesas");
            writeHeader(workbook, sheet);

            int rowIdx = 1;
            for (Expense despesa : despesas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(despesa.getDescricao());
                row.createCell(1).setCellValue(despesa.getCategoria() != null ? despesa.getCategoria().getNome() : "");
                row.createCell(2).setCellValue(resolveContaOuCartao(despesa));
                row.createCell(3).setCellValue(despesa.getData() != null ? despesa.getData().toString() : "");
                row.createCell(4).setCellValue(despesa.getValor() != null ? despesa.getValor().toString() : "");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(despesa.getPaga()) ? "Paga" : "Pendente");
            }

            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gerar planilha de despesas", e);
        }
    }

    public byte[] buildReceitasWorkbook(List<Income> receitas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Receitas");
            writeHeader(workbook, sheet);

            int rowIdx = 1;
            for (Income receita : receitas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(receita.getDescricao());
                row.createCell(1).setCellValue(receita.getCategoria() != null ? receita.getCategoria().getNome() : "");
                row.createCell(2).setCellValue(receita.getContaBancaria() != null ? receita.getContaBancaria().getNome() : "");
                row.createCell(3).setCellValue(receita.getData() != null ? receita.getData().toString() : "");
                row.createCell(4).setCellValue(receita.getValor() != null ? receita.getValor().toString() : "");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(receita.getRecebida()) ? "Recebida" : "Pendente");
            }

            return toBytes(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gerar planilha de receitas", e);
        }
    }

    private String resolveContaOuCartao(Expense despesa) {
        if (despesa.getContaBancaria() != null) {
            return despesa.getContaBancaria().getNome();
        }
        if (despesa.getCartaoCredito() != null) {
            return "Cartão: " + despesa.getCartaoCredito().getNome();
        }
        return "";
    }

    private void writeHeader(XSSFWorkbook workbook, Sheet sheet) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }

}
