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
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 *
 * @author GLLGRL68H26H282H
 */
public class CheckPwned {
    
    private final String BEENPWNED="https://haveibeenpwned.com/api/v2/breachedaccount/";
    private final String UserAgent="Pwnage-Checker-For-iOS";
    private Proxy proxy;
    private final int SLEEPTIMESECONDS=2;  // Secondi

    PwnedEvent pwnedEvent;
    
    public CheckPwned(PwnedEvent p) {
        pwnedEvent=p;
    }
    
    public void init(){        
        System.setProperty("http.agent", UserAgent);
        NetworkProxy np = new NetworkProxy() ;
        np.init();
        proxy = np.getNetworkProxy();
    }

    public void checkAccounts(ArrayList<String> accountsToCheck) throws IOException, InterruptedException{
        for (String accountToTest : accountsToCheck){
            TimeUnit.SECONDS.sleep(SLEEPTIMESECONDS);  // Rispettiamo il tempo del servizio.
            checkPwned(accountToTest);
        }     
    }
    
    private void checkPwned(String accountToTest) throws MalformedURLException, IOException{
        URL url = new URL(BEENPWNED+accountToTest);
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        
        int respCode=connection.getResponseCode();
        switch (respCode) {
            case 200 : // Ok — everything worked and there's a string array of pwned sites for the account
            {
                OhOhPWNED(accountToTest,connection);
                break; // proseguo l'analisi
            }
            case 400 : // Bad request — the account does not comply with an acceptable format (i.e. it's an empty string)
            {
                throw (new IOException("Bad request — the account does not comply with an acceptable format (i.e. it's an empty string)"));
                
            }
            case 403 : // Forbidden — no user agent has been specified in the request
            {
                throw (new IOException("Forbidden — no user agent has been specified in the request"));
            }
            case 404 : // Not found — the account could not be found and has therefore not been pwned
            {
                ItsAllOk(accountToTest);
                break;
            }
            case 429 : // Too many requests — the rate limit has been exceeded
            {
                throw (new IOException("Too many requests — the rate limit has been exceeded"));
            }
        }
    }
    
    private void OhOhPWNED (String accountName, HttpURLConnection connection) throws IOException{
        InputStream inputStream = connection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String tmpLine = "";
        StringBuilder sb = new StringBuilder();
        
        while ((tmpLine = br.readLine()) != null) {
            sb.append(tmpLine);
        }

        String risultato=parseJson(sb.toString());
        
        pwnedEvent.DoAction(accountName + "\t"+risultato);
    }
    
    private void ItsAllOk(String accountName){
        pwnedEvent.DoAction(accountName + "\t"+"NOT PWNED");
//        System.out.println(accountName + "\tNOT PWNED");
    }
    
    private String parseJson(String testo){
        StringReader sr = new StringReader(testo);
        JsonReader jsonReader = Json.createReader(sr);
        JsonArray ja = jsonReader.readArray();
        StringBuilder sb = new StringBuilder();
        for (JsonValue jo : ja){          
            sb.append(jo.asJsonObject().getString("Title")).append("\t");
            sb.append(jo.asJsonObject().getString("Name")).append("\t");
            sb.append(jo.asJsonObject().getString("Domain")).append("\t");
            sb.append(jo.asJsonObject().getString("BreachDate")).append("\t");           
        }   
        return sb.toString();
    }    
}
