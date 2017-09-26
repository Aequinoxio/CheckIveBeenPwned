/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkivebeenpwned;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 *
 * @author GLLGRL68H26H282H
 */
public class TestProxy implements PwnedEvent{

    private static ArrayList<String> testingAccounts = null;
    
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
             
        // Testing purpose
        testingAccounts=new ArrayList<>();
        testingAccounts.add("procava@gmail.com");
        testingAccounts.add("procava1@gmail.com");

        TestProxy tp=new TestProxy();
        
        CheckPwned cp = new CheckPwned(tp);
        cp.init();
        cp.checkAccounts(testingAccounts);
    } 

    @Override
    public void DoAction(String testo) {
        System.out.println(testo);
    }
}
