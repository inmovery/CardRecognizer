package hse.t.cardrecognizer;

public class CardInfo {

    enum PaymentSystem {
        VISA, MASTERCARD, MAESTRO, UNDEFINED
    }

    public String cardNumber; // десятизначное число, возможно разделённое на группы по 4 и 6 цифр
    public String cardBankNumber; // шестандцатизначное число, возможно разделённое на группы по 4 цифры
    public String cardHolder;
    public String cardExpirationDate;
    public PaymentSystem cardPaymentSystem;
    //public BufferedImage logo;

    public CardInfo(String number, String bankNumber, String holder, String expirationDate, PaymentSystem paymentSystem) {
        cardNumber = number;
        cardBankNumber = bankNumber;
        cardHolder = holder;
        cardExpirationDate = expirationDate;
        cardPaymentSystem = paymentSystem;
    }
}
