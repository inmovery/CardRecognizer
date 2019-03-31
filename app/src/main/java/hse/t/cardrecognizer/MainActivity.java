package hse.t.cardrecognizer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CardType;
import io.card.payment.CreditCard;
import io.card.payment.i18n.StringKey;
import io.card.payment.i18n.SupportedLocale;
import io.card.payment.i18n.locales.LocalizedStringsList;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_SCAN = 100;
    private static final int REQUEST_AUTOTEST = 200;

    private CheckBox mSuppressConfirmationToggle;

    private TextView scanResults;

//    private TextView mResultLabel;
//    private ImageView mResultImage;
//    private ImageView mResultCardTypeImage;

    private TextView mCardNumber;
    private TextView mExpiredDate;
    private TextView mNameHolder;
    private ImageView mKindCard;
    private ImageView mKindBank;

    private TextRecognizer detector;

    private CardInfoParser cardInfoParser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        detector = new TextRecognizer.Builder(getApplicationContext()).build();

//        scanResults = (TextView) findViewById(R.id.results);

        cardInfoParser = new CardInfoParser();

//        mResultLabel = (TextView) findViewById(R.id.result);
//        mResultImage = (ImageView) findViewById(R.id.result_image);
//        mResultCardTypeImage = (ImageView) findViewById(R.id.result_card_type_image);

        mCardNumber = (TextView)findViewById(R.id.text_card_number);
        mExpiredDate = (TextView)findViewById(R.id.text_expired_date);
        mNameHolder = (TextView)findViewById(R.id.text_card_holder);
        mKindBank = (ImageView) findViewById(R.id.kind_bank);
        mKindCard = (ImageView) findViewById(R.id.kind_card);

        onScan();

    }

    public void onScan() {
        Intent intent = new Intent(this, CardIOActivity.class)
                .putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
                .putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, true)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, true)
                .putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false)
                .putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false)
                .putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, false)
                .putExtra(CardIOActivity.EXTRA_SUPPRESS_SCAN, false)
                .putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true)
                .putExtra(CardIOActivity.EXTRA_RETURN_CARD_IMAGE, true);

        startActivityForResult(intent, REQUEST_SCAN);
    }

    @Override
    public void onStop() {
        super.onStop();

//        mResultLabel.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");

        String outStr = new String();
        Bitmap cardTypeImage = null;

        if ((requestCode == REQUEST_SCAN || requestCode == REQUEST_AUTOTEST) && data != null
                && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard result = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
            if (result != null) {

                Bitmap card = CardIOActivity.getCapturedCardImage(data);

                Frame frame = new Frame.Builder().setBitmap(card).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);

//                mResultImage.setImageBitmap(card);
//                mResultCardTypeImage.setImageBitmap(cardTypeImage);

                String lines = "";
                for (int index = 0; index < textBlocks.size(); index++) {
                    //извлечение данных
                    TextBlock tBlock = textBlocks.valueAt(index);

                    for (Text line : tBlock.getComponents()) {
                        lines = lines + line.getValue() + "\n";
                    }
                }

//                scanResults.setText(lines + "\n");

                CardInfo cardInfo = cardInfoParser.parse(lines);
                cardInfo.merge(result);

                outStr += "Номер карты: " + cardInfo.cardBankNumber + "\n";

                mCardNumber.setText(cardInfo.cardBankNumber);

                CardType cardType = result.getCardType();
//                cardTypeImage = cardType.imageBitmap(this);

                outStr += "Тип карты: " + cardType.name() + "\n";

                if(cardType.name() == "VISA") mKindCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cio_ic_visa));
                else if(cardType.name() == "MasterCard") mKindCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cio_ic_mastercard));
                //else if(cardType.name() == "Maestro") mKindCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.maestro));

                //mKindBank.setImageBitmap();

                outStr += "Срок действия: " + cardInfo.cardExpirationDate + "\n";
                mExpiredDate.setText(cardInfo.cardExpirationDate);

                outStr += "Имя владельца карты: " + cardInfo.cardHolder + "\n";
                //...

                Log.i(TAG, "\nCard.io: \n" + outStr);

                Log.d("\nVision:","\n"+lines);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                //обновление
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}