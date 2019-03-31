package hse.t.cardrecognizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Button;
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

    private Button mButtonSave;

    private TextRecognizer detector;

    private CardInfoParser cardInfoParser;

    private static final float[] BLACK_AND_WHITE = new float[] {
            1.5F, 1.5F, 1.5F, 0, -255,
            1.5F, 1.5F, 1.5F, 0, -255,
            1.5F, 1.5F, 1.5F, 0, -255,
            0, 0, 0, 1, 0,
    };

    public static final float[] blackAndWhite() {
        return BLACK_AND_WHITE.clone();
    }

    private static final float[] RGB_TO_BGR = new float[] {
            0, 0, 1, 0, 0,
            0, 1, 0, 0, 0,
            1, 0, 0, 0, 0,
            0, 0, 0, 1, 0,
    };

    public static final float[] rgbToBgr() {
        return RGB_TO_BGR.clone();
    }

    private static final float[] COMMON = new float[] {
            1, 0, 0, 0, 0,
            0, 1, 0, 0, 0,
            0, 0, 1, 0, 0,
            0, 0, 0, 1, 0
    };

    public static final float[] common() {
        return COMMON.clone();
    }

    private int brightness = 20;
    private float contrast = 5;
    private float saturation = 5;

    public Bitmap addStyleToBitmap(Context context, Bitmap bitmap){

        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        context = context.getApplicationContext();

        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);

        drawable.setColorFilter(new ColorMatrixColorFilter(calculateMatrix(brightness, contrast, saturation)));
        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        drawable.draw(canvas);

        return newBitmap;

    }

    private static float[] calculateMatrix(int brightness, float contrast, float saturation) {
        return applyBrightnessAndContrast(getMatrixByMode(saturation), brightness, contrast);
    }

    private static float[] applyBrightnessAndContrast(float[] matrix, int brightness, float contrast) {
        float t = (1.0F - contrast) / 2.0F * 255.0F;
        for (int i = 0; i < 3; i++) {
            for (int j = i * 5; j < i * 5 + 3; j++) {
                matrix[j] *= contrast;
            }
            matrix[5 * i + 4] += t + brightness;
        }
        return matrix;
    }

    private static float[] getMatrixByMode(float saturation) {
        float[] targetMatrix = common();

        targetMatrix = blackAndWhite();//rgbToBgr();

        return targetMatrix;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        detector = new TextRecognizer.Builder(getApplicationContext()).build();

//        scanResults = (TextView) findViewById(R.id.results);

        cardInfoParser = new CardInfoParser(getResources());

//        mResultLabel = (TextView) findViewById(R.id.result);
//        mResultImage = (ImageView) findViewById(R.id.result_image);
//        mResultCardTypeImage = (ImageView) findViewById(R.id.result_card_type_image);

        mCardNumber = (TextView)findViewById(R.id.text_card_number);
        mExpiredDate = (TextView)findViewById(R.id.text_expired_date);
        mNameHolder = (TextView)findViewById(R.id.text_card_holder);
        mKindBank = (ImageView) findViewById(R.id.kind_bank);
        mKindCard = (ImageView) findViewById(R.id.kind_card);

        mButtonSave = (Button)findViewById(R.id.save_vary);

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent board = new Intent(MainActivity.this,ResponseActivity.class);
                startActivity(board);
            }
        });

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

                Bitmap arn = addStyleToBitmap(getApplicationContext(),card);

                Frame frame2 = new Frame.Builder().setBitmap(card).build();
                SparseArray<TextBlock> textBlocks2 = detector.detect(frame2);

//                mResultImage.setImageBitmap(card);
//                mResultCardTypeImage.setImageBitmap(cardTypeImage);

                String lines2 = "";
                for (int index = 0; index < textBlocks.size(); index++) {
                    //извлечение данных
                    TextBlock tBlock = textBlocks2.valueAt(index);

                    for (Text line : tBlock.getComponents()) {
                        lines2 = lines2 + line.getValue() + "\n";
                    }
                }

                Log.d("\nBlack:","\n"+lines2);


                CardInfo cardInfo = cardInfoParser.parse(lines);
                cardInfo.merge(result);

                outStr += "Номер карты: " + cardInfo.formatedBankCardNumber() + "\n";

                mCardNumber.setText(cardInfo.formatedBankCardNumber());

                CardType cardType = result.getCardType();

                outStr += "Тип карты: " + cardType.name() + "\n";

                if(cardInfo.cardPaymentSystem == CardInfo.PaymentSystem.VISA) mKindCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cio_ic_visa));
                else if(cardInfo.cardPaymentSystem == CardInfo.PaymentSystem.MASTERCARD) mKindCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cio_ic_mastercard));
                else if(cardInfo.cardPaymentSystem == CardInfo.PaymentSystem.MAESTRO) mKindCard.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.maestro));

                if (cardInfo.bankLogo != null) {
                    mKindBank.setImageBitmap(cardInfo.bankLogo);
                }

                outStr += "Срок действия: " + cardInfo.cardExpirationDate + "\n";
                mExpiredDate.setText(cardInfo.cardExpirationDate);

                outStr += "Имя владельца карты: " + cardInfo.cardHolder + "\n";
                mNameHolder.setText(cardInfo.cardHolder);

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
                onScan();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}