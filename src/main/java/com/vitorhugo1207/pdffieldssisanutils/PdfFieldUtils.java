package com.vitorhugo1207.pdffieldssisanutils;

import java.util.List;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.renderer.IRenderer;

/**
 * Classe utilitária para criar campos estilizados em relatórios PDF usando
 * iText 8+
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

    // Fontes (iText 8+)
    private static final PdfFont TITLE_FONT;
    private static final PdfFont LEGEND_FONT;
    private static final PdfFont CONTENT_FONT;
    private static final PdfFont NUMBER_FONT;

    private static final float TITLE_FONT_SIZE = 8f;
    private static final float LEGEND_FONT_SIZE = 7f;
    private static final float CONTENT_FONT_SIZE = 9f;
    private static final float NUMBER_FONT_SIZE = 7f;

    static {
        try {
            TITLE_FONT = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            LEGEND_FONT = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            CONTENT_FONT = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            NUMBER_FONT = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // --- RENDERERS (equivalente aos eventos do iText 5) ---

    static class RoundBottomBorderRenderer extends CellRenderer {
        public RoundBottomBorderRenderer(Cell modelElement) {
            super(modelElement);
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);

            Rectangle position = getOccupiedAreaBBox();
            PdfCanvas cb = drawContext.getCanvas();
            cb.saveState();
            cb.setLineWidth(LINE_WIDTH);
            cb.setStrokeColor(ColorConstants.BLACK);

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

        @Override
        public IRenderer getNextRenderer() {
            return new RoundBottomBorderRenderer((Cell) getModelElement());
        }
    }

    /**
     * Desenha o quadrado e o texto no topo esquerdo.
     * Útil tanto para número do campo quanto para caixa de resposta.
     */
    static class TopLeftSquareRenderer extends CellRenderer {
        private final float size;
        private final String text;
        private final PdfFont font;
        private final float fontSize;

        public TopLeftSquareRenderer(Cell modelElement, float size, String text, PdfFont font, float fontSize) {
            super(modelElement);
            this.size = size;
            this.text = text;
            this.font = font;
            this.fontSize = fontSize;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);

            Rectangle position = getOccupiedAreaBBox();
            PdfCanvas cb = drawContext.getCanvas();

            cb.saveState();
            cb.setLineWidth(LINE_WIDTH);
            cb.setStrokeColor(ColorConstants.BLACK);
            cb.setFillColor(ColorConstants.WHITE);

            float adj = LINE_WIDTH / 2;
            float x = position.getLeft() + adj;
            float yTop = position.getTop() - adj;

            cb.rectangle(x, yTop - size, size, size);
            cb.fillStroke();
            cb.restoreState();

            String safeText = text != null ? text : "";
            if (!safeText.isEmpty()) {
                float centerX = x + (size / 2);
                float centerY = yTop - (size / 2);
                float verticalOffset = (fontSize / 3.5f);

                cb.saveState();
                cb.beginText();
                cb.setFontAndSize(font, fontSize);
                float textWidth = font.getWidth(safeText) * fontSize / 1000f;
                float textX = centerX - (textWidth / 2);
                float textY = (centerY - verticalOffset) - (fontSize / 3.2f);
                cb.moveText(textX, textY);
                cb.showText(safeText);
                cb.endText();
                cb.restoreState();
            }
        }

        @Override
        public IRenderer getNextRenderer() {
            return new TopLeftSquareRenderer((Cell) getModelElement(), size, text, font, fontSize);
        }
    }

    static class CenteredSquareRenderer extends CellRenderer {
        private final float size;

        public CenteredSquareRenderer(Cell modelElement, float size) {
            super(modelElement);
            this.size = size;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);

            Rectangle position = getOccupiedAreaBBox();
            PdfCanvas cb = drawContext.getCanvas();
            cb.saveState();
            cb.setLineWidth(LINE_WIDTH);
            cb.setStrokeColor(ColorConstants.BLACK);

            float centerX = (position.getLeft() + position.getRight()) / 2;
            float centerY = (position.getTop() + position.getBottom()) / 2;
            cb.rectangle(centerX - (size / 2), centerY - (size / 2), size, size);
            cb.stroke();
            cb.restoreState();
        }

        @Override
        public IRenderer getNextRenderer() {
            return new CenteredSquareRenderer((Cell) getModelElement(), size);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private static void configureMainCell(Cell mainCell) {
        mainCell.setBorder(Border.NO_BORDER);
        mainCell.setNextRenderer(new RoundBottomBorderRenderer(mainCell));
        mainCell.setPaddingTop(0);
        mainCell.setPaddingLeft(0);
        mainCell.setPaddingRight(0);
        mainCell.setPaddingBottom(5f);
    }

    /**
     * Cria a célula de cabeçalho unificada.
     * Título na mesma célula do número, apenas empurrado com Padding.
     */
    private static Cell createHeaderCell(String number, String title) {
        Paragraph p = new Paragraph(title != null ? title : "")
                .setFont(TITLE_FONT)
                .setFontSize(TITLE_FONT_SIZE)
                .setMargin(0);

        Cell cell = new Cell().add(p);
        cell.setBorder(Border.NO_BORDER);
        cell.setPaddingLeft(NUMBER_BOX_SIZE + 5f);
        cell.setPaddingTop(2f);
        cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cell.setMinHeight(NUMBER_BOX_SIZE + 2f);
        cell.setNextRenderer(new TopLeftSquareRenderer(cell, NUMBER_BOX_SIZE, number, NUMBER_FONT, NUMBER_FONT_SIZE));
        return cell;
    }

    // --- CAMPOS ---

    /** TIPO 1: Zona (29) */
    public static Cell createFieldWithLegendAndAnswerBox(
            String fieldNumber, String title, List<String> legendLines, String answer, float widthPercentage) {

        // Tabela principal interna de 2 colunas:
        // Coluna 1: (85%) Título + Legendas
        // Coluna 2: (15%) Caixa de Resposta (Alinhada ao Topo)
        Table splitTable = new Table(UnitValue.createPercentArray(new float[] { 85f, 15f }))
            .useAllAvailableWidth();

        // --- COLUNA DA ESQUERDA (Título + Legenda) ---
        Table leftContent = new Table(1).useAllAvailableWidth();

        // 1. Cabeçalho (Número [29] + Título "Zona")
        // Usamos a lógica de célula única para evitar desalinhamento
        Paragraph headerP = new Paragraph(title != null ? title : "")
            .setFont(TITLE_FONT)
            .setFontSize(TITLE_FONT_SIZE)
            .setMargin(0);
        Cell headerCell = new Cell().add(headerP);
        headerCell.setBorder(Border.NO_BORDER);
        headerCell.setPaddingLeft(NUMBER_BOX_SIZE + 5f);
        headerCell.setPaddingTop(2f);
        headerCell.setMinHeight(NUMBER_BOX_SIZE + 2f);
        headerCell.setNextRenderer(new TopLeftSquareRenderer(headerCell, NUMBER_BOX_SIZE, fieldNumber, NUMBER_FONT, NUMBER_FONT_SIZE));
        leftContent.addCell(headerCell);

        // 2. Legendas (Logo abaixo do título)
        for (String line : legendLines) {
            Paragraph lp = new Paragraph(line != null ? line : "")
                    .setFont(LEGEND_FONT)
                    .setFontSize(LEGEND_FONT_SIZE)
                    .setMargin(0);
            Cell l = new Cell().add(lp);
            l.setBorder(Border.NO_BORDER);
            l.setPaddingLeft(NUMBER_BOX_SIZE + 5f);
            l.setPaddingTop(0f);
            l.setPaddingBottom(1f);
            leftContent.addCell(l);
        }

        Cell leftCell = new Cell().add(leftContent);
        leftCell.setBorder(Border.NO_BORDER);
        leftCell.setPadding(0);
        splitTable.addCell(leftCell);

        // --- COLUNA DA DIREITA (Resposta [1]) ---
        // Usamos uma tabela aninhada para garantir o mesmo alinhamento vertical da esquerda
        Table rightContent = new Table(1).useAllAvailableWidth();

        // Célula vazia no corpo, a resposta é desenhada dentro do quadrado pelo evento
        Cell answerCell = new Cell();
        answerCell.setBorder(Border.NO_BORDER);
        answerCell.setPadding(0);
        answerCell.setMinHeight(ANSWER_BOX_SIZE + 2f);
        answerCell.setNextRenderer(new TopLeftSquareRenderer(answerCell, ANSWER_BOX_SIZE,
            answer != null ? answer : "", NUMBER_FONT, NUMBER_FONT_SIZE));
        rightContent.addCell(answerCell);

        Cell rightCell = new Cell().add(rightContent);
        rightCell.setBorder(Border.NO_BORDER);
        rightCell.setPadding(0);
        splitTable.addCell(rightCell);

        // --- CÉLULA MESTRA (Wrapper com borda em U) ---
        Cell mainCell = new Cell().add(splitTable);
        configureMainCell(mainCell);
        return mainCell;
    }

    /** TIPO 2: Descritivo (32) */
    public static Cell createDescriptiveField(
            String fieldNumber, String title, String content, float widthPercentage, float minHeight) {

        Table mainTable = new Table(1).useAllAvailableWidth();

        // Header unificado (Corrige o problema do "empurrão")
        mainTable.addCell(createHeaderCell(fieldNumber, title));

        // Conteúdo
        Paragraph cp = new Paragraph(content != null ? content : "")
                .setFont(CONTENT_FONT)
                .setFontSize(CONTENT_FONT_SIZE)
                .setMargin(0);
        Cell contentCell = new Cell().add(cp);
        contentCell.setBorder(Border.NO_BORDER);
        contentCell.setPaddingLeft(5f);
        contentCell.setPaddingRight(5f);
        contentCell.setPaddingBottom(5f);
        mainTable.addCell(contentCell);

        Cell mainCell = new Cell().add(mainTable);
        configureMainCell(mainCell);
        if (minHeight > 0)
            mainCell.setMinHeight(minHeight);
        return mainCell;
    }

    /** TIPO 3: Múltipla Escolha (33) */
    public static Cell createMultipleOptionsField(
            String fieldNumber, String title, String legend, List<String> options,
            List<String> answers, int columns, float widthPercentage, boolean hasOther, String otherVal) {

        Table mainTable = new Table(1).useAllAvailableWidth();

        // Header Personalizado para incluir Legenda na mesma linha
        // Usamos Phrase para manter o modo texto e o alinhamento correto (igual ao createHeaderCell)
        Paragraph headerP = new Paragraph()
            .setMargin(0);
        headerP.add(new Text(title != null ? title : "")
            .setFont(TITLE_FONT)
            .setFontSize(TITLE_FONT_SIZE));
        headerP.add(new Text("   " + (legend != null ? legend : ""))
            .setFont(LEGEND_FONT)
            .setFontSize(LEGEND_FONT_SIZE));

        Cell headerCell = new Cell().add(headerP);
        headerCell.setBorder(Border.NO_BORDER);
        headerCell.setPaddingLeft(NUMBER_BOX_SIZE + 5f);
        headerCell.setPaddingTop(2f);
        headerCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        headerCell.setMinHeight(NUMBER_BOX_SIZE + 2f);
        headerCell.setNextRenderer(new TopLeftSquareRenderer(headerCell, NUMBER_BOX_SIZE, fieldNumber, NUMBER_FONT, NUMBER_FONT_SIZE));
        mainTable.addCell(headerCell);

        // Grid de Opções
        float[] ws = new float[columns];
        for (int i = 0; i < columns; i++) {
            ws[i] = 100f / columns;
        }

        Table grid = new Table(UnitValue.createPercentArray(ws)).useAllAvailableWidth();

        for (int i = 0; i < options.size(); i++) {
            String ans = (answers != null && i < answers.size()) ? answers.get(i) : "";
            grid.addCell(createOptionWithCheckbox(options.get(i), ans));
        }
        int rem = options.size() % columns;
        if (rem > 0) {
            for (int i = 0; i < (columns - rem); i++) {
                Cell e = new Cell();
                e.setBorder(Border.NO_BORDER);
                grid.addCell(e);
            }
        }

        Cell gridRow = new Cell().add(grid);
        gridRow.setBorder(Border.NO_BORDER);
        gridRow.setPaddingLeft(5f);
        mainTable.addCell(gridRow);

        // Outros
        if (hasOther) {
            // Cria tabela com mesma estrutura do grid de opções (usando ws)
            Table otherRow = new Table(UnitValue.createPercentArray(ws)).useAllAvailableWidth();

            // Primeira coluna: checkbox + label "Outros:" (mesma estrutura de createOptionWithCheckbox)
                Table firstColTable = new Table(UnitValue.createPercentArray(new float[] { 15f, 85f }))
                    .useAllAvailableWidth();

                Cell ck = new Cell();
                ck.setBorder(Border.NO_BORDER);
                ck.setHeight(CHECKBOX_SIZE + 4f);
                ck.setTextAlignment(TextAlignment.CENTER);
                ck.setVerticalAlignment(VerticalAlignment.MIDDLE);
                ck.setNextRenderer(new CenteredSquareRenderer(ck, CHECKBOX_SIZE));
                firstColTable.addCell(ck);

                Paragraph lblP = new Paragraph("Outros:")
                    .setFont(LEGEND_FONT)
                    .setFontSize(LEGEND_FONT_SIZE)
                    .setMargin(0);
                Cell lbl = new Cell().add(lblP);
                lbl.setBorder(Border.NO_BORDER);
                lbl.setVerticalAlignment(VerticalAlignment.MIDDLE);
                lbl.setPaddingLeft(2f);
                firstColTable.addCell(lbl);

                Cell firstCol = new Cell().add(firstColTable);
                firstCol.setBorder(Border.NO_BORDER);
                firstCol.setPadding(0);
                otherRow.addCell(firstCol);

            // Colunas restantes: mescladas para o campo de input
            if (columns > 1) {
                Paragraph vP = new Paragraph(otherVal != null ? otherVal : "")
                        .setFont(CONTENT_FONT)
                        .setFontSize(CONTENT_FONT_SIZE)
                        .setMargin(0);
                Cell v = new Cell(1, columns - 1).add(vP);
                v.setBorderTop(Border.NO_BORDER);
                v.setBorderLeft(Border.NO_BORDER);
                v.setBorderRight(Border.NO_BORDER);
                v.setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.BLACK, 0.5f));
                v.setPaddingLeft(3f);
                v.setPaddingRight(50f);
                v.setVerticalAlignment(VerticalAlignment.MIDDLE);
                otherRow.addCell(v);
            }

            Cell oRowWrapper = new Cell().add(otherRow);
            oRowWrapper.setBorder(Border.NO_BORDER);
            oRowWrapper.setPaddingTop(5f);
            oRowWrapper.setPaddingLeft(5f);
            mainTable.addCell(oRowWrapper);
        }

        Cell mainCell = new Cell().add(mainTable);
        configureMainCell(mainCell);
        return mainCell;
    }

    private static Cell createOptionWithCheckbox(String txt, String ans) {
        Table t = new Table(UnitValue.createPercentArray(new float[] { 15f, 85f }))
                .useAllAvailableWidth();

        Paragraph ckP = new Paragraph(ans != null ? ans : "")
                .setFont(LEGEND_FONT)
                .setFontSize(LEGEND_FONT_SIZE)
                .setMargin(0);
        Cell ck = new Cell().add(ckP);
        ck.setBorder(Border.NO_BORDER);
        ck.setHeight(CHECKBOX_SIZE + 4f);
        ck.setTextAlignment(TextAlignment.CENTER);
        ck.setVerticalAlignment(VerticalAlignment.MIDDLE);
        ck.setNextRenderer(new CenteredSquareRenderer(ck, CHECKBOX_SIZE));
        t.addCell(ck);

        Paragraph txP = new Paragraph(txt != null ? txt : "")
                .setFont(LEGEND_FONT)
                .setFontSize(LEGEND_FONT_SIZE)
                .setMargin(0);
        Cell tx = new Cell().add(txP);
        tx.setBorder(Border.NO_BORDER);
        tx.setVerticalAlignment(VerticalAlignment.MIDDLE);
        tx.setPaddingLeft(2f);
        t.addCell(tx);

        Cell c = new Cell().add(t);
        c.setBorder(Border.NO_BORDER);
        return c;
    }

    public static Table createResponsiveRow(Cell[] cells, float[] widths) {
        Table t = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();
        t.setMarginTop(0f);
        t.setBorder(Border.NO_BORDER);
        for (Cell c : cells) {
            t.addCell(c);
        }
        return t;
    }
}
