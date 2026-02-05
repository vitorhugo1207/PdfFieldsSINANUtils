# PdfFieldsSisanUtils

This repository has two branches:

- `itext-5`: implementation using iText 5
- `itext-8+`: implementation using iText 8+ (kernel/layout API)

## What is this?

Utility helpers to build SINAN (Sistema de Informação de Agravos de Notificação) PDF fields (number box, legends, option grids, etc.).

## Dependencies

- Java (JDK)
- Gradle (via the included Gradle Wrapper)
- iText (configured as a Gradle dependency)

## How to run

From the project root:

```powershell
./gradlew.bat clean build
./gradlew.bat run
```

The example app generates `relatorio_exemplo.pdf` in the project root.

## How to use in your project

Copy `src/main/java/com/vitorhugo1207/pdffieldssisanutils/PdfFieldUtils.java` into your project and add the corresponding iText dependency in your build.