package com.vitorhugo1207.pdffieldssisanutils;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Main {        
    public static void main(String[] args) {
    try {
            // Criar documento
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream("relatorio_exemplo.pdf"));
            document.open();

            // =====================================================
            // EXEMPLO 1: Campos lado a lado (28, 29, 30)
            // =====================================================
            
            // Campo 28 - (DDD) Telefone (usando campo descritivo)
            PdfPCell campo28 = PdfFieldUtils.createDescriptiveField(
                "28", 
                "(DDD) Telefone", 
                "(11) 99999-9999",
                30f,
                0
            );

            // Campo 29 - Zona (com legenda e quadrado de resposta)
            List<String> legendaZona = Arrays.asList(
                "1 - Urbana    2 - Rural",
                "3 - Periurbana  9 - Ignorado"
            );
            PdfPCell campo29 = PdfFieldUtils.createFieldWithLegendAndAnswerBox(
                "29", 
                "Zona", 
                legendaZona,
                "1",  // Resposta: Urbana
                25f
            );

            // Campo 30 - País
            PdfPCell campo30 = PdfFieldUtils.createDescriptiveField(
                "30", 
                "País (se residente fora do Brasil)", 
                "",
                45f,
                0
            );

            // Criar linha responsiva com os 3 campos
            PdfPTable linhaComTresCampos = PdfFieldUtils.createResponsiveRow(
                new PdfPCell[]{campo28, campo29, campo30},
                new float[]{30f, 25f, 45f}
            );
            document.add(linhaComTresCampos);
            document.add(new Paragraph(" ")); // Espaçamento

            // =====================================================
            // EXEMPLO 2: Campo 32 - Ocupação (descritivo expandível)
            // =====================================================
            
            PdfPCell campo32 = PdfFieldUtils.createDescriptiveField(
                "32", 
                "Ocupação", 
                "Engenheiro de Software - Desenvolvedor Full Stack com experiência em " +
                "sistemas distribuídos, microserviços e arquiteturas cloud-native. " +
                "Especializado em Java, Spring Boot e tecnologias de containerização.",
                100f,
                40f  // Altura mínima
            );
            
            // Adiciona como linha única (100% largura)
            PdfPTable linhaCampo32 = PdfFieldUtils.createResponsiveRow(
                new PdfPCell[]{campo32},
                new float[]{100f}
            );
            document.add(linhaCampo32);
            document.add(new Paragraph(" ")); // Espaçamento

            // =====================================================
            // EXEMPLO 3: Campo 33 - Sinais e Sintomas (múltiplas opções)
            // =====================================================
            
            List<String> opcoesSintomas = Arrays.asList(
                "Febre",
                "Cefaléia",
                "Dor Abdominal",
                "Mialgia",
                "Náusea/Vômito",
                "Exantema",
                "Diarréia",
                "Icterícia",
                "Hiperemia Conjuntival",
                "Hepatomegalia/Esplenomegalia",
                "Petéquias",
                "Manifestações hemorrágicas",
                "Linfadenopatia",
                "Convulsão",
                "Necrose de extremidades",
                "Prostração",
                "Choque/Hipotensão",
                "Estupor/Coma",
                "Sufusão hemorrágica",
                "Alterações Respiratórias",
                "Oligúria/Anúria"
            );
            
            // Respostas (1 = Sim, 2 = Não, 9 = Ignorado, vazio = não preenchido)
            List<String> respostasSintomas = Arrays.asList(
                "1",  // Febre - Sim
                "2",  // Cefaléia - Não
                "1",  // Dor Abdominal - Sim
                "9",  // Mialgia - Ignorado
                "",   // Náusea/Vômito - não preenchido
                "2",  // Exantema - Não
                "1",  // Diarréia - Sim
                "2",  // Icterícia - Não
                "",   // Hiperemia Conjuntival
                "9",  // Hepatomegalia/Esplenomegalia
                "2",  // Petéquias
                "2",  // Manifestações hemorrágicas
                "",   // Linfadenopatia
                "2",  // Convulsão
                "2",  // Necrose de extremidades
                "1",  // Prostração
                "2",  // Choque/Hipotensão
                "2",  // Estupor/Coma
                "2",  // Sufusão hemorrágica
                "1",  // Alterações Respiratórias
                "2"   // Oligúria/Anúria
            );
            
            PdfPCell campo33 = PdfFieldUtils.createMultipleOptionsField(
                "33",
                "Sinais e Sintomas",
                "1 - Sim    2 - Não    9 - Ignorado",
                opcoesSintomas,
                respostasSintomas,
                4,      // 4 colunas
                100f,
                true,   // Tem campo "Outros"
                "Tosse seca persistente"  // Valor do campo outros
            );
            
            PdfPTable linhaCampo33 = PdfFieldUtils.createResponsiveRow(
                new PdfPCell[]{campo33},
                new float[]{100f}
            );
            document.add(linhaCampo33);
            document.add(new Paragraph(" ")); // Espaçamento

            // =====================================================
            // EXEMPLO 4: Dois campos de múltiplas opções lado a lado
            // =====================================================
            
            List<String> opcoesExame = Arrays.asList(
                "Hemograma",
                "Bioquímica",
                "Sorologia",
                "PCR",
                "Cultura",
                "Imagem"
            );
            
            List<String> respostasExame = Arrays.asList("1", "1", "2", "1", "9", "2");
            
            PdfPCell campoExames = PdfFieldUtils.createMultipleOptionsField(
                "34",
                "Exames Solicitados",
                "1 - Sim  2 - Não  9 - Ignorado",
                opcoesExame,
                respostasExame,
                2,      // 2 colunas
                50f,
                false,  // Sem campo "Outros"
                null
            );
            
            List<String> opcoesResultado = Arrays.asList(
                "Positivo",
                "Negativo",
                "Inconclusivo",
                "Aguardando"
            );
            
            List<String> respostasResultado = Arrays.asList("", "1", "", "");
            
            PdfPCell campoResultado = PdfFieldUtils.createMultipleOptionsField(
                "35",
                "Resultado",
                "1 - Marcar opção",
                opcoesResultado,
                respostasResultado,
                2,
                50f,
                false,
                null
            );
            
            // Criar linha com dois campos lado a lado
            PdfPTable linhaDoisCampos = PdfFieldUtils.createResponsiveRow(
                new PdfPCell[]{campoExames, campoResultado},
                new float[]{50f, 50f}
            );
            document.add(linhaDoisCampos);

            document.close();
            System.out.println("PDF gerado com sucesso: relatorio_exemplo.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
