package hse.t.cardrecognizer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardInfoParser {

    private double garbageAccuracy = 0.75;
    private double paymentSystemAccuracy = 0.65;
    private double bankLogoAccuracy = 0.65;
    private String[] garbageWords =  {"valid", "thru", "cardholder", "name", "card"};
    private Map<String, CardInfo.PaymentSystem> paymentSytemWords = new HashMap<>();
    private Map<String, Bitmap> bankLogos = new HashMap<>();

    public CardInfoParser(Resources resources) {

        // Fill payment systems
        paymentSytemWords.put("visa", CardInfo.PaymentSystem.VISA);
        paymentSytemWords.put("master", CardInfo.PaymentSystem.MASTERCARD);
        paymentSytemWords.put("maestro", CardInfo.PaymentSystem.MAESTRO);

        // Fill bank logos
        bankLogos.put("alfa", BitmapFactory.decodeResource(resources, R.drawable.alfa));
        bankLogos.put("cbepeank", BitmapFactory.decodeResource(resources, R.drawable.sberbank));
        bankLogos.put("raiffeisen", BitmapFactory.decodeResource(resources, R.drawable.raiffeisen));
        bankLogos.put("otkr poket", BitmapFactory.decodeResource(resources, R.drawable.otkrytie));
        bankLogos.put("tinkoff", BitmapFactory.decodeResource(resources, R.drawable.tinkoff));
    }

    public CardInfo parse(String text) {
        text = filterGarbage(text.toLowerCase());

        String cardNumber = getCardNumber(text);
        text = removeWord(cardNumber, text);

        String cardBankNumber = getCardBankNumber(text);
        text = removeWord(cardNumber, text);

        String cardExpirationDate = getExpirationDate(text);
        text = removeWord(cardExpirationDate, text);

        String cardHolder = getCardHolder(text);

        Bitmap bankLogo = getBankLogo(text);

        CardInfo.PaymentSystem paymentSystem = getPaymentSystem(text);

        return new CardInfo(cardNumber, cardBankNumber, cardHolder, cardExpirationDate, paymentSystem, bankLogo);
    }

    private String[] getWords(String rawText) {
        return rawText.replace("\n", " ").split(" ");
    }

    private String removeWord(String word, String text) {
        return text.replaceAll(word, "");
    }

    private String filterGarbage(String rawText) {
        String[] words = getWords(rawText);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            for (int j = 0; j < garbageWords.length; j++) {
                String garbageWord = garbageWords[j];
                double similarity = StringSimilarity.similarity(word, garbageWord);

                if (similarity >= garbageAccuracy) {
                    rawText = rawText.replaceAll(word, "");
                    continue;
                }
            }
        }

        return rawText;
    }

    private  String getCardNumber(String rawText) {
        String text = rawText.replace(" ", "");

        Pattern pattern = Pattern.compile("\\d{8,10}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private String getCardBankNumber(String rawText) {
        String text = rawText.replace(" ", "");

        Pattern pattern = Pattern.compile("\\d{13,16}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private String getExpirationDate(String rawText) {

        Pattern pattern = Pattern.compile("[0-1]?[0-9]/[0-9]{2,4}");
        Matcher matcher = pattern.matcher(rawText);

        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private CardInfo.PaymentSystem getPaymentSystem(String rawText) {
        String[] words = getWords(rawText);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            for (String payWord : paymentSytemWords.keySet()) {
                double similarity = StringSimilarity.similarity(word, payWord);

                if (similarity >= paymentSystemAccuracy) {
                    return paymentSytemWords.get(payWord);
                }
            }
        }

        return CardInfo.PaymentSystem.UNDEFINED;
    }

    private String getCardHolder(String rawText) {
        String[] lines = rawText.split("\n");
        Pattern pattern = Pattern.compile("\\b[a-zA-Z]{4,8}\\s\\b[a-zA-Z]{4,8}");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                return capitalize(matcher.group());
            }
        }

        return "";
    }

    private Bitmap getBankLogo(String rawText) {
        String[] words = getWords(rawText);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            for (String bankWord : bankLogos.keySet()) {
                double similarity = StringSimilarity.similarity(word, bankWord);

                if (similarity >= bankLogoAccuracy) {
                    return bankLogos.get(bankWord);
                }
            }
        }

        return null;
    }

    public String capitalize(String string) {
        String[] words = string.split(" ");
        String[] capitalizedWords = new String[words.length];

        for (int i = 0; i < words.length; i++) {
            capitalizedWords[i] = words[i].substring(0,1).toUpperCase() + words[i].substring(1).toLowerCase();
        }

        return TextUtils.join(" ", capitalizedWords);
    }

}
