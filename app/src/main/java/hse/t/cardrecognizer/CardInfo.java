package hse.t.cardrecognizer;

import android.graphics.Bitmap;

import io.card.payment.CardType;
import io.card.payment.CreditCard;

public class CardInfo {

    enum PaymentSystem {
        VISA, MASTERCARD, MAESTRO, UNDEFINED
    }

    public String cardNumber; // десятизначное число, возможно разделённое на группы по 4 и 6 цифр
    public String cardBankNumber; // шестандцатизначное число, возможно разделённое на группы по 4 цифры
    public String cardHolder;
    public String cardExpirationDate;
    public PaymentSystem cardPaymentSystem;
    public Bitmap bankLogo;
    //public BufferedImage logo;

    public CardInfo(String number, String bankNumber, String holder, String expirationDate, PaymentSystem paymentSystem, Bitmap logo) {
        cardNumber = number;
        cardBankNumber = bankNumber;
        cardHolder = holder;
        cardExpirationDate = expirationDate;
        cardPaymentSystem = paymentSystem;
        bankLogo = logo;
    }

    public Boolean isFuelCard() {
        return ((cardNumber.length() > 8) && cardBankNumber.isEmpty());
    }

    public void merge(CreditCard card) {

        // Если cards.io засканили номер, то берём у них.
        if (card.cardNumber != null) {
            if (!card.cardNumber.isEmpty()) {
                cardBankNumber = card.cardNumber;
            }
        }

        if ((card.expiryMonth != 0) && (card.expiryYear != 0)) {
            String incomeDate = card.expiryMonth + "/" + card.expiryYear;
            if ((!incomeDate.isEmpty()) && (cardExpirationDate.isEmpty())) {
                cardExpirationDate = incomeDate;
            }
        }

        if (card.cardholderName != null) {
            String incomeHolder = card.cardholderName;
            if ((!incomeHolder.isEmpty()) && (cardHolder.isEmpty())) {
                cardHolder = incomeHolder;
            }
        }

        if ((cardPaymentSystem == PaymentSystem.UNDEFINED) && (card.getCardType() != CardType.UNKNOWN)) {
            switch (card.getCardType()) {
                case VISA:
                    cardPaymentSystem = PaymentSystem.VISA;
                case MASTERCARD:
                    cardPaymentSystem = PaymentSystem.MASTERCARD;
                case MAESTRO:
                    cardPaymentSystem = PaymentSystem.MAESTRO;
            }
        }
    }
}
