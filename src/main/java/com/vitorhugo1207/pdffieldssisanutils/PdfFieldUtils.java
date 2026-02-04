package com.vitorhugo1207.pdffieldssisanutils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.util.List;

/**
 * Classe utilitária para criar campos estilizados em relatórios PDF usando
 * iText 5
 * Compatível com layout responsivo para campos lado a lado
 */
public class PdfFieldUtils {

    // --- CONFIGURAÇÕES VISUAIS ---
    private static final float LINE_WIDTH = 0.5f;
    private static final float CORNER_RADIUS = 6f;

    // Tamanhos
    private static final float NUMBER_BOX_SIZE = 14f;
    private static final float CHECKBOX_SIZE = 10f;
    private static final float ANSWER_BOX_SIZE = 16f;

    // Fontes
    private static Font TITLE_FONT;
    private static Font LEGEND_FONT;
    private static Font CONTENT_FONT;
    private static Font NUMBER_FONT;

    static {
        try {
            TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);
            LEGEND_FONT = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
            CONTENT_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
            NUMBER_FONT = new Font(Font.FontFamily.HELVETICA, 7f, Font.BOLD, BaseColor.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- EVENTOS ---

    static class RoundBottomBorderEvent implements PdfPCellEvent {
        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
            cb.saveState();
            cb.setLineWidth(LINE_WIDTH);
            cb.setColorStroke(BaseColor.BLACK);

            float adj = LINE_WIDTH / 2;
            float left = position.getLeft() + adj;
            float right = position.getRight() - adj;
            float top = position.getTop() - adj;
            float bottom = position.getBottom() + adj;
            float r = CORNER_RADIUS;
            float b = 0.552284749831f * r;

            cb.moveTo(left, top);
            cb.lineTo(left, bottom + r);
            cb.curveTo(left, bottom + r - b, left + r - b, bottom, left + r, bottom);
            cb.lineTo(right - r, bottom);
            cb.curveTo(right - r + b, bottom, right, bottom + r - b, right, bottom + r);
            cb.lineTo(right, top);

            cb.stroke();
            cb.restoreState();
        }
    }

    /**
     * Desenha o quadrado E o número manualmente no topo esquerdo.
     * Adiciona preenchimento Branco para cobrir linhas de fundo.
     */
    static class TopLeftSquareEvent implements PdfPCellEvent {
        private final float size;
        private final String text;

        public TopLeftSquareEvent(float size, String text) {
            this.size = size;
            this.text = text;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
            cb.saveState();
            cb.setLineWidth(LINE_WIDTH);
            cb.setColorStroke(BaseColor.BLACK);
            cb.setColorFill(BaseColor.WHITE); // Fundo Branco

            float adj = LINE_WIDTH / 2;
            float x = position.getLeft() + adj;
            float y = position.getTop() - adj;

            // Desenha Quadrado (Preenchido e com Borda)
            cb.rectangle(x, y - size, size, size);
            cb.fillStroke();
            cb.restoreState();

            // Desenha o Texto Centralizado no Quadrado
            PdfContentByte textCanvas = canvases[PdfPTable.TEXTCANVAS];
            textCanvas.saveState();
            float centerX = x + (size / 2);
            float centerY = y - (size / 2);
            float verticalOffset = (NUMBER_FONT.getSize() / 3.5f);

            ColumnText.showTextAligned(textCanvas, Element.ALIGN_CENTER,
                    new Phrase(text, NUMBER_FONT), centerX, centerY - verticalOffset, 0);
            textCanvas.restoreState();
        }
    }

    static class CenteredSquareEvent implements PdfPCellEvent {
        private final float size;

        public CenteredSquareEvent(float size) {
            this.size = size;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
            cb.saveState();
            cb.setLineWidth(LINE_WIDTH);
            cb.setColorStroke(BaseColor.BLACK);
            float centerX = (position.getLeft() + position.getRight()) / 2;
            float centerY = (position.getTop() + position.getBottom()) / 2;
            cb.rectangle(centerX - (size / 2), centerY - (size / 2), size, size);
            cb.stroke();
            cb.restoreState();
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private static void configureMainCell(PdfPCell mainCell) {
        mainCell.setBorder(Rectangle.NO_BORDER);
        mainCell.setCellEvent(new RoundBottomBorderEvent());
        mainCell.setPaddingTop(0);
        mainCell.setPaddingLeft(0);
        mainCell.setPaddingRight(0);
        mainCell.setPaddingBottom(5f);
    }

    /**
     * Cria a célula de cabeçalho unificada.
     * Título na mesma célula do número, apenas empurrado com Padding.
     */
    private static PdfPCell createHeaderCell(String number, String title) {
        PdfPCell cell = new PdfPCell(new Phrase(title, TITLE_FONT));
        cell.setBorder(Rectangle.NO_BORDER);

        // 1. O Evento desenha o número no canto esquerdo (0,0 da célula)
        cell.setCellEvent(new TopLeftSquareEvent(NUMBER_BOX_SIZE, number));

        // 2. Empurramos o texto do título para a direita para não bater no quadrado
        // Tamanho do quadrado + 5pt de margem visual
        cell.setPaddingLeft(NUMBER_BOX_SIZE + 5f);

        // Ajuste vertical para o título alinhar com o centro do quadrado
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Pequeno ajuste de topo para o texto não ficar colado na linha superior
        // invisível
        cell.setPaddingTop(2f);

        // Garante altura mínima para caber o quadrado
        cell.setMinimumHeight(NUMBER_BOX_SIZE + 2f);

        return cell;
    }

    // --- CAMPOS ---

    /** TIPO 1: Zona (29) */
    public static PdfPCell createFieldWithLegendAndAnswerBox(
            String fieldNumber, String title, List<String> legendLines, String answer, float widthPercentage) {

        // Tabela principal interna de 2 colunas:
        // Coluna 1: (85%) Título + Legendas
        // Coluna 2: (15%) Caixa de Resposta (Alinhada ao Topo)
        PdfPTable splitTable = new PdfPTable(new float[] { 85f, 15f });
        splitTable.setWidthPercentage(100);

        // --- COLUNA DA ESQUERDA (Título + Legenda) ---
        PdfPTable leftContent = new PdfPTable(1);
        leftContent.setWidthPercentage(100);
        leftContent.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // 1. Cabeçalho (Número [29] + Título "Zona")
        // Usamos a lógica de célula única para evitar desalinhamento
        PdfPCell headerCell = new PdfPCell(new Phrase(title, TITLE_FONT));
        headerCell.setBorder(Rectangle.NO_BORDER);
        // Desenha o quadrado [29] no topo esquerdo
        headerCell.setCellEvent(new TopLeftSquareEvent(NUMBER_BOX_SIZE, fieldNumber));
        // Empurra o texto "Zona" para a direita
        headerCell.setPaddingLeft(NUMBER_BOX_SIZE + 5f);
        headerCell.setPaddingTop(2f);
        headerCell.setMinimumHeight(NUMBER_BOX_SIZE + 2f);
        leftContent.addCell(headerCell);

        // 2. Legendas (Logo abaixo do título)
        for (String line : legendLines) {
            PdfPCell l = new PdfPCell(new Phrase(line, LEGEND_FONT));
            l.setBorder(Rectangle.NO_BORDER);
            // Alinha com o texto do título (pula o quadrado)
            l.setPaddingLeft(NUMBER_BOX_SIZE + 5f);
            l.setPaddingTop(0f); // Cola no título
            l.setPaddingBottom(1f);
            leftContent.addCell(l);
        }

        PdfPCell leftCell = new PdfPCell(leftContent);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(0);
        splitTable.addCell(leftCell);

        // --- COLUNA DA DIREITA (Resposta [1]) ---
        // Usamos uma tabela aninhada para garantir o mesmo alinhamento vertical da esquerda
        PdfPTable rightContent = new PdfPTable(1);
        rightContent.setWidthPercentage(100);
        rightContent.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Célula vazia no corpo, a resposta é desenhada dentro do quadrado pelo evento
        PdfPCell answerCell = new PdfPCell(new Phrase("", CONTENT_FONT));
        answerCell.setBorder(Rectangle.NO_BORDER);

        // Desenha o quadrado com a resposta dentro
        answerCell.setCellEvent(new TopLeftSquareEvent(ANSWER_BOX_SIZE, answer != null ? answer : ""));

        // Remove padding para o quadrado ficar no topo absoluto da célula
        answerCell.setPadding(0);
        answerCell.setPaddingTop(0);

        // Altura mínima para não cortar
        answerCell.setMinimumHeight(ANSWER_BOX_SIZE + 2f);

        rightContent.addCell(answerCell);

        PdfPCell rightCell = new PdfPCell(rightContent);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(0);

        splitTable.addCell(rightCell);

        // --- CÉLULA MESTRA (Wrapper com borda em U) ---
        PdfPCell mainCell = new PdfPCell(splitTable);
        configureMainCell(mainCell); // Borda redonda, padding 0

        return mainCell;
    }

    /** TIPO 2: Descritivo (32) */
    public static PdfPCell createDescriptiveField(
            String fieldNumber, String title, String content, float widthPercentage, float minHeight) {

        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        // Header unificado (Corrige o problema do "empurrão")
        mainTable.addCell(createHeaderCell(fieldNumber, title));

        // Conteúdo
        PdfPCell contentCell = new PdfPCell(new Phrase(content != null ? content : "", CONTENT_FONT));
        contentCell.setBorder(Rectangle.NO_BORDER);
        contentCell.setPaddingLeft(5f);
        contentCell.setPaddingRight(5f);
        contentCell.setPaddingBottom(5f);
        mainTable.addCell(contentCell);

        PdfPCell mainCell = new PdfPCell(mainTable);
        configureMainCell(mainCell);
        if (minHeight > 0)
            mainCell.setMinimumHeight(minHeight);
        return mainCell;
    }

    /** TIPO 3: Múltipla Escolha (33) */
    public static PdfPCell createMultipleOptionsField(
            String fieldNumber, String title, String legend, List<String> options,
            List<String> answers, int columns, float widthPercentage, boolean hasOther, String otherVal) {

        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        // Header Personalizado para incluir Legenda na mesma linha
        // Usamos Phrase para manter o modo texto e o alinhamento correto (igual ao createHeaderCell)
        Phrase headerPhrase = new Phrase();
        headerPhrase.add(new Chunk(title, TITLE_FONT));
        headerPhrase.add(new Chunk("   " + legend, LEGEND_FONT));

        PdfPCell headerCell = new PdfPCell(headerPhrase);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setCellEvent(new TopLeftSquareEvent(NUMBER_BOX_SIZE, fieldNumber));
        headerCell.setPaddingLeft(NUMBER_BOX_SIZE + 5f); // Padding para o quadrado
        headerCell.setPaddingTop(2f);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerCell.setMinimumHeight(NUMBER_BOX_SIZE + 2f);

        mainTable.addCell(headerCell);

        // Grid de Opções
        float[] ws = new float[columns];
        for (int i = 0; i < columns; i++)
            ws[i] = 100f / columns;
        PdfPTable grid = new PdfPTable(ws);
        grid.setWidthPercentage(100);

        for (int i = 0; i < options.size(); i++) {
            String ans = (answers != null && i < answers.size()) ? answers.get(i) : "";
            grid.addCell(createOptionWithCheckbox(options.get(i), ans));
        }
        int rem = options.size() % columns;
        if (rem > 0) {
            for (int i = 0; i < (columns - rem); i++) {
                PdfPCell e = new PdfPCell(new Phrase(""));
                e.setBorder(Rectangle.NO_BORDER);
                grid.addCell(e);
            }
        }

        PdfPCell gridRow = new PdfPCell(grid);
        gridRow.setBorder(Rectangle.NO_BORDER);
        gridRow.setPaddingLeft(5f);
        mainTable.addCell(gridRow);

        // Outros
        if (hasOther) {
            PdfPTable otherTable = new PdfPTable(new float[] { 15f, 30f, 150f });
            otherTable.setWidthPercentage(100);

            PdfPCell ck = new PdfPCell(new Phrase(""));
            ck.setBorder(Rectangle.NO_BORDER);
            ck.setCellEvent(new CenteredSquareEvent(CHECKBOX_SIZE));
            ck.setFixedHeight(CHECKBOX_SIZE + 4f);
            otherTable.addCell(ck);

            PdfPCell lbl = new PdfPCell(new Phrase("Outros:", LEGEND_FONT));
            lbl.setBorder(Rectangle.NO_BORDER);
            lbl.setVerticalAlignment(Element.ALIGN_MIDDLE);
            otherTable.addCell(lbl);

            PdfPCell v = new PdfPCell(new Phrase(otherVal != null ? otherVal : "", CONTENT_FONT));
            v.setBorder(Rectangle.BOTTOM);
            v.setBorderWidth(0.5f);
            otherTable.addCell(v);

            PdfPCell oRow = new PdfPCell(otherTable);
            oRow.setBorder(Rectangle.NO_BORDER);
            oRow.setPaddingLeft(5f);
            oRow.setPaddingTop(5f);
            mainTable.addCell(oRow);
        }

        PdfPCell mainCell = new PdfPCell(mainTable);
        configureMainCell(mainCell);
        return mainCell;
    }

    private static PdfPCell createOptionWithCheckbox(String txt, String ans) {
        PdfPTable t = new PdfPTable(new float[] { 15f, 85f });
        t.setWidthPercentage(100);

        PdfPCell ck = new PdfPCell(new Phrase(ans != null ? ans : "", new Font(Font.FontFamily.HELVETICA, 7)));
        ck.setBorder(Rectangle.NO_BORDER);
        ck.setCellEvent(new CenteredSquareEvent(CHECKBOX_SIZE));
        ck.setFixedHeight(CHECKBOX_SIZE + 4f);
        ck.setHorizontalAlignment(Element.ALIGN_CENTER);
        ck.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(ck);

        PdfPCell tx = new PdfPCell(new Phrase(txt, LEGEND_FONT));
        tx.setBorder(Rectangle.NO_BORDER);
        tx.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tx.setPaddingLeft(2f);
        t.addCell(tx);

        PdfPCell c = new PdfPCell(t);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    public static PdfPTable createResponsiveRow(PdfPCell[] cells, float[] widths) {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        t.setSpacingBefore(0f);
        for (PdfPCell c : cells)
            t.addCell(c);
        return t;
    }
}
