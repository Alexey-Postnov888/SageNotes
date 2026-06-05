package ru.sagenotes.ocrservice.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.jspecify.annotations.NonNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class OCRProcessor {

    private static final List<String> VALID_SHORT_WORDS = Arrays.asList(
            "а", "и", "в", "с", "у", "о", "я", "к", "б", "не", "он", "мы", "вы", "то", "та", "те", "ти", "ли", "бы", "же", "из", "до", "от", "по", "со", "за", "да", "ну", "ни", "но", "что",
            "a", "i", "me", "my", "it", "is", "in", "on", "at", "to", "no", "an", "am", "he", "do", "go", "so", "if", "by", "us", "we", "of", "or", "be", "up",
            "à", "le", "la", "en", "un", "et", "du", "se", "je", "ne", "pas", "ce", "ci", "ca", "ou", "au", "il", "on", "tu", "re"
    );

    public String extractTextFromImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            log.error("Исходный файл изображения не существует.");
            return "";
        }

        File processedImage;
        ImageProcessor.ImageType detectedType = ImageProcessor.ImageType.MIXED;

        try {
            ImageProcessor imageProcessor = new ImageProcessor();
            ImageProcessor.ImageProcessingResult resultObj = imageProcessor.ImageMake(imageFile);
            processedImage = resultObj.file();
            detectedType = resultObj.imageType();
        } catch (Exception e) {
            log.error("Ошибка на этапе OpenCV обработки: {}", e.getMessage(), e);
            processedImage = imageFile;
        }

        File convertedImageFile = ConvertFileFormat.convertToCommonFormat(processedImage);
        if (convertedImageFile == null) {
            convertedImageFile = processedImage;
        }

        List<String> languagesToTest = Arrays.asList("rus", "eng", "fra");

        String bestText = "";
        double bestScore = -1.0;

        for (String lang : languagesToTest) {
            String rawText = runOcrForLanguage(convertedImageFile, lang, detectedType);
            String cleanedText = cleanExtractedText(rawText, lang);
            double score = calculateResultScore(cleanedText, lang);

            if (score > bestScore) {
                bestScore = score;
                bestText = cleanedText;
            }
        }

        convertedImageFile.delete();
        return bestText;
    }

    private String runOcrForLanguage(File file, String lang, ImageProcessor.ImageType detectedType) {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage(lang);
        tesseract.setOcrEngineMode(1);

        tesseract.setVariable("user_defined_dpi", "300");
        tesseract.setVariable("textord_spacesize_is_fraction", "1");
        tesseract.setVariable("textord_min_linesize", "1.2");
        tesseract.setVariable("tessedit_char_blacklist", "[]{}<>|\\~_—–•§±¶°+=^`«»“”");

        tesseract.setVariable("load_system_dawg", "1");
        tesseract.setVariable("load_freq_dawg", "1");
        tesseract.setVariable("load_punc_dawg", "1");
        tesseract.setVariable("language_model_penalty_non_dict_word", "0.90");

        if (detectedType == ImageProcessor.ImageType.SHORT_TEXT) {
            tesseract.setPageSegMode(11);
        } else if (detectedType == ImageProcessor.ImageType.MIXED) {
            tesseract.setPageSegMode(1);
        } else {
            tesseract.setPageSegMode(3);
        }

        try {
            String result = tesseract.doOCR(file);
            if (result == null || result.trim().isEmpty()) {
                tesseract.setPageSegMode(6);
                result = tesseract.doOCR(file);
            }
            if (result == null || result.trim().isEmpty()) {
                File invertedFile = invertImage(file);
                if (invertedFile != null) {
                    tesseract.setPageSegMode(3);
                    result = tesseract.doOCR(invertedFile);
                    invertedFile.delete();
                }
            }
            return result != null ? result : "";
        } catch (TesseractException e) {
            log.error("Ошибка Tesseract при обработке языка {}: {}", lang, e.getMessage());
            return "";
        }
    }

    private double calculateResultScore(String text, String lang) {
        if (text == null || text.trim().isEmpty()) return 0.0;

        String[] words = text.toLowerCase().split("\\s+");
        int totalWords = words.length;
        if (totalWords == 0) return 0.0;

        double matchedStopWordsWeight = 0.0;
        int validLengthWords = 0;
        int totalLetters = 0;
        int targetScriptLetters = 0;
        int gibberishWords = 0;

        List<String> targetStopWords = getStrings(lang);

        for (String word : words) {
            String cleanWord = word.replaceAll("[\\p{Punct}„«»'‘`’_-]", "");
            if (cleanWord.isEmpty()) continue;

            if (targetStopWords.contains(cleanWord)) {
                matchedStopWordsWeight += (cleanWord.length() == 1) ? 0.5 : 2.5;
            }

            if (cleanWord.length() > 3) {
                validLengthWords++;
            }

            if (cleanWord.matches(".*[цкнгшщзхфвпрлджчсмтьб]{4,}.*") || cleanWord.matches(".*[bcdfghjklmnpqrstvwxyz]{5,}.*")) {
                gibberishWords++;
            }

            for (char c : cleanWord.toCharArray()) {
                if (Character.isLetter(c)) {
                    totalLetters++;
                    if ("rus".equals(lang) && isCyrillic(c)) {
                        targetScriptLetters++;
                    } else if (("eng".equals(lang) || "fra".equals(lang)) && isLatin(c)) {
                        targetScriptLetters++;
                    }
                }
            }
        }

        if (totalLetters == 0) return 0.0;

        double purity = (double) targetScriptLetters / totalLetters;
        double dictionaryFactor = 1.0 + matchedStopWordsWeight;

        double shortWordPenalty = 1.0;
        if (totalWords > 2 && (double) (totalWords - validLengthWords) / totalWords > 0.65) {
            shortWordPenalty = 0.2;
        }

        double gibberishPenalty = 1.0 - ((double) gibberishWords / totalWords);
        if (gibberishPenalty < 0.1) gibberishPenalty = 0.1;

        double langSignificanceBonus = 1.0;
        if ("fra".equals(lang)) {
            long diacriticsCount = text.chars().filter(c -> "àéèùçâêîôûëïüÀÉÈÙÇÂÊÎÔÛËÏÜ".indexOf(c) >= 0).count();
            if (diacriticsCount > 0) {
                langSignificanceBonus = 1.2;
            } else {
                langSignificanceBonus = 0.4;
            }
        }

        return targetScriptLetters * (purity * purity) * dictionaryFactor * shortWordPenalty * gibberishPenalty * langSignificanceBonus;
    }

    private static @NonNull List<String> getStrings(String lang) {
        List<String> rusStopWords = Arrays.asList(
                "и", "в", "не", "на", "я", "что", "тот", "с", "а", "это", "как", "она", "по", "но", "они", "к", "у", "мы", "за", "вы", "только", "мне", "было", "вот", "для"
        );
        List<String> engStopWords = Arrays.asList(
                "the", "and", "of", "to", "is", "in", "it", "you", "that", "he", "was", "for", "on", "are", "as", "with", "his", "they", "i", "at", "be", "this", "have", "from", "my", "name", "family"
        );
        List<String> fraStopWords = Arrays.asList(
                "le", "la", "les", "des", "en", "et", "un", "une", "du", "que", "est", "dans", "pour", "qui", "par", "plus", "pas", "au", "aux", "sur"
        );

        return "rus".equals(lang) ? rusStopWords :
                "eng".equals(lang) ? engStopWords : fraStopWords;
    }

    private File invertImage(File inputFile) {
        try {
            BufferedImage image = ImageIO.read(inputFile);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int rgba = image.getRGB(x, y);
                    int alpha = (rgba >> 24) & 0xFF;
                    int red = 255 - ((rgba >> 16) & 0xFF);
                    int green = 255 - ((rgba >> 8) & 0xFF);
                    int blue = 255 - (rgba & 0xFF);
                    image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
                }
            }
            File outputFile = new File(inputFile.getParent(), "inverted_" + inputFile.getName());
            ImageIO.write(image, "png", outputFile);
            return outputFile;
        } catch (IOException e) {
            log.error("Не удалось инвертировать изображение: {}", e.getMessage());
            return null;
        }
    }

    private String cleanExtractedText(String text, String currentLang) {
        if (text == null) return "";

        String[] lines = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            String cleanedLine = cleanLineTokens(line, currentLang);

            if (cleanedLine.isEmpty() || isTrashLineStructure(cleanedLine)) {
                continue;
            }

            sb.append(cleanedLine).append("\n");
        }

        return sb.toString().trim();
    }

    private String cleanLineTokens(String line, String currentLang) {
        if (line == null || line.trim().isEmpty()) return "";

        String[] tokens = line.trim().split("\\s+");
        StringBuilder rebuiltLine = new StringBuilder();

        for (String token : tokens) {
            if (token.matches("\\d+%?")) {
                rebuiltLine.append(token).append(" ");
                continue;
            }

            String cleanToken = token.replaceAll("[\\p{Punct}„«»'‘`’_-]", "");
            if (cleanToken.isEmpty()) continue;
            String lowerToken = cleanToken.toLowerCase();

            int wordCyrillic = 0;
            int wordLatin = 0;
            for (char c : cleanToken.toCharArray()) {
                if (isCyrillic(c)) wordCyrillic++;
                else if (isLatin(c)) wordLatin++;
            }

            if (wordCyrillic > 0 && wordLatin > 0) {
                continue;
            }

            if (currentLang.equals("rus") && wordLatin > 0) {
                boolean isAbbreviation = cleanToken.equals(cleanToken.toUpperCase()) && cleanToken.length() >= 2;
                if (!VALID_SHORT_WORDS.contains(lowerToken) && !isAbbreviation) {
                    continue;
                }
            }

            if ((currentLang.equals("eng") || currentLang.equals("fra")) && wordCyrillic > 0) {
                if (!VALID_SHORT_WORDS.contains(lowerToken)) {
                    continue;
                }
            }

            if (cleanToken.matches("[a-zA-Zа-яА-ЯёЁàéèù§]+")) {
                if (cleanToken.length() <= 2 && !VALID_SHORT_WORDS.contains(lowerToken)) {
                    continue;
                }
                if (cleanToken.length() >= 2 && !lowerToken.matches(".*[aeiouyаеёиоуыэюяàéèù].*")) {
                    continue;
                }
            }

            rebuiltLine.append(token).append(" ");
        }

        return rebuiltLine.toString().trim();
    }

    private boolean isTrashLineStructure(String line) {
        String trimmedLine = line.replaceAll("^[,.\\s\\-|~`_+=!><«»\"'„*?/:;]+", "");
        trimmedLine = trimmedLine.replaceAll("[,.\\s\\-|~`_+=!><«»\"'„*?/:;]+$", "");

        String pureLetters = trimmedLine.replaceAll("[\\s\\p{Punct}„«»'‘`’]", "");
        if (pureLetters.isEmpty()) return true;

        double letterRatio = (double) pureLetters.length() / trimmedLine.length();
        return letterRatio < 0.60 && trimmedLine.length() < 25;
    }

    private boolean isCyrillic(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CYRILLIC;
    }

    private boolean isLatin(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.BASIC_LATIN || block == Character.UnicodeBlock.LATIN_1_SUPPLEMENT;
    }
}