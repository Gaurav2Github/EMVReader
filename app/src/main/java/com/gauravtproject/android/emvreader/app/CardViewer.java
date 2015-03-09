package com.gauravtproject.android.emvreader.app;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gauravtproject.android.emvreader.app.emvtools.EMVReader;

import java.io.IOException;


public class CardViewer extends ActionBarActivity {

    private final String LOG_TAG = CardViewer.class.getSimpleName();
    private NfcAdapter mAdapter;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_viewer);

        mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();

        //gets the default NFC Adapter, or null if no NFC adapter exists.
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mAdapter == null ){
            showMessage(R.string.error, R.string.no_nfc);
            finish();
            return;
        }
        Log.i(LOG_TAG, "onCreate executed..");
    }


    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }


    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if(this.mAdapter!=null){
            this.mAdapter.disableForegroundDispatch(this);
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "onResume() executed..");
        super.onResume();

        if(this.mAdapter!=null){

            if(!this.mAdapter.isEnabled()){
                this.showWirelessSettingsDialog();
            }

            mAdapter.enableForegroundDispatch(this,
                    PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0),
                    new IntentFilter[] {
                            new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                    },
                    new String[][] {
                            new String[]
                                    {
                                            IsoDep.class.getName()
                                    }
                    });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_viewer, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent!=null && intent.getAction()!=null){

            //Check for Intent to start an activity when a tag is discovered.
            if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){

                //Retrieve extended data from the intent to Tag.
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                //Acquire an instance of ISO-DEP (ISO 14443-4) object for the given tag.
                IsoDep isoDep = IsoDep.get(tag);

                if(isoDep == null){
                    //tag does not support Iso Dep interface.
                    //DO nothing.
                    Log.i(LOG_TAG, "tag does not support Iso Dep interface");
                    return;
                }

                //Creating CardHandler object asynchronously.
                new CardHandler().execute(isoDep);

                Log.i(LOG_TAG, "onNewIntent(Intent intent) executed..");
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * If nfc is disabled for the phone, This method will
     * display AlertDialog message to enable it.
     */
    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent((Settings.ACTION_NFC_SETTINGS));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();

        Log.i(LOG_TAG, "showWirelessSettingsDialog() executed ...");
        return;

    }


    /**
     * CardHandler class to generate card details.
     */
    protected class CardHandler extends AsyncTask<IsoDep, Integer, CardHandler> implements EMVReader.CardReader{

        private String LOG_TAG = CardHandler.class.getSimpleName();
        private byte[] adfInfo;
        private IsoDep isoDep;

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected CardHandler doInBackground(IsoDep... params) {
            Log.i(LOG_TAG, "doInBackground()..executing...");
            try{

                isoDep = params[0];
                isoDep.connect();
                byte[] response = isoDep.transceive(EMVReader.SELECT_PPSE);

                if(response != null){

                    if ((response.length == 2) && (response[0] == (byte) 0x61))
                    {
                        byte[] getData = new byte[]
                                {
                                        0x00, (byte) 0xC0, 0x00, 0x00, response[1]
                                };

                        response = isoDep.transceive(getData);
                        if (response != null)
                        {
                        if ((response.length >= 2)
                                && (response[response.length - 2] == (byte) 0x90)
                                && (response[response.length - 1] == (byte) 0x00))
                            {
                                CardHandler card = new CardHandler(response);
                                Log.i(LOG_TAG, "doInBackground()..returns CardHandler object...");
                                return card;
                            }
                        }
                    }
                }
            }
            catch (Exception ex){
                Log.e(LOG_TAG, "Error: " + ex.getMessage() );
            }
            Log.i(LOG_TAG, "doInBackground()..returns null...");
            return null;
        }


        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param card The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(CardHandler card) {
            Log.i(LOG_TAG, "onPostExecute()..executing...");
            if(card !=null) {
                try{
                    EMVReader reader = new EMVReader(card,null /*EMVReader.AID_PPSE*/,card.getAdfInfo());
                    reader.doTrace=true;
                    reader.read();

                    //PAN number
                    TextView cardPanTextView = (TextView) findViewById(R.id.card_pan_textView);
                    if(reader.pan!=null){
                        cardPanTextView.setText(getString(R.string.card_pan_title) + reader.pan);
                    }
                    else
                    {
                        cardPanTextView.setText(getString(R.string.card_pan_title) + getString(R.string.not_available));
                    }

                    //Expiry Month
                    TextView cardExpiryMonthTextView = (TextView) findViewById(R.id.card_expiry_month_textView);
                    if(reader.expiryMonth !=null){
                        cardExpiryMonthTextView.setText(getString(R.string.expiry_month_title) + reader.expiryMonth);
                    }
                    else
                    {
                        cardExpiryMonthTextView.setText(getString(R.string.expiry_month_title) + getString(R.string.not_available));
                    }

                    //Expiry Year
                    TextView cardExpiryYearTextView = (TextView)findViewById(R.id.card_expiry_yr_textView);
                    if(reader.expiryYear  != null){
                        cardExpiryYearTextView.setText(getString(R.string.expiry_yr_title) + reader.expiryYear);
                    }
                    else
                    {
                        cardExpiryYearTextView.setText(getString(R.string.expiry_yr_title) + getString(R.string.not_available));
                    }

                    //Issuer
                    TextView cardIssuerTextView = (TextView) findViewById(R.id.card_issuer_textView);
                    if(reader.issuer!=null){
                        cardIssuerTextView.setText(getString(R.string.card_issuer_title) + reader.issuer);
                    }
                    else
                    {
                        cardIssuerTextView.setText(getString(R.string.card_issuer_title) + getString(R.string.not_available));
                    }

                }
                catch (Exception ex){
                    Log.e(LOG_TAG, "Error: " + ex.getMessage());
                }
            }
        }

        @Override
        public byte[] transceive(byte[] apdu) throws IOException {
            Log.i(LOG_TAG, "inside : transceive().. executed..");
            return new byte[0];
        }

        /**
         * public CardHandler default constructor.
         */
        public CardHandler(){
        }

        /**
         * private CardHandler constructor with parameter.
         * @param rb
         */
        private CardHandler(byte[] rb){
            adfInfo = rb;
        }

        /**
         * adfInfo byte[]
         *
         * @return
         */
        public byte[] getAdfInfo() {
            return adfInfo;
        }
    }

}