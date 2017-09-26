/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkivebeenpwned;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author utente
 */
public class CheckPwnedWorker extends SwingWorker<List<String>, PwnedResult> {

    private final String BEENPWNED = "https://haveibeenpwned.com/api/v2/breachedaccount/";
    private final String UserAgent = "Pwnage-Checker-For-iOS";
    private Proxy proxy;
    private final int SLEEPTIMESECONDS = 2;  // Secondi

    List<String> serverResponse;
    List<String> accountsToCheck;

    // Devo chiudere tutto?
    boolean shouldExit = false;

    @Override
    protected List<String> doInBackground() throws Exception {
        initNetwork();
        serverResponse = new ArrayList<String>();
        checkAccounts();
        return serverResponse;
    }

//    @Override
//    protected void process(List<String> chunks) {
//        for (String s : chunks) {
//            area.setText(s);
//        }
//    }
    PwnedEvent pwnedEvent;

    public CheckPwnedWorker(ArrayList<String> accountsToCheck, Proxy proxy) {
        // pwnedEvent = p;
        this.accountsToCheck = accountsToCheck;
        this.proxy = proxy;
    }

    private void initNetwork() {
        System.setProperty("http.agent", UserAgent);
    }

    private void checkAccounts() throws IOException, InterruptedException {
        for (String accountToTest : accountsToCheck) {
            // Se devo uscire termino tutto
            if (shouldExit) {
                break;
            }

            TimeUnit.SECONDS.sleep(SLEEPTIMESECONDS);  // Rispettiamo il tempo del servizio.
            checkPwned(accountToTest);
//            publish(accountToTest);
        }
    }

    private void checkPwned(String accountToTest) throws MalformedURLException, IOException {
        URL url = new URL(BEENPWNED + accountToTest);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

        int respCode = connection.getResponseCode();
        switch (respCode) {
            case 200: // Ok — everything worked and there's a string array of pwned sites for the account
            {
                OhOhPWNED(accountToTest, connection);
                break; // proseguo l'analisi
            }
            case 400: // Bad request — the account does not comply with an acceptable format (i.e. it's an empty string)
            {
                throw (new IOException("Bad request — the account does not comply with an acceptable format (i.e. it's an empty string)"));

            }
            case 403: // Forbidden — no user agent has been specified in the request
            {
                throw (new IOException("Forbidden — no user agent has been specified in the request"));
            }
            case 404: // Not found — the account could not be found and has therefore not been pwned
            {
                ItsAllOk(accountToTest);
                break;
            }
            case 429: // Too many requests — the rate limit has been exceeded
            {
                throw (new IOException("Too many requests — the rate limit has been exceeded"));
            }
        }

    }

    private void OhOhPWNED(String accountName, HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String tmpLine = "";
        StringBuilder sb = new StringBuilder();

        while ((tmpLine = br.readLine()) != null) {
            sb.append(tmpLine);
        }

        String risultato = parseJson(sb.toString());

        String[] lines = risultato.split("\n");
        serverResponse.add(accountName);
        for (String s : lines) {
            serverResponse.add("\t"+s);
            PwnedResult PR = new PwnedResult();
            PR.ACCOUNT = accountName;
            PR.SERVER_REULT = "\t"+s;
            publish(PR);
        }
    }

    private void ItsAllOk(String accountName) {
        serverResponse.add(accountName + "\t" + "NOT PWNED");
        PwnedResult PR = new PwnedResult();
        PR.ACCOUNT = accountName;
        PR.SERVER_REULT = "NOT PWNED";
        publish(PR);
    }

    private String parseJson(String testo) {
        StringReader sr = new StringReader(testo);
        JsonReader jsonReader = Json.createReader(sr);
        JsonArray ja = jsonReader.readArray();
        StringBuilder sb = new StringBuilder();
        for (JsonValue jo : ja) {
            sb.append(jo.asJsonObject().getString("Title")).append("\t");
            sb.append(jo.asJsonObject().getString("Name")).append("\t");
            sb.append(jo.asJsonObject().getString("Domain")).append("\t");
            sb.append(jo.asJsonObject().getString("BreachDate")).append("\n");
        }
        return sb.toString();
    }

    public void stopSearch() {
        // TODO:
        shouldExit = true;
    }
}
