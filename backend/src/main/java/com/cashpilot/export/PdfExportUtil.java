package com.cashpilot.export;

import com.cashpilot.entity.Expense;
import com.cashpilot.entity.Income;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Function;

/**
 * Builds simple tabular PDF exports for despesas/receitas: a title, a header line,
 * and one line of text per row, paginating every {@value #LINHAS_POR_PAGINA} rows.
 * Deliberately no text wrapping/layout engine — this is a plain listing, not a
 * formatted report.
 */
@Component
public class PdfExportUtil {

    private static final int LINHAS_POR_PAGINA = 40;
    private static final float MARGEM = 50f;
    private static final float ALTURA_LINHA = 15f;
    private static final float FONT_SIZE = 10f;

    public byte[] buildDespesasPdf(List<Expense> despesas) {
        return build("Relatório de Despesas", "Descrição | Categoria | Conta/Cartão | Data | Valor | Status",
                despesas, this::formatarDespesa);
    }

    public byte[] buildReceitasPdf(List<Income> receitas) {
        return build("Relatório de Receitas", "Descrição | Categoria | Conta | Data | Valor | Status",
                receitas, this::formatarReceita);
    }

    private <T> byte[] build(String titulo, String cabecalho, List<T> itens, Function<T, String> formatador) {
        try (PDDocument document = new PDDocument()) {
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDPage page = newPage(document);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float y = writeTitleAndHeader(contentStream, fontBold, titulo, cabecalho);

            int linhasNaPagina = 0;
            for (T item : itens) {
                if (linhasNaPagina >= LINHAS_POR_PAGINA) {
                    contentStream.close();
                    PDPage novaPagina = newPage(document);
                    contentStream = new PDPageContentStream(document, novaPagina);
                    y = writeTitleAndHeader(contentStream, fontBold, titulo, cabecalho);
                    linhasNaPagina = 0;
                }

                y -= ALTURA_LINHA;
                writeLine(contentStream, font, MARGEM, y, formatador.apply(item));
                linhasNaPagina++;
            }

            contentStream.close();

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                document.save(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao gerar PDF", e);
        }
    }

    private PDPage newPage(PDDocument document) {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        return page;
    }

    private float writeTitleAndHeader(PDPageContentStream contentStream, PDFont fontBold, String titulo, String cabecalho) throws IOException {
        float y = PDRectangle.A4.getHeight() - MARGEM;
        writeLine(contentStream, fontBold, MARGEM, y, titulo);
        y -= ALTURA_LINHA * 1.5f;
        writeLine(contentStream, fontBold, MARGEM, y, cabecalho);
        return y;
    }

    private void writeLine(PDPageContentStream contentStream, PDFont font, float x, float y, String texto) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, FONT_SIZE);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(texto == null ? "" : texto);
        contentStream.endText();
    }

    private String formatarDespesa(Expense despesa) {
        String categoria = despesa.getCategoria() != null ? despesa.getCategoria().getNome() : "";
        String contaOuCartao;
        if (despesa.getContaBancaria() != null) {
            contaOuCartao = despesa.getContaBancaria().getNome();
        } else if (despesa.getCartaoCredito() != null) {
            contaOuCartao = "Cartão: " + despesa.getCartaoCredito().getNome();
        } else {
            contaOuCartao = "";
        }
        String status = Boolean.TRUE.equals(despesa.getPaga()) ? "Paga" : "Pendente";
        return "%s | %s | %s | %s | %s | %s".formatted(
                despesa.getDescricao(), categoria, contaOuCartao, despesa.getData(), despesa.getValor(), status);
    }

    private String formatarReceita(Income receita) {
        String categoria = receita.getCategoria() != null ? receita.getCategoria().getNome() : "";
        String conta = receita.getContaBancaria() != null ? receita.getContaBancaria().getNome() : "";
        String status = Boolean.TRUE.equals(receita.getRecebida()) ? "Recebida" : "Pendente";
        return "%s | %s | %s | %s | %s | %s".formatted(
                receita.getDescricao(), categoria, conta, receita.getData(), receita.getValor(), status);
    }

}
