package com.vitorhugo1207.pdffieldssisanutils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

/**
 * Classe utilitária para criar campos estilizados em relatórios PDF usando
 * iText 5
 * Compatível com layout responsivo para campos lado a lado
 */
public class PdfFieldUtils {

    // Cores padrão
    private static final BaseColor BORDER_COLOR = BaseColor.BLACK;
    private static final BaseColor HEADER_BG_COLOR = BaseColor.BLACK;
    private static final BaseColor HEADER_TEXT_COLOR = BaseColor.WHITE;
    
    // Fontes padrão
    private static Font TITLE_FONT;
    private static Font LEGEND_FONT;
    private static Font CONTENT_FONT;
    
    static {
        try {
            TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);
            LEGEND_FONT = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
            CONTENT_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * CAMPO TIPO 1: Campo com legenda na parte inferior e quadrado de resposta à direita
     * Exemplo: Campo 29 (Zona) - "1 - Urbana  2 - Rural  3 - Periurbana  9 - Ignorado" com quadrado para resposta
     * 
     * @param fieldNumber Número do campo (ex: "29")
     * @param title Título do campo (ex: "Zona")
     * @param legendLines Lista de linhas da legenda (ex: ["1 - Urbana  2 - Rural", "3 - Periurbana  9 - Ignorado"])
     * @param answer Resposta a ser inserida no quadrado (pode ser null ou vazio)
     * @param widthPercentage Largura percentual da célula (para responsividade)
     * @return PdfPCell configurada
     */
    public static PdfPCell createFieldWithLegendAndAnswerBox(
            String fieldNumber, 
            String title, 
            java.util.List<String> legendLines, 
            String answer,
            float widthPercentage) {
        
        // Tabela principal que conterá todo o campo
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        
        // Tabela interna para o conteúdo (título + legenda | quadrado resposta)
        PdfPTable contentTable = new PdfPTable(new float[]{85f, 15f});
        contentTable.setWidthPercentage(100);
        
        // === LADO ESQUERDO: Título com número + Legendas ===
        PdfPTable leftSide = new PdfPTable(1);
        leftSide.setWidthPercentage(100);
        
        // Linha do título com número
        PdfPTable titleTable = new PdfPTable(new float[]{12f, 88f});
        titleTable.setWidthPercentage(100);
        
        // Célula do número (fundo preto)
        PdfPCell numberCell = new PdfPCell(new Phrase(fieldNumber, 
                new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, HEADER_TEXT_COLOR)));
        numberCell.setBackgroundColor(HEADER_BG_COLOR);
        numberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.setBorder(Rectangle.BOX);
        numberCell.setPadding(2f);
        titleTable.addCell(numberCell);
        
        // Célula do título
        PdfPCell titleCell = new PdfPCell(new Phrase(title, TITLE_FONT));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingLeft(3f);
        titleTable.addCell(titleCell);
        
        PdfPCell titleRowCell = new PdfPCell(titleTable);
        titleRowCell.setBorder(Rectangle.NO_BORDER);
        titleRowCell.setPadding(0);
        leftSide.addCell(titleRowCell);
        
        // Legendas abaixo do título
        for (String legendLine : legendLines) {
            PdfPCell legendCell = new PdfPCell(new Phrase(legendLine, LEGEND_FONT));
            legendCell.setBorder(Rectangle.NO_BORDER);
            legendCell.setPaddingLeft(15f);
            legendCell.setPaddingTop(1f);
            leftSide.addCell(legendCell);
        }
        
        PdfPCell leftCell = new PdfPCell(leftSide);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        contentTable.addCell(leftCell);
        
        // === LADO DIREITO: Quadrado de resposta ===
        PdfPTable rightSide = new PdfPTable(1);
        rightSide.setWidthPercentage(100);
        
        // Quadrado de resposta
        PdfPCell answerBox = new PdfPCell(new Phrase(answer != null ? answer : "", CONTENT_FONT));
        answerBox.setBorder(Rectangle.BOX);
        answerBox.setBorderWidth(1f);
        answerBox.setFixedHeight(18f);
        answerBox.setHorizontalAlignment(Element.ALIGN_CENTER);
        answerBox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        rightSide.addCell(answerBox);
        
        PdfPCell rightCell = new PdfPCell(rightSide);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setPaddingRight(5f);
        contentTable.addCell(rightCell);
        
        // Célula principal com borda
        PdfPCell mainCell = new PdfPCell(contentTable);
        mainCell.setBorder(Rectangle.BOX);
        mainCell.setBorderWidth(0.5f);
        mainCell.setPadding(3f);
        
        return mainCell;
    }

    /**
     * CAMPO TIPO 2: Campo descritivo simples que expande de acordo com o texto
     * Exemplo: Campo 32 (Ocupação) - Campo de texto livre que cresce verticalmente
     * 
     * @param fieldNumber Número do campo (ex: "32")
     * @param title Título do campo (ex: "Ocupação")
     * @param content Conteúdo/texto a ser exibido (pode ser longo e multilinha)
     * @param widthPercentage Largura percentual da célula (para responsividade)
     * @param minHeight Altura mínima do campo (0 para auto)
     * @return PdfPCell configurada
     */
    public static PdfPCell createDescriptiveField(
            String fieldNumber, 
            String title, 
            String content,
            float widthPercentage,
            float minHeight) {
        
        // Tabela principal
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        
        // === LINHA DO TÍTULO ===
        PdfPTable titleTable = new PdfPTable(new float[]{8f, 92f});
        titleTable.setWidthPercentage(100);
        
        // Célula do número (fundo preto)
        PdfPCell numberCell = new PdfPCell(new Phrase(fieldNumber, 
                new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, HEADER_TEXT_COLOR)));
        numberCell.setBackgroundColor(HEADER_BG_COLOR);
        numberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.setBorder(Rectangle.BOX);
        numberCell.setPadding(2f);
        titleTable.addCell(numberCell);
        
        // Célula do título
        PdfPCell titleCell = new PdfPCell(new Phrase(title, TITLE_FONT));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingLeft(3f);
        titleTable.addCell(titleCell);
        
        PdfPCell titleRowCell = new PdfPCell(titleTable);
        titleRowCell.setBorder(Rectangle.NO_BORDER);
        titleRowCell.setPadding(0);
        mainTable.addCell(titleRowCell);
        
        // === ÁREA DE CONTEÚDO (expande automaticamente) ===
        PdfPCell contentCell = new PdfPCell(new Phrase(content != null ? content : "", CONTENT_FONT));
        contentCell.setBorder(Rectangle.NO_BORDER);
        contentCell.setPaddingTop(5f);
        contentCell.setPaddingLeft(5f);
        contentCell.setPaddingRight(5f);
        contentCell.setPaddingBottom(5f);
        // Permite expansão automática - não define altura fixa
        mainTable.addCell(contentCell);
        
        // Célula principal com borda
        PdfPCell mainCell = new PdfPCell(mainTable);
        mainCell.setBorder(Rectangle.BOX);
        mainCell.setBorderWidth(0.5f);
        mainCell.setPadding(3f);
        
        if (minHeight > 0) {
            mainCell.setMinimumHeight(minHeight);
        }
        
        return mainCell;
    }

    /**
     * CAMPO TIPO 3: Campo de múltiplas opções com checkboxes em grid
     * Exemplo: Campo 33 (Sinais e Sintomas) - Grid de opções com quadradinhos para marcar
     * 
     * @param fieldNumber Número do campo (ex: "33")
     * @param title Título do campo (ex: "Sinais e Sintomas")
     * @param legend Legenda ao lado do título (ex: "1 - Sim  2 - Não  9 - Ignorado")
     * @param options Lista de opções a serem exibidas
     * @param answers Lista de respostas correspondentes às opções (mesmo índice)
     * @param columns Número de colunas para o grid de opções
     * @param widthPercentage Largura percentual da célula (para responsividade)
     * @param hasOthersField Se true, adiciona campo "Outros:" no final
     * @param othersValue Valor do campo "Outros" (se houver)
     * @return PdfPCell configurada
     */
    public static PdfPCell createMultipleOptionsField(
            String fieldNumber, 
            String title, 
            String legend,
            java.util.List<String> options,
            java.util.List<String> answers,
            int columns,
            float widthPercentage,
            boolean hasOthersField,
            String othersValue) {
        
        // Tabela principal
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        
        // === LINHA DO TÍTULO COM LEGENDA ===
        PdfPTable titleTable = new PdfPTable(new float[]{6f, 20f, 74f});
        titleTable.setWidthPercentage(100);
        
        // Célula do número (fundo preto)
        PdfPCell numberCell = new PdfPCell(new Phrase(fieldNumber, 
                new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, HEADER_TEXT_COLOR)));
        numberCell.setBackgroundColor(HEADER_BG_COLOR);
        numberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.setBorder(Rectangle.BOX);
        numberCell.setPadding(2f);
        titleTable.addCell(numberCell);
        
        // Célula do título
        PdfPCell titleCell = new PdfPCell(new Phrase(title, TITLE_FONT));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingLeft(3f);
        titleTable.addCell(titleCell);
        
        // Célula da legenda
        PdfPCell legendCell = new PdfPCell(new Phrase(legend, LEGEND_FONT));
        legendCell.setBorder(Rectangle.NO_BORDER);
        legendCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        legendCell.setPaddingLeft(10f);
        titleTable.addCell(legendCell);
        
        PdfPCell titleRowCell = new PdfPCell(titleTable);
        titleRowCell.setBorder(Rectangle.NO_BORDER);
        titleRowCell.setPaddingBottom(5f);
        mainTable.addCell(titleRowCell);
        
        // === GRID DE OPÇÕES ===
        // Calcula larguras iguais para todas as colunas
        float[] columnWidths = new float[columns];
        for (int i = 0; i < columns; i++) {
            columnWidths[i] = 100f / columns;
        }
        
        PdfPTable optionsGrid = new PdfPTable(columnWidths);
        optionsGrid.setWidthPercentage(100);
        
        int totalOptions = options.size();
        int optionIndex = 0;
        
        // Preenche o grid com as opções
        while (optionIndex < totalOptions) {
            String optionText = options.get(optionIndex);
            String answerValue = (answers != null && optionIndex < answers.size()) 
                    ? answers.get(optionIndex) : "";
            
            PdfPCell optionCell = createOptionWithCheckbox(optionText, answerValue);
            optionsGrid.addCell(optionCell);
            optionIndex++;
        }
        
        // Preenche células vazias para completar a última linha
        int remainder = totalOptions % columns;
        if (remainder > 0) {
            for (int i = 0; i < (columns - remainder); i++) {
                PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                emptyCell.setBorder(Rectangle.NO_BORDER);
                optionsGrid.addCell(emptyCell);
            }
        }
        
        PdfPCell gridCell = new PdfPCell(optionsGrid);
        gridCell.setBorder(Rectangle.NO_BORDER);
        gridCell.setPadding(0);
        mainTable.addCell(gridCell);
        
        // === CAMPO "OUTROS" (opcional) ===
        if (hasOthersField) {
            PdfPTable othersTable = new PdfPTable(new float[]{5f, 10f, 85f});
            othersTable.setWidthPercentage(100);
            
            // Checkbox para "Outros"
            PdfPCell othersCheckbox = createCheckboxCell("");
            othersTable.addCell(othersCheckbox);
            
            // Label "Outros:"
            PdfPCell othersLabel = new PdfPCell(new Phrase("Outros:", LEGEND_FONT));
            othersLabel.setBorder(Rectangle.NO_BORDER);
            othersLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            othersTable.addCell(othersLabel);
            
            // Linha para preenchimento
            PdfPCell othersLine = new PdfPCell(new Phrase(othersValue != null ? othersValue : "", CONTENT_FONT));
            othersLine.setBorder(Rectangle.BOTTOM);
            othersLine.setBorderWidth(0.5f);
            othersLine.setVerticalAlignment(Element.ALIGN_BOTTOM);
            othersLine.setPaddingBottom(2f);
            othersTable.addCell(othersLine);
            
            PdfPCell othersRowCell = new PdfPCell(othersTable);
            othersRowCell.setBorder(Rectangle.NO_BORDER);
            othersRowCell.setPaddingTop(5f);
            mainTable.addCell(othersRowCell);
        }
        
        // Célula principal com borda
        PdfPCell mainCell = new PdfPCell(mainTable);
        mainCell.setBorder(Rectangle.BOX);
        mainCell.setBorderWidth(0.5f);
        mainCell.setPadding(5f);
        
        return mainCell;
    }

    /**
     * Cria uma célula com checkbox e texto da opção
     */
    private static PdfPCell createOptionWithCheckbox(String optionText, String answer) {
        PdfPTable optionTable = new PdfPTable(new float[]{12f, 88f});
        optionTable.setWidthPercentage(100);
        
        // Checkbox
        PdfPCell checkboxCell = createCheckboxCell(answer);
        optionTable.addCell(checkboxCell);
        
        // Texto da opção
        PdfPCell textCell = new PdfPCell(new Phrase(optionText, LEGEND_FONT));
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setPaddingLeft(3f);
        optionTable.addCell(textCell);
        
        PdfPCell cell = new PdfPCell(optionTable);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2f);
        
        return cell;
    }

    /**
     * Cria uma célula de checkbox quadrado
     */
    private static PdfPCell createCheckboxCell(String value) {
        PdfPCell checkbox = new PdfPCell(new Phrase(value != null ? value : "", 
                new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL)));
        checkbox.setBorder(Rectangle.BOX);
        checkbox.setBorderWidth(0.5f);
        checkbox.setFixedHeight(12f);
        checkbox.setHorizontalAlignment(Element.ALIGN_CENTER);
        checkbox.setVerticalAlignment(Element.ALIGN_MIDDLE);
        // Define largura fixa através de padding
        checkbox.setPaddingLeft(2f);
        checkbox.setPaddingRight(2f);
        return checkbox;
    }

    /**
     * Método auxiliar para criar uma linha com múltiplos campos lado a lado (responsivo)
     * 
     * @param cells Array de células a serem colocadas lado a lado
     * @param widths Array de larguras percentuais para cada célula
     * @return PdfPTable contendo os campos lado a lado
     */
    public static PdfPTable createResponsiveRow(PdfPCell[] cells, float[] widths) {
        if (cells.length != widths.length) {
            throw new IllegalArgumentException("O número de células deve ser igual ao número de larguras");
        }
        
        PdfPTable rowTable = new PdfPTable(widths);
        rowTable.setWidthPercentage(100);
        
        for (PdfPCell cell : cells) {
            // Remove a borda externa para evitar bordas duplas
            cell.setBorder(Rectangle.BOX);
            rowTable.addCell(cell);
        }
        
        return rowTable;
    }
}
