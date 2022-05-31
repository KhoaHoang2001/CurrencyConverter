package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String mFeedTitle;
    String mFeedDescription;

    String finalURL = "https://usd.fxexchangerate.com/rss.xml";

    ArrayList<ConvertType> convertTypes = new ArrayList<>();

    EditText editText1;
    EditText editText2;
    TextView fromCurrency;
    TextView toCurrency;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        fromCurrency = findViewById(R.id.from_currency);
        toCurrency = findViewById(R.id.to_currency);
        spinner = findViewById(R.id.spinner);
        new FetchFeedTask().execute((Void) null);

        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                convert();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            urlLink = finalURL;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://")) {
                    urlLink = "http://" + urlLink;
                }
                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                convertTypes = parseFeed(inputStream);
                return true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {
                ArrayAdapter<ConvertType> adapter = new ArrayAdapter<ConvertType>(getApplicationContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, convertTypes);
                adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                adapter.notifyDataSetChanged();
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){



                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String[] currency = convertTypes.get(i).getTitle().split("/");
                        fromCurrency.setText(currency[0]);
                        toCurrency.setText(currency[1]);
                        convert();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        fromCurrency.setText("N/A");
                        toCurrency.setText("N/A");
                    }
                });
            }
            else {

            }
        }
    }

    public ArrayList<ConvertType> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String description = null;
        boolean isItem = false;
        ArrayList<ConvertType> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title") && isItem) {
                    title = result;
                } else if (name.equalsIgnoreCase("description") && isItem) {
                    description = result;
                }

                if (title != null && description != null) {
                    if(isItem) {
                        ConvertType item = new ConvertType(title, description);
                        items.add(item);
                    }
                    else {
                        mFeedTitle = title;
                        mFeedDescription = description;
                    }

                    title = null;
                    description = null;
                    isItem = false;
                }
            }

            return items;
        }
        finally {
            inputStream.close();
        }
    }

    public void convert(){
        try {

            ConvertType convertType = (ConvertType) spinner.getSelectedItem();
            Double amount = Double.parseDouble(editText1.getText().toString());
            Double rate = rate = Double.parseDouble(convertType.getDescription().split(" ")[5]);
            Double amountConverted = amount * rate;
            editText2.setText(amountConverted.toString());
        } catch (NumberFormatException e) {
            editText2.setText("");
        }
    }

}