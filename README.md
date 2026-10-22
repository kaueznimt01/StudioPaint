# StudioPaint Android

Primeira versão inicial de um editor gráfico Android, feito com Kotlin e Jetpack Compose.

## Recursos da versão 0.1.0

- Área de desenho com toque
- Pincel com cor e tamanho ajustáveis
- Desfazer/refazer
- Camadas básicas de desenho
- Limpar a camada ativa
- Exportação PNG para `Pictures/StudioPaint`

## Gerar o APK pelo GitHub usando um celular

1. Crie um repositório no GitHub chamado `StudioPaint` ou `artstudio-android`.
2. Envie todos os arquivos e pastas deste projeto para a raiz do repositório.
3. Abra a aba **Actions**.
4. Se for solicitado, habilite GitHub Actions.
5. Selecione **Build StudioPaint APK** e toque em **Run workflow**. Um push para `main` também inicia a compilação.
6. Abra a execução concluída e baixe o artefato `StudioPaint-debug-apk`.
7. Extraia o ZIP do artefato para obter `app-debug.apk` e instale no Android.

## Compilar localmente

Requer JDK 17 e Gradle 8.9. Na raiz do projeto:

```bash
gradle assembleDebug
```

APK gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Observações

- Esta é uma versão inicial para teste, não um substituto completo de Ibis Paint X ou Photoshop Touch.
- O PNG exportado tem fundo branco e tamanho de 1200 × 1600 px; esta primeira versão não preserva transparência.
- O formato editável do projeto, importação de imagens, texto como objeto, filtros e persistência de projetos ainda não estão implementados.
- A compilação usa uma versão debug, adequada para teste. Uma versão de lançamento exige configuração de assinatura.
- O workflow usa GitHub Actions. A disponibilidade e os limites de minutos dependem do plano e das configurações do repositório.
