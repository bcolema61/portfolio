/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author colemanb
 */


//Copyright 2016, Brandon Coleman, All rights reserved.

import com.jaunt.*;
import com.jaunt.component.*;
import java.awt.ComponentOrientation;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class AwesomeSEC extends javax.swing.JFrame {

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy.hhmma");
    String date = sdf.format(new Date());
    String currentDate = date;

    ImageIcon img = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo/MPAA_LOGO.png")));

    String url;
    String path;
    String fileName;
    String pages;

    String gPage;

    String gSearch = "";
    String bSearch = "";

    List<String> gSites = new ArrayList<>();
    List<String> gLinks = new ArrayList<>();
    List<String> gDescs = new ArrayList<>();
    List<String> gDates = new ArrayList<>();

    List<String> bTitles = new ArrayList<>();
    List<String> bLinks = new ArrayList<>();
    List<String> bDescs = new ArrayList<>();

    String bKey = "vGHKqr4Z3suNn30V/djLDqXCtBNnTCOtZ2nBwH29o+A";

    private googleWorker googler;
    private bingWorker binger;
    private screenshotWorker screener;
    private gScreenshotWorker gScreener;

    class googleWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, JauntException {
            returnData();
            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            if (gLinks.isEmpty()) {
                googleExportButton.setEnabled(false);
            } else {
                googleExportButton.setEnabled(true);
            }
        }
    }

    class bingWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, IOException {
            pullData();
            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            if (bLinks.isEmpty()) {
                bingExportButton.setEnabled(false);
            } else {
                bingExportButton.setEnabled(true);
            }
        }
    }

    class screenshotWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, IOException {
            String search = bSearch;
            search = search.replace(" ", "%20");
            String pages = pagesText1.getText();
            int counter = 1;
            int cc = 1;

            bingUpdate("Windows will pop up while the program does it's work.  Please don't touch anything until you see the \"Done!\" message.");

            for (int i = 0; i <= Integer.parseInt(pages) - 1; i++) {
                try {
                    String screenshotName = bScreenshotFile.getText() + "page " + cc;
                    bingUpdate("Gathering screenshot...");
                    screenshot("http://www.bing.com/search?q=" + search + "&go=Submit&qs=n&form=QBLH&pq=test&sc=8-5&sp=-1&sk=&cvid=f8257356f90e44bf86365827fbb618dc&first=" + counter + "&FORM=PERE", screenshotName);
                    counter = counter + 14;
                    cc++;
                } catch (IOException ex) {
                    Logger.getLogger(AwesomeSEC.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            bingUpdate("-------------Done!  You are free to use the computer.-------------");
            bingUpdate("-------------Files saved in G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\Search Engine Crawler-------------");
        }
    }

    class gScreenshotWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, IOException, JauntException {
            UserAgent ua = googleSearch();
            String url = ua.doc.getUrl();
            String screenshotName = gScreenshotFile.getText();
            String search = gSearch;
            search = search.replace(" ", "+");
            String timeRange = searchTime.getSelectedItem().toString();

            if (!timeRange.equals("Any time")) {
                String time = "";

                if (timeRange.equals("Past hour")) {
                    time = "h";
                }
                if (timeRange.equals("Past 24 hours")) {
                    time = "d";
                }
                if (timeRange.equals("Past week")) {
                    time = "w";
                }
                if (timeRange.equals("Past month")) {
                    time = "m";
                }
                if (timeRange.equals("Past year")) {
                    time = "y";
                }
                search = search.concat("&tbs=qdr:" + time);
            }

            int pages = Integer.parseInt(pagesText.getText());
            int counter = 10;

            update("Windows may pop up while the program does it's work.  Please don't touch anything until you see the \"Done!\" message.");

            update("Gathering screenshot for page 1");
            screenshot(url, screenshotName + " page 1");

            for (int i = 0; i < pages - 1; i++) {
                String mod = "https://www.google.com/search?q=" + search + "&start=" + counter + "#";

                update("Gathering screenshot for page " + (i + 2));

                screenshot(mod, screenshotName + " page " + (i + 2));
                counter = counter + 10;
            }

            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            update("-------------Done!  You are free to use the computer.-------------");
            update("-------------Files saved in G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\Search Engine Crawler-------------");
        }
    }

    //---------------------Google---------------------------------
    public String getUrl() {
        url = searchTerms.getText();
        return url;
    }

    public UserAgent googleSearch() throws ResponseException, JauntException {
        UserAgent ua = new UserAgent();
        String search = gSearch;
        search = search.replace(" ", "+");
        String timeRange = searchTime.getSelectedItem().toString();

        if (!timeRange.equals("Any time")) {
            String time = "";

            if (timeRange.equals("Past hour")) {
                time = "h";
            }
            if (timeRange.equals("Past 24 hours")) {
                time = "d";
            }
            if (timeRange.equals("Past week")) {
                time = "w";
            }
            if (timeRange.equals("Past month")) {
                time = "m";
            }
            if (timeRange.equals("Past year")) {
                time = "y";
            }
            search = search.concat("&tbs=qdr:" + time);
        }

        update("Searching Google for: " + search);
        ua.visit("http://google.com");
        ua.doc.apply(search);
        ua.doc.submit();


        return ua;
    }

    public void returnData() throws JauntException {
        int pages = Integer.parseInt(pagesText.getText());
        String search = gSearch;
        search = search.replace(" ", "+");
        String timeRange = searchTime.getSelectedItem().toString();

        if (!timeRange.equals("Any time")) {
            String time = "";

            if (timeRange.equals("Past hour")) {
                time = "h";
            }
            if (timeRange.equals("Past 24 hours")) {
                time = "d";
            }
            if (timeRange.equals("Past week")) {
                time = "w";
            }
            if (timeRange.equals("Past month")) {
                time = "m";
            }
            if (timeRange.equals("Past year")) {
                time = "y";
            }
            search = search.concat("&tbs=qdr:" + time);
        }

        int counter = 0;

        UserAgent ua = googleSearch();
        String url = ua.doc.getUrl();
        Elements directLinks = ua.doc.findEvery("<h3 class=r>").findEvery("<a>");
        Elements descriptions = ua.doc.findEvery("<span class=st>");

        for (Element e : directLinks) {
            String href = e.getAt("href");
            String[] actualLink = href.split("&amp;sa=U&amp;ved=0");
            String mod = actualLink[0];
            mod = mod.replace("http://www.google.com/url?q=", "");
            String text = e.getText();
            text = text.replace(",", "");
            update(mod);
            update(text);
            gLinks.add(mod);
            gSites.add(text);
        }

        for (Element e : descriptions) {
            String text = e.getText();
            text = text.replace(",", "");
            text = text.replace("\n", "");
            update(text);
            gDescs.add(text);
        }

        for (int i = 0; i < pages - 1; i++) {
            String mod = "https://www.google.com/search?q=" + search + "&start=" + counter + "#";

            UserAgent ua1 = new UserAgent();
            ua1.visit(mod);
            update("Searching Google for: " + search + " - Page " + (i + 2));

            Elements directLinks1 = ua1.doc.findEvery("<h3 class=r>").findEvery("<a>");
            Elements descriptions1 = ua1.doc.findEvery("<span class=st>");
            
            
            
            for (Element e : directLinks1) {
                String href = e.getAt("href");
                String[] actualLink = href.split("&amp;sa=U&amp;ved=0");
                String mod1 = actualLink[0];
                mod1 = mod1.replace("https://www.google.com/url?q=", "");
                String text = e.getText();
                text = text.replace(",", "");
                update(mod1);
                update(text);
                gLinks.add(mod1);
                gSites.add(text);
            }

            for (Element e : descriptions1) {
                String text = e.getText();
                text = text.replace(",", "");
                text = text.replace("\n", "");
                update(text);
                gDescs.add(text);
            }

            counter = counter + 10;
        }

        for (int o = 0; o <= gLinks.size() - 1; o++) {
            update((o + 1) + ") Link: " + gLinks.get(o));
            update((o + 1) + ") Site: " + gSites.get(o));
            update((o + 1) + ") Description: " + gDescs.get(o));
            update("-------------------");
        }

    }

    public void update(String input) {
        output.append(input + "\n");
    }

    public void updateGoogleSearch() {
        String specific = googleQuotes.getText();
        String ex1 = googleExclude.getText();
        String ex2 = googleExclude2.getText();
        String site = googleSite.getText();
        String finalText = "";
        String gSearchTerms = searchTerms.getText();
        String commonSearchTerms = commonSearchTermsList.getSelectedItem().toString();

        finalText = finalText.concat(gSearchTerms);
        if (gSearchTerms.equals("")) {
            if (!site.equals("")) {
                finalText = finalText.concat("site:" + site);
            }
        } else {
            if (!site.equals("")) {
                finalText = finalText.concat(" site:" + site);
            }
        }

        if (!specific.equals("")) {
            finalText = finalText.concat(" \"" + specific + "\"");
        }
        if (!ex1.equals("")) {
            finalText = finalText.concat(" -" + ex1);
        }
        if (!ex2.equals("")) {
            finalText = finalText.concat(" -" + ex2);
        }

        if (cst.isSelected()) {
            finalText = finalText.concat(" " + commonSearchTerms);
        }

        if (gSearch != finalText) {
            finalGoogleSearchText.setText(finalText);
            gSearch = finalText;
        }

    }

    public Boolean checkGoogle() {
        try {
            int pages = Integer.parseInt(pagesText.getText());
        } catch (java.lang.NumberFormatException q) {
            update("Pages field needs a valid number!");
            return false;
        }

        if (finalGoogleSearchText.getText().equals("")) {
            update("Please enter a search.");
            return false;
        }

        return true;
    }

    public void popTermList() throws FileNotFoundException, IOException {
        List<String> list = Files.readAllLines(new File("searchterms.txt").toPath(), Charset.defaultCharset());

        for (int i = 0; i <= list.size() - 1; i++) {
            commonSearchTermsList.addItem(list.get(i));
            commonSearchTermsList1.addItem(list.get(i));
        }

    }

    public void googleCsv() {
        googleExport.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = googleExport.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = googleExport.getSelectedFile();

            String path = file.getAbsolutePath() + "/";
            File dir = new File(path);
            if (!dir.exists()) {
                update("Creating output folder: " + path);
                dir.mkdir();
            }
            try {
                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + fileNameText.getText() + " " + date + ".csv"), "UTF8"));

                writer.append("Site,Link,Description\n");
                for (int i = 0; i <= gSites.size() - 1; i++) {
                    String link = gLinks.get(i);
                    if (link.contains("&amp")) {
                        String[] linksArray = link.split("&amp");
                        writer.append(gSites.get(i) + "," + linksArray[0] + "," + gDescs.get(i) + "\n");
                    } else {
                        writer.append(gSites.get(i) + "," + gLinks.get(i) + "," + gDescs.get(i) + "\n");
                    }

                }

                update("File saved as: " + path + fileNameText.getText() + " " + date + ".csv");

                writer.flush();
                writer.close();

            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AwesomeSEC.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AwesomeSEC.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AwesomeSEC.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //-------------------Bing-----------------------------------------
    public void bingUpdate(String input) {
        output1.append(input + "\n");
    }

    public org.jsoup.nodes.Document Connect() throws IOException {
        String search = bSearch;
        search = search.replace(" ", "+");

        search = search.replace(" ", "%20");
        String username = bKey;
        String password = bKey;
        String login = username + ":" + password;
        String base64login = new String(Base64.encodeBase64(login.getBytes()));

        bingUpdate("Searching Bing for: " + search);
        bingUpdate("-------------");
        System.out.println("https://api.datamarket.azure.com/Bing/Search/v1/Web?Query=%27" + search + "%27");
        org.jsoup.nodes.Document doc = Jsoup.connect("https://api.datamarket.azure.com/Bing/Search/v1/Web?Query=%27" + search + "%27")
                .header("Authorization", "Basic " + base64login)
                .get();
        return doc;
    }

    public void updateBingSearch() {
        String specific = bingQuotes.getText();
        String ex1 = bingExclude1.getText();
        String ex2 = bingExclude2.getText();
        String site = bingSite.getText();
        String finalText = "";
        String finalSText = "";
        String bSearchTerms = searchTerms1.getText();
        String commonSearchTerms = commonSearchTermsList1.getSelectedItem().toString();

        finalText = finalText.concat(bSearchTerms);
        finalSText = finalSText.concat(bSearchTerms);
        if (bSearchTerms.equals("")) {
            if (!site.equals("")) {
                finalText = finalText.concat("site:" + site);
                finalSText = finalSText.concat("site%3A" + site);
            }
        } else {
            if (!site.equals("")) {
                finalText = finalText.concat(" site:" + site);
                finalSText = finalSText.concat(" site%3A" + site);
            }
        }

        if (!specific.equals("")) {
            finalText = finalText.concat(" \"" + specific + "\"");
            finalSText = finalSText.concat(" \"" + specific + "\"");
        }
        if (!ex1.equals("")) {
            finalText = finalText.concat(" -" + ex1);
            finalSText = finalSText.concat(" -" + ex1);
        }
        if (!ex2.equals("")) {
            finalText = finalText.concat(" -" + ex2);
            finalSText = finalSText.concat(" -" + ex2);
        }

        if (cst1.isSelected()) {
            finalText = finalText.concat(" " + commonSearchTerms);
            finalSText = finalSText.concat(" " + commonSearchTerms);
        }

        if (bSearch != finalSText) {
            finalBingSearchText.setText(finalText);
            bSearch = finalSText;
        }

    }

    public org.jsoup.nodes.Document ConnectPage(int page) throws IOException {
        String search = bSearch;
        search = search.replace(" ", "%20");
        String username = bKey;
        String password = bKey;
        String login = username + ":" + password;
        String base64login = new String(Base64.encodeBase64(login.getBytes()));

        bingUpdate("Searching Bing for: " + search + (" - Page " + (page + 1)));
        bingUpdate("-------------");

        page = 14 * page + 15;
        System.out.println("https://api.datamarket.azure.com/Bing/Search/v1/Web?Query=%27" + search + "%27&$top" + page);
        org.jsoup.nodes.Document doc = Jsoup.connect("https://api.datamarket.azure.com/Bing/Search/v1/Web?Query=%27" + search + "%27&$top=" + page)
                .header("Authorization", "Basic " + base64login)
                .get();

        return doc;
    }

    public void pullData() throws IOException {
        String pages = pagesText1.getText();
        int pagesToRun = Integer.parseInt(pages);
        org.jsoup.nodes.Document doc = Connect();
        int results = Integer.parseInt(resultsText.getText());
        int counter = 1;
        int pCounter = 1;

        for (org.jsoup.nodes.Element e : doc.select("entry")) {
            String title = e.select("d|title").text();
            String link = e.select("d|url").text();
            String desc = e.select("d|description").text();
            title = title.replace(",", "");
            desc = desc.replace(",", "");
            if (counter <= results) {
                bTitles.add(title);
                bLinks.add(link);
                bDescs.add(desc);
                bingUpdate("(" + counter + ") " + title);
                bingUpdate("(" + counter + ") " + link);
                bingUpdate("(" + counter + ") " + desc);
                bingUpdate("-------------");
            }
            counter++;
        }

        for (int i = 1; i < pagesToRun; i++) {
            org.jsoup.nodes.Document doc1 = ConnectPage(i);
            for (org.jsoup.nodes.Element e : doc1.select("entry")) {
                String title = e.select("d|title").text();
                String link = e.select("d|url").text();
                String desc = e.select("d|description").text();
                title = title.replace(",", "");
                desc = desc.replace(",", "");
                if (pCounter <= results) {
                    bTitles.add(title);
                    bLinks.add(link);
                    bDescs.add(desc);
                    bingUpdate("(" + pCounter + ") " + title);
                    bingUpdate("(" + pCounter + ") " + link);
                    bingUpdate("(" + pCounter + ") " + desc);
                    bingUpdate("-------------");
                }
                pCounter++;
            }
        }
    }

    public void bingCsv() {
        bingExport.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = bingExport.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = bingExport.getSelectedFile();

            String path = file.getAbsolutePath() + "/";
            File dir = new File(path);
            if (!dir.exists()) {
                update("Creating output folder: " + path);
                dir.mkdir();
            }
            try {
                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + fileNameText1.getText() + " " + date + ".csv"), "UTF8"));

                writer.append("Search Terms Used,Title,Link,Description\n");
                for (int i = 0; i <= bTitles.size() - 1; i++) {
                    writer.append(finalBingSearchText.getText() + "," + bTitles.get(i) + "," + bLinks.get(i) + "," + bDescs.get(i) + "\n");
                }

                bingUpdate("File saved as: " + path + fileNameText1.getText() + " " + date + ".csv");

                writer.flush();
                writer.close();

            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AwesomeSEC.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AwesomeSEC.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AwesomeSEC.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void screenshot(String url, String file) throws IOException {
        WebDriver driver = new FirefoxDriver();
        driver.get(url);
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\Search Engine Crawler\\" + file + " - " + date + ".png"));
        driver.close();
    }

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void openWebpage(String urlString) throws MalformedURLException {
        try {
            String mod = urlString;
            mod = mod.replace(" ", "+");
            mod = mod.replace("\"", "%22");
            openWebpage(new URL(mod).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates new form JsCrawlerUI
     */
    public AwesomeSEC() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        googleExport = new javax.swing.JFileChooser();
        bingExport = new javax.swing.JFileChooser();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        searchTerms = new javax.swing.JTextField();
        googleSearchCopy = new javax.swing.JButton();
        finalGoogleSearchText = new javax.swing.JTextField();
        openGoogleSearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        output = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fileNameText = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        gScreenshotFile = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        pagesText = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        googleSite = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        googleExclude = new javax.swing.JTextField();
        googleExclude2 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        googleQuotes = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        commonSearchTermsList = new javax.swing.JComboBox();
        cst = new javax.swing.JCheckBox();
        searchTime = new javax.swing.JComboBox();
        jPanel11 = new javax.swing.JPanel();
        googleClearButton = new javax.swing.JButton();
        outputButton = new javax.swing.JButton();
        googleExportButton = new javax.swing.JButton();
        googleScreenshotButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        output1 = new javax.swing.JTextArea();
        jPanel12 = new javax.swing.JPanel();
        searchTerms1 = new javax.swing.JTextField();
        bingSearchCopy = new javax.swing.JButton();
        finalBingSearchText = new javax.swing.JTextField();
        openBingSearch = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        pagesText1 = new javax.swing.JTextField();
        jPanel14 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        bingSite = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        bingExclude1 = new javax.swing.JTextField();
        bingExclude2 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        bingQuotes = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        commonSearchTermsList1 = new javax.swing.JComboBox();
        cst1 = new javax.swing.JCheckBox();
        searchTime1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        resultsText = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        fileNameText1 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        bScreenshotFile = new javax.swing.JTextField();
        jPanel17 = new javax.swing.JPanel();
        bingclearButton = new javax.swing.JButton();
        outputButton2 = new javax.swing.JButton();
        bingExportButton = new javax.swing.JButton();
        bingScreenshotButton = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();

        googleExport.setCurrentDirectory(new java.io.File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\Search Engine Crawler\\Exports"));

        bingExport.setCurrentDirectory(new java.io.File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\Search Engine Crawler\\Exports"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Terms"));

        searchTerms.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchTermsFocusLost(evt);
            }
        });

        googleSearchCopy.setText("Copy");
        googleSearchCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleSearchCopyActionPerformed(evt);
            }
        });

        finalGoogleSearchText.setEditable(false);
        finalGoogleSearchText.setBackground(new java.awt.Color(204, 204, 255));

        openGoogleSearch.setText("Open Google");
        openGoogleSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openGoogleSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(finalGoogleSearchText)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(googleSearchCopy)
                        .addGap(18, 18, 18)
                        .addComponent(openGoogleSearch))
                    .addComponent(searchTerms))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchTerms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(finalGoogleSearchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(googleSearchCopy)
                    .addComponent(openGoogleSearch)))
        );

        output.setEditable(false);
        output.setColumns(20);
        output.setRows(5);
        jScrollPane1.setViewportView(output);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));

        jLabel1.setText("File Name");

        jLabel7.setText("Screenshot File Name");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fileNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(gScreenshotFile))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fileNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(gScreenshotFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        jLabel5.setText("Pages");

        pagesText.setText("1");
        pagesText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                pagesTextFocusLost(evt);
            }
        });

        jLabel8.setText("Site");

        googleSite.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                googleSiteFocusLost(evt);
            }
        });

        jLabel9.setText("Exclude");

        googleExclude.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                googleExcludeFocusLost(evt);
            }
        });

        googleExclude2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                googleExclude2FocusLost(evt);
            }
        });

        jLabel10.setText("Specific Text");

        googleQuotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                googleQuotesFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(googleSite))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(googleQuotes, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(googleExclude2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(googleExclude))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(googleSite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(googleQuotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(googleExclude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(googleExclude2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        commonSearchTermsList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commonSearchTermsListFocusLost(evt);
            }
        });

        cst.setText("Common Search Terms");
        cst.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cstFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cst)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(commonSearchTermsList, 0, 163, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cst)
                    .addComponent(commonSearchTermsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        searchTime.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Any time", "Past hour", "Past 24 hours", "Past week", "Past month", "Past year" }));
        searchTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchTimeFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(searchTime, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(168, 168, 168))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pagesText, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(pagesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(searchTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        googleClearButton.setText("Clear");
        googleClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleClearButtonActionPerformed(evt);
            }
        });

        outputButton.setText("Run");
        outputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputButtonActionPerformed(evt);
            }
        });

        googleExportButton.setText("Export");
        googleExportButton.setEnabled(false);
        googleExportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleExportButtonActionPerformed(evt);
            }
        });

        googleScreenshotButton.setText("Screenshot");
        googleScreenshotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                googleScreenshotButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(googleScreenshotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(googleExportButton, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 188, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(googleClearButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(outputButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(googleClearButton)
                        .addGap(0, 14, Short.MAX_VALUE))
                    .addComponent(googleExportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(googleScreenshotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 482, Short.MAX_VALUE))
                        .addGap(10, 10, 10))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Google", jPanel1);

        output1.setEditable(false);
        output1.setColumns(20);
        output1.setRows(5);
        jScrollPane2.setViewportView(output1);

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Terms"));

        searchTerms1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchTerms1FocusLost(evt);
            }
        });

        bingSearchCopy.setText("Copy");
        bingSearchCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bingSearchCopyActionPerformed(evt);
            }
        });

        finalBingSearchText.setEditable(false);
        finalBingSearchText.setBackground(new java.awt.Color(204, 204, 255));

        openBingSearch.setText("Open Bing");
        openBingSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBingSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(finalBingSearchText)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(bingSearchCopy)
                        .addGap(18, 18, 18)
                        .addComponent(openBingSearch))
                    .addComponent(searchTerms1))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchTerms1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(finalBingSearchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bingSearchCopy)
                    .addComponent(openBingSearch)))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        jLabel11.setText("Pages");

        pagesText1.setText("1");
        pagesText1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                pagesText1FocusLost(evt);
            }
        });

        jLabel12.setText("Site");

        bingSite.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                bingSiteFocusLost(evt);
            }
        });

        jLabel13.setText("Exclude");

        bingExclude1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                bingExclude1FocusLost(evt);
            }
        });

        bingExclude2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                bingExclude2FocusLost(evt);
            }
        });

        jLabel14.setText("Specific Text");

        bingQuotes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                bingQuotesFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bingSite))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bingQuotes, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bingExclude2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(bingExclude1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(bingSite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(bingQuotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(bingExclude1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bingExclude2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel15.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        commonSearchTermsList1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commonSearchTermsList1FocusLost(evt);
            }
        });

        cst1.setText("Common Search Terms");
        cst1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                cst1FocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cst1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(commonSearchTermsList1, 0, 163, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cst1)
                    .addComponent(commonSearchTermsList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        searchTime1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Any time", "Past 24 hours", "Past week", "Past month" }));
        searchTime1.setEnabled(false);
        searchTime1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                searchTime1FocusLost(evt);
            }
        });

        jLabel2.setText("Results");

        resultsText.setText("30");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(searchTime1, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(168, 168, 168))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pagesText1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(resultsText, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(pagesText1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(resultsText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(searchTime1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));

        jLabel15.setText("File Name");

        jLabel16.setText("Screenshot File Name");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(fileNameText1, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(bScreenshotFile))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(fileNameText1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(bScreenshotFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        bingclearButton.setText("Clear");
        bingclearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bingclearButtonActionPerformed(evt);
            }
        });

        outputButton2.setText("Run");
        outputButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputButton2ActionPerformed(evt);
            }
        });

        bingExportButton.setText("Export");
        bingExportButton.setEnabled(false);
        bingExportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bingExportButtonActionPerformed(evt);
            }
        });

        bingScreenshotButton.setText("Screenshot");
        bingScreenshotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bingScreenshotButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bingScreenshotButton, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(bingExportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bingclearButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(outputButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(bingclearButton)
                        .addGap(0, 14, Short.MAX_VALUE))
                    .addComponent(bingExportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bingScreenshotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 482, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Bing", jPanel6);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 112, Short.MAX_VALUE)
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(659, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(115, 115, 115)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(359, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Google Analytics", jPanel7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 950, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void outputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputButtonActionPerformed
        if (checkGoogle().equals(true)) {
            googler = new googleWorker();
            googler.execute();
        }
    }//GEN-LAST:event_outputButtonActionPerformed

    private void googleExportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleExportButtonActionPerformed
        googleCsv();
    }//GEN-LAST:event_googleExportButtonActionPerformed

    private void googleScreenshotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleScreenshotButtonActionPerformed
        gScreener = new gScreenshotWorker();
        gScreener.execute();
    }//GEN-LAST:event_googleScreenshotButtonActionPerformed

    private void googleClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleClearButtonActionPerformed
        gSites.clear();
        gDescs.clear();
        gLinks.clear();
        googleExportButton.setEnabled(false);
        update("List cleared.");
    }//GEN-LAST:event_googleClearButtonActionPerformed

    private void googleSearchCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_googleSearchCopyActionPerformed
        StringSelection selection = new StringSelection(finalGoogleSearchText.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }//GEN-LAST:event_googleSearchCopyActionPerformed

    private void openGoogleSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openGoogleSearchActionPerformed
        try {
            String search = gSearch;
            int pages = Integer.parseInt(pagesText.getText());
            int counter = 0;
            String timeRange = searchTime.getSelectedItem().toString();
            if (!timeRange.equals("Any time")) {
                String time = "";

                if (timeRange.equals("Past hour")) {
                    time = "h";
                }
                if (timeRange.equals("Past 24 hours")) {
                    time = "d";
                }
                if (timeRange.equals("Past week")) {
                    time = "w";
                }
                if (timeRange.equals("Past month")) {
                    time = "m";
                }
                if (timeRange.equals("Past year")) {
                    time = "y";
                }
                search = search.concat("&tbs=qdr:" + time);
            }

            for (int i = 0; i <= pages - 1; i++) {
                openWebpage("https://www.google.com/search?q=" + search + "&start=" + counter + "#");
                counter = counter + 10;
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(AwesomeSEC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_openGoogleSearchActionPerformed

    private void pagesTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pagesTextFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_pagesTextFocusLost

    private void googleSiteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_googleSiteFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_googleSiteFocusLost

    private void googleQuotesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_googleQuotesFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_googleQuotesFocusLost

    private void googleExcludeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_googleExcludeFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_googleExcludeFocusLost

    private void googleExclude2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_googleExclude2FocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_googleExclude2FocusLost

    private void searchTermsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTermsFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_searchTermsFocusLost

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            popTermList();
            this.setIconImage(img.getImage());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AwesomeSEC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AwesomeSEC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowOpened

    private void cstFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cstFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_cstFocusLost

    private void commonSearchTermsListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commonSearchTermsListFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_commonSearchTermsListFocusLost

    private void searchTimeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTimeFocusLost
        updateGoogleSearch();
    }//GEN-LAST:event_searchTimeFocusLost

    private void searchTerms1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTerms1FocusLost
        updateBingSearch();
    }//GEN-LAST:event_searchTerms1FocusLost

    private void bingSearchCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bingSearchCopyActionPerformed
        StringSelection selection = new StringSelection(finalBingSearchText.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }//GEN-LAST:event_bingSearchCopyActionPerformed

    private void openBingSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBingSearchActionPerformed
        try {
            String search = bSearch;
            int pages = Integer.parseInt(pagesText1.getText());
            int counter = 1;

            for (int i = 1; i <= pages; i++) {
                openWebpage("http://www.bing.com/search?q=" + search + "&go=Submit&qs=n&form=QBLH&pq=test&sc=8-5&sp=-1&sk=&cvid=f8257356f90e44bf86365827fbb618dc&first=" + counter + "&FORM=PERE");
                counter = counter + 14;
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(AwesomeSEC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_openBingSearchActionPerformed

    private void pagesText1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pagesText1FocusLost
        updateBingSearch();
    }//GEN-LAST:event_pagesText1FocusLost

    private void bingSiteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_bingSiteFocusLost
        updateBingSearch();
    }//GEN-LAST:event_bingSiteFocusLost

    private void bingExclude1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_bingExclude1FocusLost
        updateBingSearch();
    }//GEN-LAST:event_bingExclude1FocusLost

    private void bingExclude2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_bingExclude2FocusLost
        updateBingSearch();
    }//GEN-LAST:event_bingExclude2FocusLost

    private void bingQuotesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_bingQuotesFocusLost
        updateBingSearch();
    }//GEN-LAST:event_bingQuotesFocusLost

    private void commonSearchTermsList1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commonSearchTermsList1FocusLost
        updateBingSearch();
    }//GEN-LAST:event_commonSearchTermsList1FocusLost

    private void cst1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cst1FocusLost
        updateBingSearch();
    }//GEN-LAST:event_cst1FocusLost

    private void searchTime1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_searchTime1FocusLost
        updateBingSearch();
    }//GEN-LAST:event_searchTime1FocusLost

    private void bingclearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bingclearButtonActionPerformed
        bTitles.clear();
        bDescs.clear();
        bLinks.clear();
        bingExportButton.setEnabled(false);
        bingUpdate("List cleared.");
    }//GEN-LAST:event_bingclearButtonActionPerformed

    private void outputButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputButton2ActionPerformed
        binger = new bingWorker();
        binger.execute();
    }//GEN-LAST:event_outputButton2ActionPerformed

    private void bingExportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bingExportButtonActionPerformed
        bingCsv();
    }//GEN-LAST:event_bingExportButtonActionPerformed

    private void bingScreenshotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bingScreenshotButtonActionPerformed
        screener = new screenshotWorker();
        screener.execute();
    }//GEN-LAST:event_bingScreenshotButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AwesomeSEC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AwesomeSEC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AwesomeSEC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AwesomeSEC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AwesomeSEC().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bScreenshotFile;
    private javax.swing.JTextField bingExclude1;
    private javax.swing.JTextField bingExclude2;
    private javax.swing.JFileChooser bingExport;
    private javax.swing.JButton bingExportButton;
    private javax.swing.JTextField bingQuotes;
    private javax.swing.JButton bingScreenshotButton;
    private javax.swing.JButton bingSearchCopy;
    private javax.swing.JTextField bingSite;
    private javax.swing.JButton bingclearButton;
    private javax.swing.JComboBox commonSearchTermsList;
    private javax.swing.JComboBox commonSearchTermsList1;
    private javax.swing.JCheckBox cst;
    private javax.swing.JCheckBox cst1;
    private javax.swing.JTextField fileNameText;
    private javax.swing.JTextField fileNameText1;
    private javax.swing.JTextField finalBingSearchText;
    private javax.swing.JTextField finalGoogleSearchText;
    private javax.swing.JTextField gScreenshotFile;
    private javax.swing.JButton googleClearButton;
    private javax.swing.JTextField googleExclude;
    private javax.swing.JTextField googleExclude2;
    private javax.swing.JFileChooser googleExport;
    private javax.swing.JButton googleExportButton;
    private javax.swing.JTextField googleQuotes;
    private javax.swing.JButton googleScreenshotButton;
    private javax.swing.JButton googleSearchCopy;
    private javax.swing.JTextField googleSite;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton openBingSearch;
    private javax.swing.JButton openGoogleSearch;
    private javax.swing.JTextArea output;
    private javax.swing.JTextArea output1;
    private javax.swing.JButton outputButton;
    private javax.swing.JButton outputButton2;
    private javax.swing.JTextField pagesText;
    private javax.swing.JTextField pagesText1;
    private javax.swing.JTextField resultsText;
    private javax.swing.JTextField searchTerms;
    private javax.swing.JTextField searchTerms1;
    private javax.swing.JComboBox searchTime;
    private javax.swing.JComboBox searchTime1;
    // End of variables declaration//GEN-END:variables
}
