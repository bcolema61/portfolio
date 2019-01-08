import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jsoup.*;
import org.jsoup.nodes.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jsoup.select.Elements;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author colemanb
 */
public class AwesomeSearchTools extends javax.swing.JFrame {

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy.hhmma");
    String date = sdf.format(new Date());
    String currentDate = date;
    Random rn = new Random();
    int alexaCounter;
    int sniperCounter;
    int awesomeCounter;

    int alexaSearchCounter = 0;
    int sniperSearchCounter = 0;
    int whoisSearchCounter = 0;

    int worth = 0;

    String format;
    String sniperFormat;
    String filePath;
    String sniperPath;
    String sniperKey = "aK255bdw2o758d4WspXsm4";
    String ua = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
    String failedSite = "";
    Boolean limitOneError = false;

    String domainName = null;
    String domainAlexaGlobal = null;
    String domainAlexaUS = null;
    String domainIP = null;
    String domainHost = null;
    String domainCountry = null;
    String domainCity = null;
    String domainState = null;
    String domainRegName = null;
    String domainRegUrl = null;
    String domainRegWhois = null;
    String domainRegistered = null;
    String domainUpdated = null;
    String domainWhois = null;

    List<String> failedSites = new ArrayList<>();

    List<String> domainNames = new ArrayList<>();
    List<String> domainGlobals = new ArrayList<>();
    List<String> domainUSes = new ArrayList<>();
    List<String> domainIPs = new ArrayList<>();
    List<String> domainHosts = new ArrayList<>();
    List<String> domainCountries = new ArrayList<>();
    List<String> domainCities = new ArrayList<>();
    List<String> domainStates = new ArrayList<>();
    List<String> domainRegNames = new ArrayList<>();
    List<String> domainRegUrls = new ArrayList<>();
    List<String> domainRegWhoises = new ArrayList<>();
    List<String> domainRegistereds = new ArrayList<>();
    List<String> domainUpdateds = new ArrayList<>();
    List<String> domainWhoisStuff = new ArrayList<>();

    Boolean siteExists;
    Boolean validToCopy;

    volatile boolean alexaFinished = false;
    volatile boolean sniperFinished = false;

    private alexaWorker alexaThread;
    private sniperWorker sniperThread;
    private awesomeWorker awesomeThread;

//-----------------------Worker threads-----------------------------------------
    //Alexa processing
    class alexaWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, IOException {
            stopAlexa.setEnabled(true);
            alexaCounter = 0;
            List<String> list = new ArrayList<>();
            if (csvFile.isSelected()) {
                list = generateUrlsFromCsv(fromFile.getText());
                update("Alexa", "Searching " + (list.size()) + " sites");
            }
            if (manual.isSelected()) {
                list = generateUrlsFromManual();
                update("Alexa", "Searching " + (list.size()) + " sites");
            }

            String path = outPath.getText() + "/";
            File dir = new File(path);

            if (!dir.exists()) {
                update("Alexa", "Creating output folder: " + path);
                dir.mkdir();
            }

            if (csvOutput.isSelected()) {
                siteOutput.setText("");
                globalOutput.setText("");
                countryOutput.setText("");

                for (int l = 1; l <= 3; l++) {
                    diamonds("site");
                    diamonds("global");
                    diamonds("country");
                }

                update("Alexa", "Creating CSV file at: " + path);
                FileWriter writer = new FileWriter(path + "Alexa Rankings " + date + ".csv");
                format = "csv";
                update("Alexa", "Writing CSV headers...");
                //----------------------------------------------------------------------------------
                writer.append("Site,");

                if (globalRanking.isSelected()) {
                    writer.append("Global Ranking,");
                }
                if (countryRanking.isSelected()) {
                    writer.append("US Ranking");
                }
                writer.append("\n");
                //--------------------------------------------------------------------------------
                update("Alexa", "Generating urls...");
                for (int j = 0; j <= list.size() - 1; j++) {
                    update("Alexa", list.get(j));
                }

                for (int i = 0; i <= list.size() - 1; i++) {
                    String url = alexaSearch(list.get(i));

                    Document doc = alexaConnection(url, (i + 1), (list.size()));

                    writer.append(list.get(i) + ",");

                    if (globalRanking.isSelected()) {
                        globalRankingCsv(doc, writer, list.get(i));
                    }

                    if (countryRanking.isSelected()) {
                        countryRankingCsv(doc, writer, list.get(i));
                    }

                    writer.append("\n");

                }
                writer.flush();
                writer.close();
            }

            if (displayResults.isSelected()) {
                format = "output";
                globalOutput.setText("");
                countryOutput.setText("");
                update("Alexa", "Generating urls...");
                for (int j = 0; j <= list.size() - 1; j++) {
                    update("Alexa", list.get(j));
                }

                for (int i = 0; i <= list.size() - 1; i++) {
                    String url = alexaSearch(list.get(i));

                    Document doc = alexaConnection(url, (i + 1), (list.size()));

                    if (csvFile.isSelected()) {
                        updateSite(list.get(i));
                    }

                    if (globalRanking.isSelected()) {
                        globalRankingOutput(doc, list.get(i));
                    }

                    if (countryRanking.isSelected()) {
                        countryRankingOutput(doc, list.get(i));
                    }
                }
            }
            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            endRunAlexa(alexaCounter, format, filePath);
        }
    }

    //InfoSniper processing
    class sniperWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, IOException {
            stopSniper.setEnabled(true);
            sniperCounter = 0;
            List<String> list = new ArrayList<>();
            if (hostingCsvRadio.isSelected()) {
                list = generateUrlsFromCsvSniper();
                update("Sniper", "Searching " + (list.size()) + " sites");
            }
            if (hostingMultipleRadio.isSelected()) {
                list = generateUrlsFromMultipleSniper();
                update("Sniper", "Searching " + (list.size()) + " sites");
            }
            String path = hostingCsvOut.getText() + "/";
            File dir = new File(path);
            if (!dir.exists()) {
                update("Sniper", "Creating output folder: " + path);
                dir.mkdir();
            }
            sniperPath = hostingCsvOut.getText();
            if (hostingDisplay.isSelected()) {
                sniperFormat = "output";
                if (hostingSingleRadio.isSelected()) {
                    String site = hostingSingle.getText();
                    String url = sniperSearch(site);
                    hostingDomain.setText(site);
                    Document doc = sniperConnection(url, 1, 1);
                    hostingResults.append("-----" + site + "-----\n");
                    hostingResults.append("IP: " + pullIP(doc) + "\n");
                    hostingResults.append("Host: " + pullHost(doc) + "\n");
                    hostingResults.append("City: " + pullCity(doc) + "\n");
                    hostingResults.append("State: " + pullState(doc) + "\n");
                    hostingResults.append("Country: " + pullCountry(doc) + "\n");
                    hostingResults.append("--------------------------\n\n");
                    sniperCounter++;
                }
                if (hostingMultipleRadio.isSelected() || hostingCsvRadio.isSelected()) {
                    for (int i = 0; i <= list.size() - 1; i++) {
                        String site = list.get(i);
                        String url = sniperSearch(site);
                        Document doc = sniperConnection(url, (i + 1), list.size());
                        hostingDomain.setText(site);
                        hostingResults.append("-----" + site + "-----\n");
                        hostingResults.append("IP: " + pullIP(doc) + "\n");
                        hostingResults.append("Host: " + pullHost(doc) + "\n");
                        hostingResults.append("City: " + pullCity(doc) + "\n");
                        hostingResults.append("State: " + pullState(doc) + "\n");
                        hostingResults.append("Country: " + pullCountry(doc) + "\n");
                        hostingResults.append("--------------------------\n\n");
                        sniperCounter++;
                    }
                }
            }

            if (hostingOutputCsv.isSelected()) {
                sniperFormat = "csv";
                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "InfoSniper " + date + ".csv"), "UTF8"));
                //write headers
                writer.append("Domain, IP, Host, Country, State, City\n");
                if (hostingSingleRadio.isSelected()) {
                    String site = hostingSingle.getText();
                    String url = sniperSearch(site);
                    Document doc = sniperConnection(url, 1, 1);
                    hostingDomain.setText(site);
                    writer.append(site + "," + pullIP(doc) + "," + pullHost(doc) + "," + pullCountry(doc) + "," + pullState(doc) + "," + pullCity(doc) + "\n");
                    sniperCounter++;
                }
                if (hostingMultipleRadio.isSelected() || hostingCsvRadio.isSelected()) {
                    for (int i = 0; i <= list.size() - 1; i++) {
                        String site = list.get(i);
                        String url = sniperSearch(site);
                        Document doc = sniperConnection(url, (i + 1), list.size());
                        hostingDomain.setText(site);
                        writer.append(site + "," + pullIP(doc) + "," + pullHost(doc) + "," + pullCountry(doc) + "," + pullState(doc) + "," + pullCity(doc) + "\n");
                        sniperCounter++;
                    }
                }
                writer.flush();
                writer.close();
            }
            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            endRunSniper(sniperCounter, sniperFormat, sniperPath);
        }
    }

    //InfoSniper processing
    class awesomeWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, IOException {
            resetStuff();

            if (awesomeDomain.isSelected()) {
                String domain = awesomeUrl.getText();

                if (awesomeAlexa.isSelected()) {
                    Document alexaDoc = awesomeConnection("Alexa", domain);
                    globalRankingOutputAwesome(alexaDoc, domain);
                    countryRankingOutputAwesome(alexaDoc, domain);
                }

                if (awesomeHosting.isSelected() || awesomeLocation.isSelected()) {
                    Document sniperDoc = awesomeConnection("Sniper", domain);

                    if (awesomeHosting.isSelected()) {
                        IPAddressOutputAwesome(sniperDoc, domain);
                        hostOutputAwesome(sniperDoc, domain);
                    }
                    if (awesomeLocation.isSelected()) {
                        countryOutputAwesome(sniperDoc, domain);
                        stateOutputAwesome(sniperDoc, domain);
                        cityOutputAwesome(sniperDoc, domain);
                    }

                }

                if (awesomeRegistrar.isSelected() || whoisCheck.isSelected()) {
                    Document regDoc = awesomeConnection("Whois", domain);

                    if (awesomeRegistrar.isSelected()) {
                        regNameOutputAwesome(regDoc, domain);
                        regUrlOutputAwesome(regDoc, domain);
                        regWhoisOutputAwesome(regDoc, domain);
                        regDateOutputAwesome(regDoc, domain);
                        regUpdateOutputAwesome(regDoc, domain);
                    }

                    if (whoisCheck.isSelected()) {
                        whoisOutputAwesome(regDoc, domain);
                    }
                }

            }
            if (awesomeCsvIn.isSelected()) {
                List<String> list = new ArrayList<>();
                list = generateUrlsFromCsv(awesomeCsvPath.getText());
                for (int i = 0; i <= list.size() - 1; i++) {
                    alexaSearchCounter = 0;
                    sniperSearchCounter = 0;
                    whoisSearchCounter = 0;

                    String domain = list.get(i);
                    update("Awesome", "(" + (i + 1) + "/" + list.size() + ") Scanning: " + domain);
                    Thread.sleep(sleepTime());
                    domainNames.add(domain);

                    if (awesomeAlexa.isSelected()) {
                        Document alexaDoc = awesomeConnection("Alexa", domain);
                        globalRankingCsvAwesome(alexaDoc, domain);
                        countryRankingCsvAwesome(alexaDoc, domain);
                    }

                    if (awesomeHosting.isSelected() || awesomeLocation.isSelected()) {
                        Document sniperDoc = awesomeConnection("Sniper", domain);

                        if (awesomeHosting.isSelected()) {
                            IPAddressCsvAwesome(sniperDoc, domain);
                            hostCsvAwesome(sniperDoc, domain);
                        }

                        if (awesomeLocation.isSelected()) {
                            countryCsvAwesome(sniperDoc, domain);
                            stateCsvAwesome(sniperDoc, domain);
                            cityCsvAwesome(sniperDoc, domain);
                        }
                    }

                    if (awesomeRegistrar.isSelected() || whoisCheck.isSelected()) {
                        Document regDoc = awesomeConnection("Whois", domain);
                        if (awesomeRegistrar.isSelected()) {
                            regNameCsvAwesome(regDoc, domain);
                            regUrlCsvAwesome(regDoc, domain);
                            regWhoisCsvAwesome(regDoc, domain);
                            regDateCsvAwesome(regDoc, domain);
                            regUpdateCsvAwesome(regDoc, domain);
                        }

                        if (whoisCheck.isSelected()) {
                            whoisCsvAwesome(regDoc, domain);
                        }
                    }
                }
            }

            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            if (siteExists.equals(true)) {
                checkPreview();
                if (preview.isSelected()) {
                    copyPreview(domainName, domainAlexaGlobal, domainAlexaUS, domainIP, domainHost,
                            domainCountry, domainState, domainCity, domainRegName, domainRegUrl, domainRegWhois,
                            domainRegistered, domainUpdated, domainWhois);
                }
                enableButtons();
                hyphens("Awesome");
                update("Awesome", "Done!");
                hyphens("Awesome");

                if (worth >= 3) {
                    awesomeCopy.setEnabled(true);
                    awesomeCsv.setEnabled(true);
                }
            } else {
                enableButtons();
            }
            update("Awesome", failedSites.size() + " Failed sites:\n");
            for (int i = 0; i <= failedSites.size() - 1; i++) {
                update("Awesome", failedSites.get(i));
            }
        }

    }
//------------------------------------------------------------------------------

//----------------------------Methods to open document--------------------------
    //Creates connection from url in string   
    public Document alexaConnection(String url, int index, int size) throws IOException, HttpStatusException, InterruptedException {
        while (!alexaFinished) {
            if (alexaWait.isSelected()) {
                Thread.sleep(sleepTime());
            }
            String site = url.replace("http://www.alexa.com/siteinfo/", "");

            update("Alexa", "(" + index + "/" + size + ") " + "Searching page: " + site);
            try {
                Connection.Response html = Jsoup.connect(url)
                        .userAgent(ua)
                        .timeout(0)
                        .execute();
                int statusCode = html.statusCode();

                if (statusCode == 200 && alexaFinished == false) {
                    update("Alexa", "Success... scraping data");
                    Document doc = (Document) Jsoup.connect(url)
                            .userAgent(ua)
                            .referrer("http://www.google.com")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();
                    return doc;
                } else {
                    if (statusCode == 200) {
                        alexaThread.cancel(true);
                    } else {
                        update("Alexa", url + " did not open successfully.  Error code: " + statusCode);
                    }
                }
            } catch (HttpStatusException ex) {
                update("Alexa", "Received error opening page.. trying again.");
                alexaConnection(url, index, size);
            }
        }
        return null;
    }

    //Creates connection from url in string   
    public Document sniperConnection(String url, int index, int size) throws IOException, HttpStatusException, InterruptedException {
        while (!sniperFinished) {
            if (sniperWait.isSelected()) {
                Thread.sleep(sleepTime());
            }
            String site = url.replace("http://www.infosniper.net/members.php?k=" + sniperKey + "&domain=", "");
            if (hostingSingleRadio.isSelected()) {
                update("Sniper", "Searching page: " + site);
            } else {
                update("Sniper", "(" + index + "/" + size + ") " + "Searching page: " + site);
            }
            try {

                if (sniperFinished == false) {
                    update("Sniper", "Success... scraping data");
                    Document doc = (Document) Jsoup.connect(url)
                            .userAgent(ua)
                            .referrer("http://www.google.com")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();
                    getRuns();
                    return doc;
                } else {
                }

            } catch (HttpStatusException ex) {
                update("Sniper", "Received error opening page.. trying again");
                sniperConnection(url, index, size);
            }
        }
        return null;
    }

    //Creates connection from url in string   
    public Document awesomeConnection(String tool, String domain) throws IOException, HttpStatusException, InterruptedException {
        domainName = domain;

        if (tool.equals("Alexa") && (alexaSearchCounter <= 2)) {
            try {
                update("Awesome", "Scanning Alexa Page...");
                Document doc = (Document) Jsoup.connect("http://www.alexa.com/siteinfo/" + domain)
                        .userAgent(ua)
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .timeout(0)
                        .get();
                getRuns();
                return doc;
            } catch (HttpStatusException ex) {
                update("Awesome", "Received error opening Alexa.. trying again");
                alexaSearchCounter++;
                failedSite = ("http://www.alexa.com/siteinfo/" + domain);
                awesomeConnection(tool, domain);
            }
        }

        if (tool.equals("Sniper") && (sniperSearchCounter <= 2)) {
            try {
                update("Awesome", "Scanning InfoSniper Page...");
                Document doc = (Document) Jsoup.connect("http://www.infosniper.net/members.php?k=" + sniperKey + "&domain=" + domain)
                        .userAgent(ua)
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .timeout(0)
                        .get();
                getRuns();
                return doc;
            } catch (HttpStatusException ex) {
                update("Awesome", "Received error opening InfoSniper.. trying again");
                sniperSearchCounter++;
                failedSite = ("http://www.infosniper.net/members.php?k=" + sniperKey + "&domain=" + domain);
                awesomeConnection(tool, domain);
            }
        }

        if (tool.equals("Whois") && (whoisSearchCounter <= 2)) {
            try {
                update("Awesome", "Scanning Whois Page...");
                Document doc = (Document) Jsoup.connect("https://who.is/whois/" + domain)
                        .userAgent(ua)
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .timeout(0)
                        .get();
                getRuns();
                return doc;
            } catch (HttpStatusException ex) {
                update("Awesome", "Received error opening WhoIs.. trying again");
                whoisSearchCounter++;
                failedSite = ("https://who.is/whois/" + domain);
                awesomeConnection(tool, domain);
            }
        }

        if (alexaSearchCounter == 3 || sniperSearchCounter == 3 || whoisSearchCounter == 3) {
            hyphens("Awesome");
            update("Awesome", "Error: Could not open page.");
            update("Awesome", "Please check the page manually: " + failedSite);
            hyphens("Awesome");
            update("Awesome", "Using Google for this one...");
            try {
                Document doc = (Document) Jsoup.connect("https://who.is/whois/google.com")
                        .userAgent(ua)
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .timeout(0)
                        .get();
                return doc;
            } catch (HttpStatusException ex) {
                update("Awesome", "Received error opening WhoIs.. trying again");
                whoisSearchCounter++;
                failedSite = ("https://who.is/whois/" + domain);
                awesomeConnection(tool, domain);
            }

        }

        return null;
    }
        //------------------------------------------------------------------------------

    //------------------------Global methods----------------------------------------
    //adds dashes to updateAlexa
    public void hyphens(String loc) {
        if (loc.equals("Progress")) {
            update("Alexa", "----------------------------------------------------");
        }
        if (loc.equals("Global")) {
            updateGlobal("---------------");
        }
        if (loc.equals("Country")) {
            updateCountry("---------------");
        }
        if (loc.equals("Sniper")) {
            update("Sniper", "----------------------------------------------------");
        }
        if (loc.equals("Awesome")) {
            update("Awesome", "----------------------------------------------------");
        }

    }

    //adds diamonds to output
    public void diamonds(String loc) {
        if (loc.equals("site")) {
            updateSite("          -");
            updateSite("         ---");
            updateSite("        -----");
            updateSite("       -------");
            updateSite("      ---------");
            updateSite("       -------");
            updateSite("        -----");
            updateSite("         ---");
            updateSite("          -");
        }

        if (loc.equals("global")) {
            updateGlobal("          -");
            updateGlobal("         ---");
            updateGlobal("        -----");
            updateGlobal("       -------");
            updateGlobal("      ---------");
            updateGlobal("       -------");
            updateGlobal("        -----");
            updateGlobal("         ---");
            updateGlobal("          -");
        }

        if (loc.equals("country")) {
            updateCountry("          -");
            updateCountry("         ---");
            updateCountry("        -----");
            updateCountry("       -------");
            updateCountry("      ---------");
            updateCountry("       -------");
            updateCountry("        -----");
            updateCountry("         ---");
            updateCountry("          -");
        }
    }

    //Disables buttons
    public void disableButtons() {
        runAlexa.setEnabled(false);
        csvOutput.setEnabled(false);
        displayResults.setEnabled(false);
        outPath.setEnabled(false);
        browseButtonAlexa.setEnabled(false);
        fromFile.setEnabled(false);
        globalRanking.setEnabled(false);
        countryRanking.setEnabled(false);
        csvFile.setEnabled(false);
        manual.setEnabled(false);

        siteOutput.setEditable(false);

        hostingSingleRadio.setEnabled(false);
        hostingMultipleRadio.setEnabled(false);
        hostingCsvRadio.setEnabled(false);
        hostingDisplay.setEnabled(false);
        hostingOutputCsv.setEnabled(false);
        hostingCsvOut.setEnabled(false);
        hostingBrowseInput.setEnabled(false);
        hostingBrowseOutput.setEnabled(false);
        hostingSingle.setEnabled(false);
        hostingMultiple.setEnabled(false);
        hostingCsvIn.setEnabled(false);
        hostingRun.setEnabled(false);

        awesomeCopy.setEnabled(false);
        awesomeCsv.setEnabled(false);
        awesomeRun.setEnabled(false);
        clearUpdates.setEnabled(false);
        clearResults.setEnabled(false);
        awesomeUrl.setEnabled(false);
        awesomeAlexa.setEnabled(false);
        awesomeHosting.setEnabled(false);
        awesomeRegistrar.setEnabled(false);
        whoisCheck.setEnabled(false);
        awesomeLocation.setEnabled(false);
        preview.setEnabled(false);

    }

    //Enables buttons
    public void enableButtons() {
        runAlexa.setEnabled(true);
        csvOutput.setEnabled(true);
        displayResults.setEnabled(true);
        outPath.setEnabled(true);
        browseButtonAlexa.setEnabled(true);
        fromFile.setEnabled(true);
        globalRanking.setEnabled(true);
        countryRanking.setEnabled(true);
        csvFile.setEnabled(true);
        manual.setEnabled(true);
        stopAlexa.setEnabled(true);

        if (manual.isSelected()) {
            siteOutput.setEditable(true);
        }

        hostingSingleRadio.setEnabled(true);
        hostingMultipleRadio.setEnabled(true);
        hostingCsvRadio.setEnabled(true);
        hostingDisplay.setEnabled(true);
        hostingOutputCsv.setEnabled(true);
        hostingCsvOut.setEnabled(true);
        hostingBrowseInput.setEnabled(true);
        hostingBrowseOutput.setEnabled(true);
        hostingSingle.setEnabled(true);
        hostingMultiple.setEnabled(true);
        hostingCsvIn.setEnabled(true);
        hostingRun.setEnabled(true);
        stopSniper.setEnabled(true);

        awesomeRun.setEnabled(true);
        clearUpdates.setEnabled(true);
        clearResults.setEnabled(true);
        awesomeUrl.setEnabled(true);
        awesomeAlexa.setEnabled(true);
        awesomeHosting.setEnabled(true);
        awesomeRegistrar.setEnabled(true);
        whoisCheck.setEnabled(true);
        awesomeLocation.setEnabled(true);
        preview.setEnabled(true);
        awesomeCsv.setEnabled(true);
    }

    //Formats search domains
    public String formatSearch(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("\\s+", "");
        return text;
    }

    //Returns random number between fields in Settings for thread sleep function
    public int sleepTime() {
        String preformatMin = waitMin.getText();
        String preformatMax = waitMax.getText();
        int min = Integer.parseInt(preformatMin);
        int max = Integer.parseInt(preformatMax);
        int waitTime = rn.nextInt((max - min) + 1) + min;
        if (alexaWait.isSelected()) {
            update("Alexa", "Waiting " + waitTime + " seconds...");
        }
        if (sniperWait.isSelected()) {
            update("Sniper", "Waiting " + waitTime + " seconds...");
        }
        if (awesomeWait.isSelected()) {
            update("Awesome", "Waiting " + waitTime + " seconds...");
        }

        return (waitTime * 1000);
    }
//------------------------------------------------------------------------------    

//----------------------------Methods to search---------------------------------   
    //Displays output for global ranking
    public void globalRankingOutput(Document doc, String site) {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            updateGlobal("Invalid site");
            update("Alexa", site + " returned no data.");
        } else {
            update("Alexa", "Pulling global rank...");
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String globalRank = global.text();
            updateGlobal(globalRank);
            alexaCounter++;
        }
    }

    //Displays output for US ranking
    public void countryRankingOutput(Document doc, String site) {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            updateCountry("Invalid site");
            update("Alexa", site + " returned no data.");
        } else {
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String global1 = global.text();
            Boolean hasText = false;

            for (Element e : doc.select("td")) {
                if (e.text().contains("United States")) {
                    hasText = true;
                }
            }
            if (global1.equals("-")) {
                updateCountry("N/A");
            } else {
                if (hasText.equals(true)) {
                    Boolean runOnce = false;
                    for (Element e : doc.select("td")) {
                        if (e.text().contains("United States") && runOnce == false) {
                            update("Alexa", "Pulling US rank...");
                            Element r = e.nextElementSibling().nextElementSibling();
                            updateCountry(r.text());
                            runOnce = true;
                        }
                    }
                } else {
                    updateCountry("No data available.");
                    update("Alexa", site + " returned no US rank.");
                }
            }
        }

    }

    //----------------AWESOME OUTPUTS-------------------------------------------
    //Displays output for global ranking
    public void globalRankingOutputAwesome(Document doc, String site) {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            awesomeGlobal.setText("Invalid domain");
            update("Awesome", "Alexa (Global) - " + site + " returned no data.");
        } else {
            update("Awesome", "Alexa - Retrieving Global Rank...");
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String globalRank = global.text();
            awesomeGlobal.setText(globalRank);
            domainAlexaGlobal = globalRank;
        }

    }

    //Displays output for US ranking
    public void countryRankingOutputAwesome(Document doc, String site) {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            awesomeUS.setText("Invalid domain");
            update("Awesome", "Alexa (US) - " + site + " returned no data.");
        } else {
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String global1 = global.text();
            Boolean hasText = false;

            for (Element e : doc.select("td")) {
                if (e.text().contains("United States")) {
                    hasText = true;
                }
            }
            if (global1.equals("-")) {
                update("Awesome", "Alexa - No US score for this domain");
                awesomeUS.setText("N/A");
            } else {
                if (hasText.equals(true)) {
                    Boolean runOnce = false;
                    for (Element e : doc.select("td")) {
                        if (e.text().contains("United States") && runOnce == false) {
                            update("Awesome", "Alexa - Retrieving US Rank...");
                            Element r = e.nextElementSibling().nextElementSibling();
                            String usRank = r.text();
                            awesomeUS.setText(usRank);
                            domainAlexaUS = usRank;
                            runOnce = true;
                        }
                    }
                } else {
                    awesomeUS.setText("-");
                    update("Awesome", site + " returned no US rank.");
                }
            }
        }

    }

    //Displays output for IP Address
    public void IPAddressOutputAwesome(Document doc, String site) {
        Element IP = doc.select(".content-td2").get(0);
        String address = IP.childNode(0).toString();
        address = sniperClean(address);
        update("Awesome", "InfoSniper - Retrieving IP Address...");
        domainIP = address;
        if (domainIP != null) {
            awesomeIP.setText(address);
        } else {
            awesomeIP.setText("N/A");
        }
    }

    //Displays output for Host
    public void hostOutputAwesome(Document doc, String site) {
        Element host = doc.select(".content-td2").get(4);
        String hostS = host.childNode(0).toString();
        hostS = sniperClean(hostS);
        update("Awesome", "InfoSniper - Retrieving Host...");
        domainHost = hostS;
        if (domainHost != null) {
            awesomeHost.setText(hostS);
        } else {
            awesomeHost.setText("N/A");
        }
    }

    //Displays output for Country
    public void countryOutputAwesome(Document doc, String site) {
        Element country = doc.select(".content-td2").get(9);
        String countryS = country.text();
        countryS = sniperClean(countryS);
        update("Awesome", "InfoSniper - Retrieving Country...");
        domainCountry = countryS;
        if (countryS != null) {
            awesomeCountry.setText(countryS);
        } else {
            awesomeCountry.setText("N/A");
        }
    }

    //Displays output for State
    public void stateOutputAwesome(Document doc, String site) {
        Element state = doc.select(".content-td2").get(5);
        String stateS = state.text();
        stateS = sniperClean(stateS);
        update("Awesome", "InfoSniper - Retrieving State...");
        domainState = stateS;
        if (stateS != null) {
            awesomeState.setText(stateS);
        } else {
            awesomeState.setText("N/A");
        }
    }

    //Displays output for City
    public void cityOutputAwesome(Document doc, String site) {
        Element city = doc.select(".content-td2").get(1);
        String cityS = city.text();
        cityS = sniperClean(cityS);
        update("Awesome", "InfoSniper - Retrieving City...");
        domainCity = cityS;
        if (domainCity != null) {
            awesomeCity.setText(cityS);
        } else {
            awesomeCity.setText("N/A");
        }
    }

    //Displays output for Registrar Name
    public void regNameOutputAwesome(Document doc, String site) {
        String name = null;
        Elements span = doc.select("span");
        int size = span.size();
        for (int i = 0; i <= size - 1; i++) {
            if (span.get(i).outerHtml().contains("registrar_name")) {
                name = span.get(i).outerHtml();
                name = name.replace("<span data-bind-domain=\"registrar_name\">", "");
                name = name.replace("</span>", "");
            }
        }
        if (name == null) {
            update("Awesome", "Whois (Name) - No information available...");
            awesomeRegName.setText("N/A");
        } else {
            update("Awesome", "Whois - Retrieving Registrar Name...");
            domainRegName = name;
            awesomeRegName.setText(name);
        }
    }

    //Displays output for Registrar URL
    public void regUrlOutputAwesome(Document doc, String site) {
        String url = null;
        Boolean runOnce = false;
        Elements span = doc.select("span");
        int size = span.size();
        for (int i = 0; i <= size - 1; i++) {
            if (span.get(i).outerHtml().contains("referral_url") && (runOnce == false)) {
                url = span.get(i).outerHtml();
                url = url.replace("<span data-bind-domain=\"referral_url\">", "");
                url = url.replace("</span>", "");
                runOnce = true;
            }
        }
        if (url == null) {
            update("Awesome", "Whois (Referral URL) - No information available...");
            awesomeRegUrl.setText("N/A");
        } else {
            update("Awesome", "Whois - Retrieving Referral URL...");
            domainRegUrl = url;
            awesomeRegUrl.setText(url);
        }
    }

    //Displays output for Registrar Updated date
    public void regWhoisOutputAwesome(Document doc, String site) {
        String whoisurl = null;
        Elements span = doc.select("span");
        int size = span.size();
        for (int i = 0; i <= size - 1; i++) {
            if (span.get(i).outerHtml().contains("whois_server_services_url")) {
                whoisurl = span.get(i).outerHtml();
                whoisurl = whoisurl.replace("<span data-bind-domain=\"whois_server_services_url\">", "");
                whoisurl = whoisurl.replace("</span>", "");
            }
        }
        if (whoisurl == null) {
            update("Awesome", "Whois (Whois URL) - No information available...");
            awesomeWhois.setText("N/A");
        } else {
            update("Awesome", "Whois - Retrieving Whois Service URL...");
            domainRegWhois = whoisurl;
            awesomeWhois.setText(whoisurl);
        }
    }

    //Displays output for Registrar Register date
    public void regDateOutputAwesome(Document doc, String site) {
        Elements th = doc.select("th");
        int size = th.size();
        String registered = null;

        for (int i = 0; i <= size - 1; i++) {
            if (th.get(i).text().equals("Registered On")) {
                registered = th.get(i).nextElementSibling().text();
            }
        }
        if (registered == null) {
            update("Awesome", "Whois (Registration Date) - No information available...");
            awesomeRegDate.setText("N/A");
        } else {
            update("Awesome", "Whois - Retrieving Registration Date...");
            domainRegistered = registered;
            domainRegistered = domainRegistered.replace(",", "");
            awesomeRegDate.setText(registered);
        }
    }

    //Displays output for Registrar Updated date
    public void regUpdateOutputAwesome(Document doc, String site) {
        Elements th = doc.select("th");
        int size = th.size();
        String updated = null;

        for (int i = 0; i <= size - 1; i++) {
            if (th.get(i).text().equals("Updated On")) {
                updated = th.get(i).nextElementSibling().text();
            }
        }
        if (updated == null) {
            update("Awesome", "Whois (Last Whois Update) - No information available...");
            awesomeRegUpdated.setText("N/A");
        } else {
            update("Awesome", "Whois - Retrieving whois last updated Date...");
            domainUpdated = updated;
            domainUpdated = domainUpdated.replace(",", "");
            awesomeRegUpdated.setText(updated);
        }
    }

    //Displays output for Whois
    public void whoisOutputAwesome(Document doc, String site) {
        String allText = "";
        String rawData = null;
        whoisText.setText("");
        Elements raw = doc.select(".raw_data");

        if (!raw.isEmpty()) {
            update("Awesome", "Located raw Whois data... Retrieving...");
            rawData = raw.html();
            rawData = whoisClean(rawData);
            allText = allText.concat(rawData);
            whoisText.append(rawData);
            whoisText.append("----------------------------------------------------\n");
        }

        Element raw1 = doc.select(".registrar-information.domain-data").first();
        if (!raw1.outerHtml().contains("registrar_name")) {
            update("Awesome", "Located registrar information Whois data... Retrieving...");
            String rawt = raw1.html();
            rawt = whoisClean(rawt);
            allText = allText.concat("\n" + rawt);
            whoisText.append(rawt);
            whoisText.append("----------------------------------------------------\n");
        }
        domainWhois = allText;

    }

    //Displays output for global ranking
    public void globalRankingCsvAwesome(Document doc, String site) {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            domainGlobals.add("No info available..");
            update("Awesome", "Alexa (Global) - " + site + " returned no data.");
        } else {
            update("Awesome", "Alexa - Retrieving Global Rank...");
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String globalRank = global.text();
            domainGlobals.add(globalRank);
            domainAlexaGlobal = globalRank;
        }

    }

    //Displays output for US ranking
    public void countryRankingCsvAwesome(Document doc, String site) {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            domainUSes.add("No info available..");
            update("Awesome", "Alexa (US) - " + site + " returned no data.");
        } else {
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String global1 = global.text();
            Boolean hasText = false;

            for (Element e : doc.select("td")) {
                if (e.text().contains("United States")) {
                    hasText = true;
                }
            }
            if (global1.equals("-")) {
                update("Awesome", "Alexa - No US score for this domain");
                domainUSes.add("No info available..");
            } else {
                if (hasText.equals(true)) {
                    Boolean runOnce = false;
                    for (Element e : doc.select("td")) {
                        if (e.text().contains("United States") && runOnce == false) {
                            update("Awesome", "Alexa - Retrieving US Rank...");
                            Element r = e.nextElementSibling().nextElementSibling();
                            String usRank = r.text();
                            domainUSes.add(usRank);
                            domainAlexaUS = usRank;
                            runOnce = true;
                        }
                    }
                } else {
                    awesomeUS.setText("-");
                    update("Awesome", site + " returned no US rank.");
                    domainUSes.add("No US rank available..");
                }
            }
        }

    }

    //Displays output for IP Address
    public void IPAddressCsvAwesome(Document doc, String site) {
        Element IP = doc.select(".content-td2").get(0);
        String address = IP.childNode(0).toString();
        address = sniperClean(address);
        update("Awesome", "InfoSniper - Retrieving IP Address...");
        domainIP = address;
        if (domainIP != null) {
            domainIPs.add(address);
        } else {
            domainIPs.add("No info available..");
        }
    }

    //Displays output for Host
    public void hostCsvAwesome(Document doc, String site) {
        Element host = doc.select(".content-td2").get(4);
        String hostS = host.childNode(0).toString();
        hostS = sniperClean(hostS);
        update("Awesome", "InfoSniper - Retrieving Host...");
        domainHost = hostS;
        if (domainHost != null) {
            domainHosts.add(hostS);
        } else {
            domainHosts.add("No info available..");
        }
    }

    //Displays output for Country
    public void countryCsvAwesome(Document doc, String site) {
        Element country = doc.select(".content-td2").get(9);
        String countryS = country.text();
        countryS = sniperClean(countryS);
        update("Awesome", "InfoSniper - Retrieving Country...");
        domainCountry = countryS;
        if (countryS != null) {
            domainCountries.add(countryS);
        } else {
            domainCountries.add("No info available..");
        }
    }

    //Displays output for State
    public void stateCsvAwesome(Document doc, String site) {
        Element state = doc.select(".content-td2").get(5);
        String stateS = state.text();
        stateS = sniperClean(stateS);
        update("Awesome", "InfoSniper - Retrieving State...");
        domainState = stateS;
        if (stateS != null) {
            domainStates.add(stateS);
        } else {
            domainStates.add("No info available..");
        }
    }

    //Displays output for City
    public void cityCsvAwesome(Document doc, String site) {
        Element city = doc.select(".content-td2").get(1);
        String cityS = city.text();
        cityS = sniperClean(cityS);
        update("Awesome", "InfoSniper - Retrieving City...");
        domainCity = cityS;
        if (domainCity != null) {
            domainCities.add(cityS);
        } else {
            domainCities.add("No info available..");
        }
    }

    //Displays output for Registrar Name
    public void regNameCsvAwesome(Document doc, String site) {
        String name = null;
        Elements span = doc.select("span");
        int size = span.size();
        for (int i = 0; i <= size - 1; i++) {
            if (span.get(i).outerHtml().contains("registrar_name")) {
                name = span.get(i).outerHtml();
                name = name.replace("<span data-bind-domain=\"registrar_name\">", "");
                name = name.replace("</span>", "");
                name = name.replace(",", "");
            }
        }
        if (name == null) {
            update("Awesome", "Whois (Name) - No information available...");
            domainRegNames.add("No info available..");
        } else {
            update("Awesome", "Whois - Retrieving Registrar Name...");
            domainRegNames.add(name);
        }
    }

    //Displays output for Registrar URL
    public void regUrlCsvAwesome(Document doc, String site) {
        String url = null;
        Boolean runOnce = false;
        Elements span = doc.select("span");
        int size = span.size();
        for (int i = 0; i <= size - 1; i++) {
            if (span.get(i).outerHtml().contains("referral_url") && (runOnce == false)) {
                url = span.get(i).outerHtml();
                url = url.replace("<span data-bind-domain=\"referral_url\">", "");
                url = url.replace("</span>", "");
                url = url.replace(",", "");
                runOnce = true;
            }
        }
        if (url == null) {
            update("Awesome", "Whois (Referral URL) - No information available...");
            domainRegUrls.add("No info available..");
        } else {
            update("Awesome", "Whois - Retrieving Referral URL...");
            domainRegUrls.add(url);
        }
    }

    //Displays output for Registrar Updated date
    public void regWhoisCsvAwesome(Document doc, String site) {
        String whoisurl = null;
        Elements span = doc.select("span");
        int size = span.size();
        for (int i = 0; i <= size - 1; i++) {
            if (span.get(i).outerHtml().contains("whois_server_services_url")) {
                whoisurl = span.get(i).outerHtml();
                whoisurl = whoisurl.replace("<span data-bind-domain=\"whois_server_services_url\">", "");
                whoisurl = whoisurl.replace("</span>", "");
                whoisurl = whoisurl.replace(",", "");
            }
        }
        if (whoisurl == null) {
            update("Awesome", "Whois (Whois URL) - No information available...");
            domainRegWhoises.add("No info available..");
        } else {
            update("Awesome", "Whois - Retrieving Whois Service URL...");
            domainRegWhoises.add(whoisurl);
        }
    }

    //Displays output for Registrar Register date
    public void regDateCsvAwesome(Document doc, String site) {
        Elements th = doc.select("th");
        int size = th.size();
        String registered = null;

        for (int i = 0; i <= size - 1; i++) {
            if (th.get(i).text().equals("Registered On")) {
                registered = th.get(i).nextElementSibling().text();
            }
        }
        if (registered == null) {
            update("Awesome", "Whois (Registration Date) - No information available...");
            domainRegistereds.add("No info available..");
        } else {
            update("Awesome", "Whois - Retrieving Registration Date...");
            registered = registered.replace(",", "");
            domainRegistereds.add(registered);
        }
    }

    //Displays output for Registrar Updated date
    public void regUpdateCsvAwesome(Document doc, String site) {
        Elements th = doc.select("th");
        int size = th.size();
        String updated = null;

        for (int i = 0; i <= size - 1; i++) {
            if (th.get(i).text().equals("Updated On")) {
                updated = th.get(i).nextElementSibling().text();
            }
        }
        if (updated == null) {
            update("Awesome", "Whois (Last Whois Update) - No information available...");
            domainUpdateds.add("No info available..");
        } else {
            update("Awesome", "Whois - Retrieving whois last updated Date...");
            updated = updated.replace(",", "");
            domainUpdateds.add(updated);
        }
    }

    //Displays output for Whois
    public void whoisCsvAwesome(Document doc, String site) {
        String allText = "";
        String rawData = null;
        whoisText.setText("");
        Elements raw = doc.select(".raw_data");

        if (!raw.isEmpty()) {
            update("Awesome", "Located raw Whois data... Retrieving...");
            rawData = raw.html();
            rawData = whoisClean(rawData);
            allText = allText.concat(rawData);
        }

        Element raw1 = doc.select(".registrar-information.domain-data").first();
        if (!raw1.outerHtml().contains("registrar_name")) {
            update("Awesome", "Located registrar information Whois data... Retrieving...");
            String rawt = raw1.html();
            rawt = whoisClean(rawt);
            allText = allText.concat("\n" + rawt);
        }
        domainWhoisStuff.add(allText);

    }

    public String whoisClean(String text) {
        text = text.replace("<span data-bind-domain=\"raw_registrar_lookup\"> ", "");
        text = text.replace("<br>", "\n");
        text = text.replace("<span>", "");
        text = text.replace("</span>", "");
        text = text.replace("&nbsp;", "");
        text = text.replace("'", "\\'");
        text = text.replaceAll("\"[^\"]*\"", "");
        text = text.replace("Registrar Info", "");
        text = text.replace("&gt;", "");
        text = text.replace("<header> ", "");
        text = text.replace(" <!-- span title= rel= class=></span --> ", "");
        text = text.replace(" <span title= rel= class= style=> ", "");
        text = text.replace(" <h5></h5> ", "");
        text = text.replace("</header> ", "");
        text = text.replace("<table> ", "");
        text = text.replace(" <tbody> ", "");
        text = text.replace("  <tr>", "");
        text = text.replace("   <td colspan=> ", "");
        text = text.replace(" </td>", "");
        text = text.replace("  </tr> ", "");
        text = text.replace(" </tbody>", "");
        text = text.replace("</table>", "");
        text = text.replace("<img alt= src= class=>", "[HIDDEN]");

        return text;
    }

    public void checkPreview() {
        int counter = 0;
        hyphens("Awesome");
        update("Awesome", "------------------FORMAT CHECK------------------");
        update("Awesome", "Finding missing values...");

        if (domainAlexaGlobal == null) {
            update("Awesome", "-- No Global Alexa rank found");
            counter++;
        }
        if (domainAlexaUS == null) {
            update("Awesome", "-- No US Alexa rank found");
            counter++;
        }
        if (domainIP == null) {
            update("Awesome", "-- No IP Address found");
            counter++;
        }
        if (domainHost == null) {
            update("Awesome", "-- No Host found");
            counter++;
        }
        if (domainCountry == null || domainCountry == "n/a ()" || domainCountry == "n/a") {
            domainCountry = null;
            update("Awesome", "-- No country found");
            counter++;
        }
        if (domainCity == null || domainCity == "n/a () " || domainCity == "n/a") {
            domainCity = null;
            update("Awesome", "-- No city found");
            counter++;
        }
        if (domainState == null || domainState == "n/a ()" || domainState == "n/a") {
            domainState = null;
            update("Awesome", "-- No state found");
            counter++;
        }
        if (domainRegName == null) {
            update("Awesome", "-- No Registrar name found");
            counter++;
        }
        if (domainRegUrl == null) {
            update("Awesome", "-- No Registrar Referral URL found");
            counter++;
        }
        if (domainRegWhois == null) {
            update("Awesome", "-- No Registrar Whois Server found");
            counter++;
        }
        if (domainRegistered == null) {
            update("Awesome", "-- No Registration Date found");
            counter++;
        }
        if (domainUpdated == null) {
            update("Awesome", "-- No Last Updated Date found");
            counter++;
        }
        if (domainWhois == null) {
            update("Awesome", "-- No Whois data found");
            counter++;
        }

        update("Awesome", counter + " missing value(s) found.");
        hyphens("Awesome");
    }

    public void resetStuff() {
        domainName = null;
        domainAlexaGlobal = null;
        domainAlexaUS = null;
        domainIP = null;
        domainHost = null;
        domainCountry = null;
        domainCity = null;
        domainState = null;
        domainRegName = null;
        domainRegUrl = null;
        domainRegWhois = null;
        domainRegistered = null;
        domainUpdated = null;
        domainWhois = null;

        alexaSearchCounter = 0;
        sniperSearchCounter = 0;
        whoisSearchCounter = 0;

        siteExists = true;
        limitOneError = false;
    }

    public Boolean checkSite(String domain, Document doc) throws IOException {
        if (alexaSearchCounter <= 2) {
            Element title = doc.select("title").first();
            if (title.text().equals("Alexa Site Overview")) {
                return false;
            } else {
                Element global = doc.select(".metrics-data.align-vmiddle").first();
                String globalRank = global.text();
                if (globalRank.equals("-")) {
                    return false;
                }
            }
        }
        currentSite.setText(domain);
        return true;
    }

    public void awesomeCsv() {
        AwesomeCsvOut.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = AwesomeCsvOut.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = AwesomeCsvOut.getSelectedFile();

            String path = file.getAbsolutePath() + "/";
            File dir = new File(path);
            if (!dir.exists()) {
                update("Awesome", "Creating output folder: " + path);
                dir.mkdir();
            }
            if (awesomeDomain.isSelected()) {
                try {
                    Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + " AwesomeSearch " + domainName + "-" + date + ".csv"), "UTF8"));
                    //Headers
                    if (domainName != null) {
                        writer.append("Domain,");
                    }

                    if (domainIP != null) {
                        writer.append("IP Address,");
                    }

                    if (domainAlexaGlobal != null) {
                        writer.append("Global Alexa Rank,");
                    }

                    if (domainAlexaUS != null) {
                        writer.append("US Alexa Rank,");
                    }

                    if (domainRegName != null) {
                        writer.append("Registrar Name,");
                    }

                    if (domainRegUrl != null) {
                        writer.append("Registrar Referral URL,");
                    }

                    if (domainRegWhois != null) {
                        writer.append("Registrar Whois Service URL,");
                    }

                    if (domainHost != null) {
                        writer.append("Host,");
                    }

                    if (domainCountry != null) {
                        writer.append("Country,");
                    }

                    if (domainState != null) {
                        writer.append("State,");
                    }

                    if (domainCity != null) {
                        writer.append("City,");
                    }

                    if (domainRegistered != null) {
                        writer.append("Registration Date,");
                    }

                    if (domainUpdated != null) {
                        writer.append("Last Updated,");
                    }

                    writer.append("\n");

                    //Content
                    if (domainName != null) {
                        writer.append(domainName + ",");
                    }

                    if (domainIP != null) {
                        writer.append(domainIP + ",");
                    }

                    if (domainAlexaGlobal != null) {
                        writer.append(domainAlexaGlobal + ",");
                    }

                    if (domainAlexaUS != null) {
                        writer.append(domainAlexaUS + ",");
                    }

                    if (domainRegName != null) {
                        writer.append(domainRegName + ",");
                    }

                    if (domainRegUrl != null) {
                        writer.append(domainRegUrl + ",");
                    }

                    if (domainRegWhois != null) {
                        writer.append(domainRegWhois + ",");
                    }

                    if (domainHost != null) {
                        writer.append(domainHost + ",");
                    }

                    if (domainCountry != null) {
                        writer.append(domainCountry + ",");
                    }

                    if (domainState != null) {
                        writer.append(domainState + ",");
                    }

                    if (domainCity != null) {
                        writer.append(domainCity + ",");
                    }

                    if (domainRegistered != null) {
                        writer.append(domainRegistered + ",");
                    }

                    if (domainUpdated != null) {
                        writer.append(domainUpdated + ",");
                    }

                    if (domainWhois != null) {
                        writer.append("\n\n\nWhois:\n");
                        writer.append(domainWhois);
                    }

                    hyphens("Awesome");
                    update("Awesome", "File saved as: " + path + " AwesomeSearch " + domainName + "-" + date + ".csv");
                    hyphens("Awesome");

                    writer.flush();
                    writer.close();
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (awesomeCsvIn.isSelected()) {
                try {
                    Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + " AwesomeSearches " + date + ".csv"), "UTF8"));
                    //Headers
                    writer.append("Domain,");

                    if (awesomeHosting.isSelected()) {
                        writer.append("IP Address,");
                    }

                    if (awesomeAlexa.isSelected()) {
                        writer.append("Global Alexa Rank,");
                        writer.append("US Alexa Rank");
                    }

                    if (awesomeHosting.isSelected()) {
                        writer.append("Host,");
                    }

                    if (awesomeLocation.isSelected()) {
                        writer.append("Country,");
                        writer.append("State,");
                        writer.append("City,");
                    }

                    if (awesomeRegistrar.isSelected()) {
                        writer.append("Domain,");
                        writer.append("Registrar Name,");
                        writer.append("Registrar Referral URL,");
                        writer.append("Registrar Whois Service URL,");
                        writer.append("Registration Date,");
                        writer.append("Last Updated,");
                    }

                    writer.append("\n");

                    //Content
                    for (int i = 0; i <= domainNames.size() - 1; i++) {
                        if (domainNames.get(i) != null) {
                            writer.append(domainNames.get(i) + ",");
                        }

                        if (!domainIPs.isEmpty()) {
                            writer.append(domainIPs.get(i) + ",");
                        }

                        if (!domainGlobals.isEmpty()) {
                            writer.append(domainGlobals.get(i) + ",");
                        } else {

                        }

                        if (!domainUSes.isEmpty()) {
                            writer.append(domainUSes.get(i) + ",");
                        } else {

                        }
                        if (!domainRegNames.isEmpty()) {
                            writer.append(domainRegNames.get(i) + ",");
                        } else {

                        }

                        if (!domainRegUrls.isEmpty()) {
                            writer.append(domainRegUrls.get(i) + ",");
                        } else {

                        }

                        if (!domainRegWhoises.isEmpty()) {
                            writer.append(domainRegWhoises.get(i) + ",");
                        } else {

                        }

                        if (!domainHosts.isEmpty()) {
                            writer.append(domainHosts.get(i) + ",");
                        } else {

                        }

                        if (!domainCountries.isEmpty()) {
                            writer.append(domainCountries.get(i) + ",");
                        } else {

                        }

                        if (!domainStates.isEmpty()) {
                            writer.append(domainStates.get(i) + ",");
                        } else {

                        }

                        if (!domainCities.isEmpty()) {
                            writer.append(domainCities.get(i) + ",");
                        } else {

                        }

                        if (!domainRegistereds.isEmpty()) {
                            writer.append(domainRegistereds.get(i) + ",");
                        } else {

                        }

                        if (!domainUpdateds.isEmpty()) {
                            writer.append(domainUpdateds.get(i) + ",");
                        } else {

                        }

                        writer.append("\n");

                        if (!domainWhoisStuff.isEmpty()) {
                            writer.append("Whois:\n");
                            writer.append(domainWhoisStuff.get(i));
                        } else {

                        }
                    }

                    hyphens("Awesome");
                    update("Awesome", "File saved as: " + path + " AwesomeSearches " + date + ".csv");
                    hyphens("Awesome");

                    writer.flush();
                    writer.close();
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public void copy(String name, String global, String us, String IP, String host,
            String country, String state, String city, String rname,
            String rurl, String rwurl, String registered, String updated, String whois) {

        StringBuilder builder = new StringBuilder();

        if (name != null) {
            builder.append("Domain Information for: " + name + "\n");
        }

        if (IP != null) {
            builder.append("IP Address: " + IP + "\n");
        }

        if (registered != null) {
            builder.append("Registered on: " + registered + "\n");
        }

        if (global != null) {
            builder.append("Alexa Global Rank: " + global + "\n");
        }

        if (us != null) {
            builder.append("Alexa US Rank: " + us + "\n");
        }

        if (rname != null) {
            builder.append("Registrar: " + rname + "\n");
        }

        if (rurl != null) {
            builder.append("Registrar: " + rurl + "\n");
        }

        if (rwurl != null) {
            builder.append("Registrar: " + rwurl + "\n");
        }

        if (host != null) {
            builder.append("Host: " + host + "\n");
        }

        if (country != null) {
            builder.append("Country: " + country + "\n");
        }

        if (state != null) {
            builder.append("State: " + state + "\n");
        }

        if (city != null) {
            builder.append("City: " + city + "\n");
        }

        if (updated != null) {
            builder.append("Domain WhoIs Information last updated: " + updated + "\n");
        }

        if (whois != null) {
            builder.append("Whois:\n");
            builder.append(whois);
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection strSel = new StringSelection(builder.toString());
        clipboard.setContents(strSel, null);
    }

    public void copyPreview(String name, String global, String us, String IP, String host,
            String country, String state, String city, String rname,
            String rurl, String rwurl, String registered, String updated, String whois) {
        hyphens("Awesome");
        update("Awesome", "Copy/Paste Format Preview:");
        hyphens("Awesome");

        if (name != null) {
            update("Awesome", "Domain Information for: " + name);
        }

        if (IP != null) {
            update("Awesome", "IP Address: " + IP);
            worth++;
        }

        if (registered != null) {
            update("Awesome", "Registered on: " + registered);
            worth++;
        }

        if (global != null) {
            update("Awesome", "Alexa Global Rank: " + global);
            worth++;
        }

        if (us != null) {
            update("Awesome", "Alexa US Rank: " + us);
            worth++;
        }

        if (rname != null) {
            update("Awesome", "Registrar: " + rname);
            worth++;
        }

        if (rurl != null) {
            update("Awesome", "Registrar: " + rurl);
            worth++;
        }

        if (rwurl != null) {
            update("Awesome", "Registrar: " + rwurl);
            worth++;
        }

        if (host != null) {
            update("Awesome", "Host: " + host);
            worth++;
        }

        if (country != null) {
            update("Awesome", "Country: " + country);
            worth++;
        }

        if (state != null) {
            update("Awesome", "State: " + state);
            worth++;
        }

        if (city != null) {
            update("Awesome", "City: " + city);
            worth++;
        }

        if (updated != null) {
            update("Awesome", "Domain WhoIs Information last updated: " + updated);
            worth++;
        }

        if (whois != null) {
            update("Awesome", "Whois:");
            update("Awesome", whois);
        }

        hyphens("Awesome");
        hyphens("Awesome");

    }
    //--------------------------------------------------------------------------
    //Writes global ranking to csv file

    public void globalRankingCsv(Document doc, FileWriter writer, String site) throws IOException {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            writer.append("N/A,");
            update("Alexa", site + " returned no available data.");
        } else {
            update("Alexa", "Pulling global rank...");
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String text = global.text();
            text = text.replace(",", "");
            writer.append(text + ",");
            alexaCounter++;
        }

    }

    //writes US ranking to csv file
    public void countryRankingCsv(Document doc, FileWriter writer, String site) throws IOException {
        Element title = doc.select("title").first();

        if (title.text().equals("Alexa Site Overview")) {
            writer.append("N/A,");
            update("Alexa", site + " returned no data.");
        } else {
            Element global = doc.select(".metrics-data.align-vmiddle").first();
            String global1 = global.text();
            Boolean hasText = false;

            for (Element e : doc.select("td")) {
                if (e.text().contains("United States")) {
                    hasText = true;
                }
            }
            if (global1.equals("-")) {
                writer.append("N/A");
            } else {
                if (hasText.equals(true)) {
                    Boolean runOnce = false;
                    for (Element e : doc.select("td")) {
                        if (e.text().contains("United States") && runOnce == false) {
                            update("Alexa", "Pulling US rank...");
                            Element r = e.nextElementSibling().nextElementSibling();
                            String text = r.text();
                            text = text.replace(",", "");
                            writer.append(text);
                            runOnce = true;
                        }
                    }
                } else {
                    writer.append("No data available.");
                    update("Alexa", site + " returned no US rank.");
                }
            }
        }

    }
//------------------------------------------------------------------------------

//------------------------Methods to generate lists-----------------------------
    //Generates list of urls from selected csv file
    public List<String> generateUrlsFromCsv(String source) throws FileNotFoundException, IOException {
        List<String> UrlList = new ArrayList<>();
        BufferedReader CSVFile = new BufferedReader(new FileReader(source));
        String UrlRows = CSVFile.readLine();

        while (UrlRows != null) {
            String formated = formatSearch(UrlRows);
            UrlList.add(formated);
            UrlRows = CSVFile.readLine();
        }

        return UrlList;
    }

    //Generates list of urls from manual input
    public List<String> generateUrlsFromManual() throws FileNotFoundException, IOException {
        List<String> UrlList = new ArrayList<>();
        String[] stringList = siteOutput.getText().split("\\n");

        for (int i = 0; i <= stringList.length - 1; i++) {
            String formated = formatSearch(stringList[i]);
            UrlList.add(formated);
        }

        siteOutput.setText("");

        for (int i = 0; i <= UrlList.size() - 1; i++) {
            siteOutput.append(UrlList.get(i) + "\n");
        }

        return UrlList;
    }
//------------------------------------------------------------------------------

    //Provides updates to UI
    public void update(String loc, String text) {
        if (loc.equals("Alexa")) {
            currentStatusAlexa.append(text + "\n");
            int len = currentStatusAlexa.getDocument().getLength();
            currentStatusAlexa.setCaretPosition(len);
        }

        if (loc.equals("Sniper")) {
            hostingUpdates.append(text + "\n");
            int len = hostingUpdates.getDocument().getLength();
            hostingUpdates.setCaretPosition(len);
        }

        if (loc.equals("Awesome")) {
            awesomeUpdate.append(text + "\n");
            int len = awesomeUpdate.getDocument().getLength();
            awesomeUpdate.setCaretPosition(len);
        }

    }
//------------------------------------------------------------------------------

//------------------------Alexa Methods-----------------------------------------   
    //Enables buttons and returns report
    public void endRunAlexa(int counter, String format, String path) {
        enableButtons();
        hyphens("Progress");
        update("Alexa", "Done!");
        update("Alexa", counter + " sites returned valid data.");

        if (format.equals("csv")) {
            update("Alexa", "File saved as " + path);
        }

        if (format.equals("output")) {
            update("Alexa", "Please see output ------------->");
        }

        hyphens("Progress");
    }

    //Stops Alexa processing
    public void stopAlexa() {
        alexaFinished = true;
        stopAlexa.setEnabled(false);
        update("Alexa", "Stopping!");
    }

    //Returns Alexa search string
    public String alexaSearch(String search) {
        String url = null;

        url = "http://www.alexa.com/siteinfo/" + search;

        return url;
    }
//------------------------------------------------------------------------------

//------------------------InfoSniper (hosting) methods--------------------------
    //Generates list of urls from selected csv file
    public List<String> generateUrlsFromCsvSniper() throws FileNotFoundException, IOException {
        List<String> UrlList = new ArrayList<>();
        BufferedReader CSVFile = new BufferedReader(new FileReader(hostingCsvIn.getText()));
        String UrlRows = CSVFile.readLine();

        while (UrlRows != null) {
            String formated = formatSearch(UrlRows);
            UrlList.add(formated);
            UrlRows = CSVFile.readLine();
        }

        return UrlList;
    }

    //Generates list of urls from manual input
    public List<String> generateUrlsFromMultipleSniper() throws FileNotFoundException, IOException {
        List<String> UrlList = new ArrayList<>();
        String[] stringList = hostingMultiple.getText().split("\\n");

        for (int i = 0; i <= stringList.length - 1; i++) {
            String formated = formatSearch(stringList[i]);
            UrlList.add(formated);
        }

        hostingMultiple.setText("");

        for (int i = 0; i <= UrlList.size() - 1; i++) {
            hostingMultiple.append(UrlList.get(i) + "\n");
        }

        return UrlList;
    }

    //Enables buttons and returns report
    public void endRunSniper(int counter, String format, String path) {
        enableButtons();
        hyphens("Sniper");
        update("Sniper", "Done!");
        update("Sniper", counter + " site(s) returned valid data.");

        if (format.equals("csv")) {
            update("Sniper", "File saved as " + path);
        }

        if (format.equals("output")) {
            update("Sniper", "Please see output ------------->");
        }

        hyphens("Sniper");
    }

    //Stops Alexa processing
    public void stopSniper() {
        sniperFinished = true;
        stopSniper.setEnabled(false);
        update("Sniper", "Stopping!");
    }

    //Creates connection from url in string   
    public void getRuns() throws IOException, HttpStatusException, InterruptedException {
        String ua = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
        Document doc = (Document) Jsoup.connect("http://www.infosniper.net/location-based-service-account.php?key=" + sniperKey + "&lang=1")
                .userAgent(ua)
                .referrer("http://www.google.com")
                .maxBodySize(0)
                .timeout(0)
                .get();

        Element e = doc.select(".content").first();
        String status = e.childNode(15).toString();
        String used = e.childNode(19).toString();

        status = fixRuns(status);
        used = fixRuns(used);

        usedLabel.setText("Searches used: " + used);
        statusLabel.setText("Searches remaining: " + status);

    }

    //Formats remaining runs display
    public String fixRuns(String text) {
        text = text.replace("<b>", "");
        text = text.replace("</b>", "");
        text = text.replace(".", ",");
        return text;
    }

    //Returns Alexa search string
    public String sniperSearch(String search) {
        String url = null;

        url = "http://www.infosniper.net/members.php?k=aK255bdw2o758d4WspXsm4&domain=" + search;

        return url;
    }

    //Removes spaces in string
    public String sniperClean(String text) {
        text = text.replace("&nbsp;", "");
        text = text.replace("n/a", "");
        text = text.replace("()", "");

        if (text.equals(" ") || text.equals("")) {
            text = null;
        }
        return text;
    }

    //Retrieves IP from infosniper
    public String pullIP(Document doc) {
        Element IP = doc.select(".content-td2").get(0);
        String address = IP.childNode(0).toString();
        address = sniperClean(address);
        hostingIP.setText(address);
        return (address);
    }

    //Retrieves City from infosniper
    public String pullCity(Document doc) {
        Element city = doc.select(".content-td2").get(1);
        String cityS = city.text();
        cityS = sniperClean(cityS);
        hostingCity.setText(cityS);
        return (cityS);
    }

    //Retrieves Host from infosniper
    public String pullHost(Document doc) {
        Element host = doc.select(".content-td2").get(4);
        String hostS = host.childNode(0).toString();
        hostS = sniperClean(hostS);
        hostingHost.setText(hostS);
        return (hostS);
    }

    //Retrieves State from infosniper
    public String pullState(Document doc) {
        Element state = doc.select(".content-td2").get(5);
        String stateS = state.text();
        stateS = sniperClean(stateS);
        hostingState.setText(stateS);
        return (stateS);
    }

    //Retrieves Country from infosniper
    public String pullCountry(Document doc) {
        Element country = doc.select(".content-td2").get(9);
        String countryS = country.text();
        countryS = sniperClean(countryS);
        hostingCountry.setText(countryS);
        return (countryS);
    }

    //----------------------------UI updates----------------------------------------
    //adds text to Global text field
    public void updateGlobal(String text) {
        globalOutput.append(text + "\n");
        int len = globalOutput.getDocument().getLength();
        globalOutput.setCaretPosition(len);
    }

    //adds text to Coutnry text field
    public void updateCountry(String text) {
        countryOutput.append(text + "\n");
        int len = countryOutput.getDocument().getLength();
        countryOutput.setCaretPosition(len);
    }

    //adds text to Site text field
    public void updateSite(String text) {
        siteOutput.append(text + "\n");
        int len = siteOutput.getDocument().getLength();
        siteOutput.setCaretPosition(len);
    }
//------------------------------------------------------------------------------

    /**
     * Creates new form UI
     */
    public AwesomeSearchTools() {
        initComponents();
        try {
            getRuns();
        } catch (IOException ex) {
            Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        alexaInFile = new javax.swing.JFileChooser();
        alexaOutput = new javax.swing.ButtonGroup();
        inputAlexa = new javax.swing.ButtonGroup();
        alexaOutFile = new javax.swing.JFileChooser();
        hostingInput = new javax.swing.ButtonGroup();
        hostingOutput = new javax.swing.ButtonGroup();
        sniperInFile = new javax.swing.JFileChooser();
        sniperOutFile = new javax.swing.JFileChooser();
        AwesomeCsvOut = new javax.swing.JFileChooser();
        awesomeInFile = new javax.swing.JFileChooser();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        AllInOne = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        awesomeUrl = new javax.swing.JTextField();
        awesomeCsvIn = new javax.swing.JCheckBox();
        awesomeDomain = new javax.swing.JCheckBox();
        awesomeCsvInBrowse = new javax.swing.JButton();
        awesomeCsvPath = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        awesomeAlexa = new javax.swing.JCheckBox();
        awesomeHosting = new javax.swing.JCheckBox();
        awesomeRegistrar = new javax.swing.JCheckBox();
        awesomeLocation = new javax.swing.JCheckBox();
        whoisCheck = new javax.swing.JCheckBox();
        resultsPanel = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        awesomeIP = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        awesomeHost = new javax.swing.JTextField();
        jPanel18 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        awesomeGlobal = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        awesomeUS = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        awesomeRegName = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        awesomeRegUrl = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        awesomeRegDate = new javax.swing.JTextField();
        awesomeRegUpdated = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        awesomeWhois = new javax.swing.JTextField();
        jPanel20 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        awesomeCountry = new javax.swing.JTextField();
        awesomeState = new javax.swing.JTextField();
        awesomeCity = new javax.swing.JTextField();
        jPanel25 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        whoisText = new javax.swing.JTextArea();
        jPanel21 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        awesomeUpdate = new javax.swing.JTextArea();
        jPanel22 = new javax.swing.JPanel();
        awesomeCopy = new javax.swing.JButton();
        awesomeCsv = new javax.swing.JButton();
        preview = new javax.swing.JCheckBox();
        jPanel17 = new javax.swing.JPanel();
        awesomeRun = new javax.swing.JButton();
        jPanel26 = new javax.swing.JPanel();
        clearUpdates = new javax.swing.JButton();
        clearResults = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        currentSite = new javax.swing.JLabel();
        Alexa = new javax.swing.JPanel();
        runAlexa = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        currentStatusAlexa = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        csvOutput = new javax.swing.JRadioButton();
        outPath = new javax.swing.JTextField();
        displayResults = new javax.swing.JRadioButton();
        browseOutputAlexa = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        csvFile = new javax.swing.JRadioButton();
        browseButtonAlexa = new javax.swing.JButton();
        fromFile = new javax.swing.JTextField();
        manual = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        countryRanking = new javax.swing.JCheckBox();
        globalRanking = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        siteOutput = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        countryOutput = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        copySite = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        globalOutput = new javax.swing.JTextArea();
        copyGlobal = new javax.swing.JButton();
        copyCountry = new javax.swing.JButton();
        stopAlexa = new javax.swing.JButton();
        Hosting = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        hostingSingle = new javax.swing.JTextField();
        hostingSingleRadio = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        hostingMultiple = new javax.swing.JTextArea();
        hostingMultipleRadio = new javax.swing.JRadioButton();
        hostingCsvRadio = new javax.swing.JRadioButton();
        hostingCsvIn = new javax.swing.JTextField();
        hostingBrowseInput = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        hostingUpdates = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        hostingDisplay = new javax.swing.JRadioButton();
        hostingOutputCsv = new javax.swing.JRadioButton();
        hostingBrowseOutput = new javax.swing.JButton();
        hostingCsvOut = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        hostingDomain = new javax.swing.JTextField();
        hostingIP = new javax.swing.JTextField();
        hostingHost = new javax.swing.JTextField();
        hostingCountry = new javax.swing.JTextField();
        hostingState = new javax.swing.JTextField();
        hostingCity = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        hostingResults = new javax.swing.JTextArea();
        hostingRun = new javax.swing.JButton();
        stopSniper = new javax.swing.JButton();
        clearOutput = new javax.swing.JButton();
        Settings = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        alexaWait = new javax.swing.JCheckBox();
        sniperWait = new javax.swing.JCheckBox();
        waitMin = new javax.swing.JTextField();
        waitMax = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        awesomeWait = new javax.swing.JCheckBox();
        jPanel23 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        usedLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();

        alexaInFile.setCurrentDirectory(new java.io.File("C:\\"));

            alexaOutFile.setCurrentDirectory(new java.io.File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\AwesomeSauce Tools\\CSV Outputs"));

            sniperInFile.setCurrentDirectory(new java.io.File("C:\\"));

                sniperOutFile.setCurrentDirectory(new java.io.File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\AwesomeSauce Tools\\CSV Outputs"));

                AwesomeCsvOut.setCurrentDirectory(new java.io.File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\AwesomeSauce Tools\\CSV Outputs"));

                awesomeInFile.setCurrentDirectory(new java.io.File("G:\\AP-US\\USAPO\\U.S. Internet Investigations\\Tools\\AwesomeSauce Tools\\CSV Outputs"));

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                setTitle("Awesome Tools");
                setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

                jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Input"));

                awesomeCsvIn.setText("CSV");

                awesomeDomain.setText("Domain");

                awesomeCsvInBrowse.setText("Browse");
                awesomeCsvInBrowse.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        awesomeCsvInBrowseActionPerformed(evt);
                    }
                });

                awesomeCsvPath.setEditable(false);

                javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
                jPanel14.setLayout(jPanel14Layout);
                jPanel14Layout.setHorizontalGroup(
                    jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(awesomeDomain)
                            .addComponent(awesomeCsvIn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(awesomeCsvInBrowse)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(awesomeCsvPath))
                            .addComponent(awesomeUrl)))
                );
                jPanel14Layout.setVerticalGroup(
                    jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(awesomeDomain)
                            .addComponent(awesomeUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(awesomeCsvIn)
                            .addComponent(awesomeCsvInBrowse)
                            .addComponent(awesomeCsvPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                );

                jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Options"));

                awesomeAlexa.setSelected(true);
                awesomeAlexa.setText("Alexa");

                awesomeHosting.setSelected(true);
                awesomeHosting.setText("Hosting");

                awesomeRegistrar.setSelected(true);
                awesomeRegistrar.setText("Registrar");

                awesomeLocation.setSelected(true);
                awesomeLocation.setText("Location");

                whoisCheck.setSelected(true);
                whoisCheck.setText("Whois");

                javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
                jPanel15.setLayout(jPanel15Layout);
                jPanel15Layout.setHorizontalGroup(
                    jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(awesomeAlexa)
                        .addGap(18, 18, 18)
                        .addComponent(awesomeHosting)
                        .addGap(18, 18, 18)
                        .addComponent(awesomeRegistrar)
                        .addGap(18, 18, 18)
                        .addComponent(awesomeLocation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(whoisCheck)
                        .addContainerGap())
                );
                jPanel15Layout.setVerticalGroup(
                    jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(awesomeAlexa)
                            .addComponent(awesomeRegistrar)
                            .addComponent(awesomeHosting)
                            .addComponent(whoisCheck)
                            .addComponent(awesomeLocation))
                        .addGap(0, 14, Short.MAX_VALUE))
                );

                resultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Results"));

                jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Hosting Details"));

                jLabel21.setText("IP Address");

                awesomeIP.setEditable(false);

                jLabel22.setText("Host");

                awesomeHost.setEditable(false);

                javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
                jPanel13.setLayout(jPanel13Layout);
                jPanel13Layout.setHorizontalGroup(
                    jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel22))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(awesomeHost, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .addComponent(awesomeIP))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel13Layout.setVerticalGroup(
                    jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel21)
                            .addComponent(awesomeIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(awesomeHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder("Alexa"));

                jLabel19.setText("Global Rank");

                awesomeGlobal.setEditable(false);

                jLabel20.setText("US Rank");

                awesomeUS.setEditable(false);

                javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
                jPanel18.setLayout(jPanel18Layout);
                jPanel18Layout.setHorizontalGroup(
                    jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(awesomeGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(awesomeUS, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(97, Short.MAX_VALUE))
                );
                jPanel18Layout.setVerticalGroup(
                    jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel20)
                                .addComponent(awesomeUS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(awesomeGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel19)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Registrar"));

                jLabel23.setText("Name");

                awesomeRegName.setEditable(false);

                jLabel24.setText("URL");

                awesomeRegUrl.setEditable(false);

                jLabel25.setText("Registered");

                jLabel26.setText("Updated");

                awesomeRegDate.setEditable(false);

                awesomeRegUpdated.setEditable(false);

                jLabel27.setText("Whois");

                awesomeWhois.setEditable(false);

                javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
                jPanel19.setLayout(jPanel19Layout);
                jPanel19Layout.setHorizontalGroup(
                    jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addGap(40, 40, 40)
                                .addComponent(awesomeRegName, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel25))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(awesomeRegDate, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .addComponent(awesomeRegUpdated)))
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24)
                                    .addComponent(jLabel27))
                                .addGap(38, 38, 38)
                                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(awesomeWhois, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .addComponent(awesomeRegUrl))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel19Layout.setVerticalGroup(
                    jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(awesomeRegName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(awesomeRegUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(awesomeWhois, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(awesomeRegDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(awesomeRegUpdated, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder("Location"));

                jLabel16.setText("Country");

                jLabel17.setText("State");

                jLabel18.setText("City");

                awesomeCountry.setEditable(false);

                awesomeState.setEditable(false);

                awesomeCity.setEditable(false);

                javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
                jPanel20.setLayout(jPanel20Layout);
                jPanel20Layout.setHorizontalGroup(
                    jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(awesomeCountry)
                            .addComponent(awesomeState)
                            .addComponent(awesomeCity, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel20Layout.setVerticalGroup(
                    jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(awesomeCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(awesomeState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(awesomeCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder("Whois"));

                whoisText.setEditable(false);
                whoisText.setColumns(20);
                whoisText.setRows(5);
                jScrollPane9.setViewportView(whoisText);

                javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
                jPanel25.setLayout(jPanel25Layout);
                jPanel25Layout.setHorizontalGroup(
                    jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9)
                );
                jPanel25Layout.setVerticalGroup(
                    jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                );

                javax.swing.GroupLayout resultsPanelLayout = new javax.swing.GroupLayout(resultsPanel);
                resultsPanel.setLayout(resultsPanelLayout);
                resultsPanelLayout.setHorizontalGroup(
                    resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resultsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );
                resultsPanelLayout.setVerticalGroup(
                    resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resultsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                );

                jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder("Updates"));

                awesomeUpdate.setEditable(false);
                awesomeUpdate.setColumns(20);
                awesomeUpdate.setRows(5);
                jScrollPane7.setViewportView(awesomeUpdate);

                javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
                jPanel21.setLayout(jPanel21Layout);
                jPanel21Layout.setHorizontalGroup(
                    jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane7)
                        .addContainerGap())
                );
                jPanel21Layout.setVerticalGroup(
                    jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane7)
                        .addContainerGap())
                );

                jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

                awesomeCopy.setText("Copy");
                awesomeCopy.setEnabled(false);
                awesomeCopy.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        awesomeCopyActionPerformed(evt);
                    }
                });

                awesomeCsv.setText("CSV File");
                awesomeCsv.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        awesomeCsvActionPerformed(evt);
                    }
                });

                preview.setSelected(true);
                preview.setText("Preview");

                javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
                jPanel22.setLayout(jPanel22Layout);
                jPanel22Layout.setHorizontalGroup(
                    jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(preview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(awesomeCopy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(awesomeCsv, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                        .addContainerGap())
                );
                jPanel22Layout.setVerticalGroup(
                    jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(preview)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(awesomeCopy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(awesomeCsv)
                        .addContainerGap())
                );

                jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Run"));

                awesomeRun.setText("Run");
                awesomeRun.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        awesomeRunActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
                jPanel17.setLayout(jPanel17Layout);
                jPanel17Layout.setHorizontalGroup(
                    jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(awesomeRun, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                jPanel17Layout.setVerticalGroup(
                    jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(awesomeRun)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder("Clear"));

                clearUpdates.setText("Clear Updates");
                clearUpdates.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        clearUpdatesActionPerformed(evt);
                    }
                });

                clearResults.setText("Clear Results");
                clearResults.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        clearResultsActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
                jPanel26.setLayout(jPanel26Layout);
                jPanel26Layout.setHorizontalGroup(
                    jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel26Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clearResults, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(clearUpdates, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                        .addContainerGap())
                );
                jPanel26Layout.setVerticalGroup(
                    jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel26Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(clearUpdates)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clearResults)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jLabel28.setText("Current Domain:");

                currentSite.setText("None");

                javax.swing.GroupLayout AllInOneLayout = new javax.swing.GroupLayout(AllInOne);
                AllInOne.setLayout(AllInOneLayout);
                AllInOneLayout.setHorizontalGroup(
                    AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AllInOneLayout.createSequentialGroup()
                        .addGroup(AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(AllInOneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(AllInOneLayout.createSequentialGroup()
                                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, AllInOneLayout.createSequentialGroup()
                                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AllInOneLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentSite, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(60, 60, 60)))
                        .addComponent(resultsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                );
                AllInOneLayout.setVerticalGroup(
                    AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AllInOneLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(AllInOneLayout.createSequentialGroup()
                                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addGroup(AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(AllInOneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel28)
                                    .addComponent(currentSite))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
                );

                jTabbedPane1.addTab("Awesome Search", AllInOne);

                runAlexa.setText("Run");
                runAlexa.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        runAlexaActionPerformed(evt);
                    }
                });

                jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Updates"));

                currentStatusAlexa.setEditable(false);
                currentStatusAlexa.setColumns(20);
                currentStatusAlexa.setRows(5);
                jScrollPane2.setViewportView(currentStatusAlexa);

                javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                    jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                );
                jPanel3Layout.setVerticalGroup(
                    jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                        .addContainerGap())
                );

                jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

                alexaOutput.add(csvOutput);
                csvOutput.setSelected(true);
                csvOutput.setText("Output to CSV");

                alexaOutput.add(displayResults);
                displayResults.setText("Display Results");

                browseOutputAlexa.setText("Browse");
                browseOutputAlexa.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        browseOutputAlexaActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
                jPanel7.setLayout(jPanel7Layout);
                jPanel7Layout.setHorizontalGroup(
                    jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(csvOutput)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseOutputAlexa)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(outPath))
                            .addComponent(displayResults))
                        .addGap(10, 10, 10))
                );
                jPanel7Layout.setVerticalGroup(
                    jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(csvOutput)
                            .addComponent(outPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(browseOutputAlexa))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(displayResults))
                );

                jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Input"));

                inputAlexa.add(csvFile);
                csvFile.setSelected(true);
                csvFile.setText("CSV file");
                csvFile.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent evt) {
                        csvFileStateChanged(evt);
                    }
                });

                browseButtonAlexa.setText("Browse");
                browseButtonAlexa.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        browseButtonAlexaActionPerformed(evt);
                    }
                });

                inputAlexa.add(manual);
                manual.setText("Manual");
                manual.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent evt) {
                        manualStateChanged(evt);
                    }
                });

                javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
                jPanel8.setLayout(jPanel8Layout);
                jPanel8Layout.setHorizontalGroup(
                    jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(csvFile, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(browseButtonAlexa)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromFile))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(manual)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                );
                jPanel8Layout.setVerticalGroup(
                    jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(browseButtonAlexa)
                            .addComponent(csvFile)
                            .addComponent(fromFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(manual)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

                countryRanking.setText("United States Rank");

                globalRanking.setText("Global Rank");

                javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
                jPanel6.setLayout(jPanel6Layout);
                jPanel6Layout.setHorizontalGroup(
                    jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(countryRanking)
                            .addComponent(globalRanking))
                        .addGap(0, 36, Short.MAX_VALUE))
                );
                jPanel6Layout.setVerticalGroup(
                    jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(globalRanking)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(countryRanking)
                        .addContainerGap(14, Short.MAX_VALUE))
                );

                jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

                siteOutput.setEditable(false);
                siteOutput.setColumns(7);
                siteOutput.setRows(5);
                jScrollPane3.setViewportView(siteOutput);

                countryOutput.setEditable(false);
                countryOutput.setColumns(7);
                countryOutput.setRows(5);
                jScrollPane4.setViewportView(countryOutput);

                jLabel1.setText("Site");

                jLabel4.setText("Global Alexa");

                jLabel5.setText("US Alexa");

                copySite.setText("Copy");
                copySite.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        copySiteActionPerformed(evt);
                    }
                });

                globalOutput.setEditable(false);
                globalOutput.setColumns(7);
                globalOutput.setRows(5);
                jScrollPane5.setViewportView(globalOutput);

                copyGlobal.setText("Copy");
                copyGlobal.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        copyGlobalActionPerformed(evt);
                    }
                });

                copyCountry.setText("Copy");
                copyCountry.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        copyCountryActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                    jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap(2, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(copySite)
                        .addGap(52, 52, 52)
                        .addComponent(copyGlobal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(copyCountry)
                        .addGap(24, 24, 24))
                );
                jPanel2Layout.setVerticalGroup(
                    jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5)
                            .addComponent(jScrollPane4)
                            .addComponent(jScrollPane3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(copySite)
                            .addComponent(copyGlobal)
                            .addComponent(copyCountry))
                        .addContainerGap())
                );

                stopAlexa.setText("Stop");
                stopAlexa.setEnabled(false);
                stopAlexa.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        stopAlexaActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout AlexaLayout = new javax.swing.GroupLayout(Alexa);
                Alexa.setLayout(AlexaLayout);
                AlexaLayout.setHorizontalGroup(
                    AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AlexaLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, AlexaLayout.createSequentialGroup()
                                .addGroup(AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(AlexaLayout.createSequentialGroup()
                                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(18, 18, 18))
                                    .addGroup(AlexaLayout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(runAlexa, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                                        .addComponent(stopAlexa, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(44, 44, 44)))
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))
                );
                AlexaLayout.setVerticalGroup(
                    AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AlexaLayout.createSequentialGroup()
                        .addGroup(AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AlexaLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(AlexaLayout.createSequentialGroup()
                                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(AlexaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(runAlexa)
                                            .addComponent(stopAlexa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                );

                jTabbedPane1.addTab("Alexa", Alexa);

                jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

                hostingInput.add(hostingSingleRadio);
                hostingSingleRadio.setSelected(true);
                hostingSingleRadio.setText("Single domain");

                hostingMultiple.setColumns(20);
                hostingMultiple.setRows(5);
                jScrollPane1.setViewportView(hostingMultiple);

                hostingInput.add(hostingMultipleRadio);
                hostingMultipleRadio.setText("Multiple domains");

                hostingInput.add(hostingCsvRadio);
                hostingCsvRadio.setText("From CSV");

                hostingBrowseInput.setText("Browse");
                hostingBrowseInput.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        hostingBrowseInputActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
                jPanel10.setLayout(jPanel10Layout);
                jPanel10Layout.setHorizontalGroup(
                    jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hostingSingleRadio)
                            .addComponent(hostingMultipleRadio)
                            .addComponent(hostingCsvRadio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(hostingSingle)
                            .addComponent(hostingCsvIn)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(hostingBrowseInput)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                );
                jPanel10Layout.setVerticalGroup(
                    jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingSingleRadio)
                            .addComponent(hostingSingle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(hostingMultipleRadio)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingCsvRadio)
                            .addComponent(hostingBrowseInput))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                        .addComponent(hostingCsvIn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                );

                jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Updates"));

                hostingUpdates.setEditable(false);
                hostingUpdates.setColumns(20);
                hostingUpdates.setRows(5);
                jScrollPane6.setViewportView(hostingUpdates);

                javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
                jPanel11.setLayout(jPanel11Layout);
                jPanel11Layout.setHorizontalGroup(
                    jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                        .addContainerGap())
                );
                jPanel11Layout.setVerticalGroup(
                    jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                );

                jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));
                jPanel4.setMaximumSize(new java.awt.Dimension(186, 120));

                hostingOutput.add(hostingDisplay);
                hostingDisplay.setSelected(true);
                hostingDisplay.setText("Display");

                hostingOutput.add(hostingOutputCsv);
                hostingOutputCsv.setText("Output to CSV");

                hostingBrowseOutput.setText("Browse");
                hostingBrowseOutput.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        hostingBrowseOutputActionPerformed(evt);
                    }
                });

                hostingCsvOut.setMaximumSize(new java.awt.Dimension(164, 164));

                javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
                jPanel4.setLayout(jPanel4Layout);
                jPanel4Layout.setHorizontalGroup(
                    jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(hostingDisplay)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(hostingOutputCsv)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                                .addComponent(hostingBrowseOutput))
                            .addComponent(hostingCsvOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                );
                jPanel4Layout.setVerticalGroup(
                    jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(hostingDisplay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingOutputCsv)
                            .addComponent(hostingBrowseOutput))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hostingCsvOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));

                hostingDomain.setEditable(false);

                hostingIP.setEditable(false);

                hostingHost.setEditable(false);

                hostingCountry.setEditable(false);

                hostingState.setEditable(false);

                hostingCity.setEditable(false);

                jLabel6.setText("Domain");

                jLabel7.setText("IP Address");

                jLabel8.setText("Host");

                jLabel9.setText("Country");

                jLabel10.setText("State");

                jLabel13.setText("City");

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(hostingDomain)
                            .addComponent(hostingIP)
                            .addComponent(hostingHost)
                            .addComponent(hostingCountry)
                            .addComponent(hostingState)
                            .addComponent(hostingCity, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                        .addGap(45, 45, 45))
                );
                jPanel1Layout.setVerticalGroup(
                    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingDomain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostingCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addGap(0, 0, Short.MAX_VALUE))
                );

                jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

                hostingResults.setEditable(false);
                hostingResults.setColumns(20);
                hostingResults.setRows(5);
                jScrollPane8.setViewportView(hostingResults);

                javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
                jPanel9.setLayout(jPanel9Layout);
                jPanel9Layout.setHorizontalGroup(
                    jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane8)
                        .addContainerGap())
                );
                jPanel9Layout.setVerticalGroup(
                    jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8)
                );

                hostingRun.setText("Run");
                hostingRun.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        hostingRunActionPerformed(evt);
                    }
                });

                stopSniper.setText("Stop");
                stopSniper.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        stopSniperActionPerformed(evt);
                    }
                });

                clearOutput.setText("Clear");
                clearOutput.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        clearOutputActionPerformed(evt);
                    }
                });

                javax.swing.GroupLayout HostingLayout = new javax.swing.GroupLayout(Hosting);
                Hosting.setLayout(HostingLayout);
                HostingLayout.setHorizontalGroup(
                    HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(HostingLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(HostingLayout.createSequentialGroup()
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                            .addGroup(HostingLayout.createSequentialGroup()
                                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(HostingLayout.createSequentialGroup()
                                        .addComponent(hostingRun)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(stopSniper))
                                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(clearOutput, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(29, 29, 29))))
                );
                HostingLayout.setVerticalGroup(
                    HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(HostingLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(HostingLayout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearOutput))
                            .addGroup(HostingLayout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(hostingRun)
                                    .addComponent(stopSniper))))
                        .addGroup(HostingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                );

                jTabbedPane1.addTab("InfoSniper", Hosting);

                jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/MPAA_LOGO.png"))); // NOI18N

                jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Wait"));

                alexaWait.setText("Alexa");

                sniperWait.setText("InfoSniper");

                waitMin.setText("1");

                waitMax.setText("6");

                jLabel3.setText("to");

                awesomeWait.setSelected(true);
                awesomeWait.setText("Awesome");

                javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
                jPanel12.setLayout(jPanel12Layout);
                jPanel12Layout.setHorizontalGroup(
                    jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                                .addGap(0, 4, Short.MAX_VALUE)
                                .addComponent(waitMin, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(waitMax, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(alexaWait)
                                    .addComponent(sniperWait)
                                    .addComponent(awesomeWait))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                );
                jPanel12Layout.setVerticalGroup(
                    jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(alexaWait)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sniperWait)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(awesomeWait)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(waitMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(waitMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                );

                jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder("Remaining Queries"));

                jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder("InfoSniper (Location)"));

                usedLabel.setText(" ");

                statusLabel.setText(" ");

                javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
                jPanel24.setLayout(jPanel24Layout);
                jPanel24Layout.setHorizontalGroup(
                    jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel24Layout.createSequentialGroup()
                                .addComponent(usedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                                .addContainerGap())
                            .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );
                jPanel24Layout.setVerticalGroup(
                    jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(usedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusLabel)
                        .addContainerGap())
                );

                javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
                jPanel23.setLayout(jPanel23Layout);
                jPanel23Layout.setHorizontalGroup(
                    jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                );
                jPanel23Layout.setVerticalGroup(
                    jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 26, Short.MAX_VALUE))
                );

                javax.swing.GroupLayout SettingsLayout = new javax.swing.GroupLayout(Settings);
                Settings.setLayout(SettingsLayout);
                SettingsLayout.setHorizontalGroup(
                    SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SettingsLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addGroup(SettingsLayout.createSequentialGroup()
                                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(383, 383, 383)
                                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(59, Short.MAX_VALUE))
                );
                SettingsLayout.setVerticalGroup(
                    SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SettingsLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel14)
                        .addGap(33, 33, 33)
                        .addGroup(SettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(83, Short.MAX_VALUE))
                );

                jTabbedPane1.addTab("Settings", Settings);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addContainerGap())
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1)
                        .addContainerGap())
                );

                pack();
            }// </editor-fold>//GEN-END:initComponents

    private void browseButtonAlexaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonAlexaActionPerformed
        alexaInFile.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Separated Value (.csv)", "csv");
        alexaInFile.addChoosableFileFilter(filter);
        int returnVal = alexaInFile.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = alexaInFile.getSelectedFile();
            fromFile.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonAlexaActionPerformed

    private void runAlexaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runAlexaActionPerformed
        boolean noErrors = true;
        if (!globalRanking.isSelected() && !countryRanking.isSelected()) {
            hyphens("Progress");
            update("Alexa", "No search options selected!");
            hyphens("Progress");
        } else {
            if (csvFile.isSelected() && (fromFile.getText().isEmpty())) {
                hyphens("Progress");
                update("Alexa", "Please input a csv file to pull from!");
                hyphens("Progress");
            } else {
                if (csvOutput.isSelected() && outPath.getText().isEmpty()) {
                    hyphens("Progress");
                    update("Alexa", "Please input a path to export data!");
                    hyphens("Progress");
                } else {

                    List<String> list = new ArrayList<>();
                    if (csvFile.isSelected()) {
                        try {
                            list = generateUrlsFromCsv(fromFile.getText());
                        } catch (IOException ex) {
                            Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (manual.isSelected()) {
                        try {
                            list = generateUrlsFromManual();
                        } catch (IOException ex) {
                            Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    //check for errors
                    for (int n = 0; n <= list.size() - 1; n++) {
                        if (!list.get(n).contains(".")) {
                            hyphens("Progress");
                            update("Alexa", "(" + (n + 1) + ") " + list.get(n) + " needs a domain!");
                            hyphens("Progress");
                            enableButtons();
                            noErrors = false;
                        }
                    }

                    if (noErrors == true) {
                        alexaFinished = false;
                        disableButtons();
                        alexaThread = new alexaWorker();
                        alexaThread.execute();
                    }
                }
            }
        }
    }//GEN-LAST:event_runAlexaActionPerformed

    private void copySiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copySiteActionPerformed
        String text = siteOutput.getText();
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }//GEN-LAST:event_copySiteActionPerformed

    private void copyGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyGlobalActionPerformed
        String text = globalOutput.getText();
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }//GEN-LAST:event_copyGlobalActionPerformed

    private void copyCountryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyCountryActionPerformed
        String text = countryOutput.getText();
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }//GEN-LAST:event_copyCountryActionPerformed

    private void manualStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_manualStateChanged
        if (manual.isSelected()) {
            siteOutput.setEditable(true);
        }

        if (csvFile.isSelected()) {
            siteOutput.setEditable(false);
        }
    }//GEN-LAST:event_manualStateChanged

    private void csvFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_csvFileStateChanged
        if (manual.isSelected()) {
            siteOutput.setEditable(true);
        }

        if (csvFile.isSelected()) {
            siteOutput.setEditable(false);
        }
    }//GEN-LAST:event_csvFileStateChanged

    private void stopAlexaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAlexaActionPerformed
        stopAlexa();
    }//GEN-LAST:event_stopAlexaActionPerformed

    private void browseOutputAlexaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseOutputAlexaActionPerformed
        alexaOutFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = alexaOutFile.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = alexaOutFile.getSelectedFile();
            outPath.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseOutputAlexaActionPerformed

    private void hostingRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostingRunActionPerformed
        boolean noErrors = true;
        if (hostingCsvRadio.isSelected() && (hostingCsvIn.getText().isEmpty())) {
            hyphens("Sniper");
            update("Sniper", "Please input a csv file to pull from!");
            hyphens("Sniper");
            noErrors = false;
        } else {
            if (hostingOutputCsv.isSelected() && hostingCsvOut.getText().isEmpty()) {
                hyphens("Sniper");
                update("Sniper", "Please input a path to export data!");
                hyphens("Sniper");
                noErrors = false;
            } else {
                if (hostingMultipleRadio.isSelected() && hostingMultiple.getText().isEmpty()) {
                    hyphens("Sniper");
                    update("Sniper", "Please input a list of domains!");
                    hyphens("Sniper");
                    noErrors = false;
                } else {
                    if (hostingSingleRadio.isSelected() && hostingSingle.getText().isEmpty()) {
                        hyphens("Sniper");
                        update("Sniper", "Please input a domain!");
                        hyphens("Sniper");
                        noErrors = false;
                    }
                }

                List<String> list = new ArrayList<>();
                if (hostingCsvRadio.isSelected() && noErrors == true) {
                    try {
                        list = generateUrlsFromCsvSniper();
                    } catch (IOException ex) {
                        Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (hostingMultipleRadio.isSelected() && noErrors == true) {
                    try {
                        list = generateUrlsFromMultipleSniper();
                    } catch (IOException ex) {
                        Logger.getLogger(AwesomeSearchTools.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (hostingSingleRadio.isSelected() && noErrors == true) {
                    list.add(hostingSingle.getText());
                }

                //check for errors
                if (noErrors == true) {
                    for (int n = 0; n <= list.size() - 1; n++) {
                        if (!list.get(n).contains(".")) {
                            hyphens("Sniper");
                            if (hostingSingleRadio.isSelected()) {
                                update("Sniper", list.get(n) + " needs a domain!");
                            } else {
                                update("Sniper", "(" + (n + 1) + ") " + list.get(n) + " needs a domain!");
                            }
                            hyphens("Sniper");
                            enableButtons();
                            noErrors = false;
                        }
                    }
                }

                if (noErrors == true) {
                    sniperFinished = false;
                    disableButtons();
                    sniperThread = new sniperWorker();
                    sniperThread.execute();
                }
            }
        }


    }//GEN-LAST:event_hostingRunActionPerformed

    private void hostingBrowseOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostingBrowseOutputActionPerformed
        sniperOutFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = sniperOutFile.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = sniperOutFile.getSelectedFile();
            hostingCsvOut.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_hostingBrowseOutputActionPerformed

    private void hostingBrowseInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostingBrowseInputActionPerformed
        sniperInFile.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Separated Value (.csv)", "csv");
        sniperInFile.addChoosableFileFilter(filter);
        int returnVal = sniperInFile.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = sniperInFile.getSelectedFile();
            hostingCsvIn.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_hostingBrowseInputActionPerformed

    private void clearOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearOutputActionPerformed
        hostingResults.setText("");
    }//GEN-LAST:event_clearOutputActionPerformed

    private void stopSniperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopSniperActionPerformed
        stopSniper();
    }//GEN-LAST:event_stopSniperActionPerformed

    private void awesomeRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_awesomeRunActionPerformed
        disableButtons();
        awesomeThread = new awesomeWorker();
        awesomeThread.execute();
    }//GEN-LAST:event_awesomeRunActionPerformed

    private void clearUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearUpdatesActionPerformed
        awesomeUpdate.setText("");
    }//GEN-LAST:event_clearUpdatesActionPerformed

    private void clearResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearResultsActionPerformed
        awesomeGlobal.setText("");
        awesomeUS.setText("");
        awesomeIP.setText("");
        awesomeHost.setText("");
        awesomeCountry.setText("");
        awesomeState.setText("");
        awesomeCity.setText("");
        awesomeRegName.setText("");
        awesomeRegUrl.setText("");
        awesomeWhois.setText("");
        awesomeRegDate.setText("");
        awesomeRegUpdated.setText("");
        whoisText.setText("");
    }//GEN-LAST:event_clearResultsActionPerformed

    private void awesomeCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_awesomeCopyActionPerformed
        copy(domainName, domainAlexaGlobal, domainAlexaUS, domainIP, domainHost,
                domainCountry, domainState, domainCity, domainRegName, domainRegUrl, domainRegWhois,
                domainRegistered, domainUpdated, domainWhois);
    }//GEN-LAST:event_awesomeCopyActionPerformed

    private void awesomeCsvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_awesomeCsvActionPerformed
        awesomeCsv();
    }//GEN-LAST:event_awesomeCsvActionPerformed

    private void awesomeCsvInBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_awesomeCsvInBrowseActionPerformed
        awesomeInFile.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Separated Value (.csv)", "csv");
        awesomeInFile.addChoosableFileFilter(filter);
        int returnVal = awesomeInFile.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = awesomeInFile.getSelectedFile();
            awesomeCsvPath.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_awesomeCsvInBrowseActionPerformed

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
            java.util.logging.Logger.getLogger(AwesomeSearchTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AwesomeSearchTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AwesomeSearchTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AwesomeSearchTools.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AwesomeSearchTools().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Alexa;
    private javax.swing.JPanel AllInOne;
    private javax.swing.JFileChooser AwesomeCsvOut;
    private javax.swing.JPanel Hosting;
    private javax.swing.JPanel Settings;
    private javax.swing.JFileChooser alexaInFile;
    private javax.swing.JFileChooser alexaOutFile;
    private javax.swing.ButtonGroup alexaOutput;
    private javax.swing.JCheckBox alexaWait;
    private javax.swing.JCheckBox awesomeAlexa;
    private javax.swing.JTextField awesomeCity;
    private javax.swing.JButton awesomeCopy;
    private javax.swing.JTextField awesomeCountry;
    private javax.swing.JButton awesomeCsv;
    private javax.swing.JCheckBox awesomeCsvIn;
    private javax.swing.JButton awesomeCsvInBrowse;
    private javax.swing.JTextField awesomeCsvPath;
    private javax.swing.JCheckBox awesomeDomain;
    private javax.swing.JTextField awesomeGlobal;
    private javax.swing.JTextField awesomeHost;
    private javax.swing.JCheckBox awesomeHosting;
    private javax.swing.JTextField awesomeIP;
    private javax.swing.JFileChooser awesomeInFile;
    private javax.swing.JCheckBox awesomeLocation;
    private javax.swing.JTextField awesomeRegDate;
    private javax.swing.JTextField awesomeRegName;
    private javax.swing.JTextField awesomeRegUpdated;
    private javax.swing.JTextField awesomeRegUrl;
    private javax.swing.JCheckBox awesomeRegistrar;
    private javax.swing.JButton awesomeRun;
    private javax.swing.JTextField awesomeState;
    private javax.swing.JTextField awesomeUS;
    private javax.swing.JTextArea awesomeUpdate;
    private javax.swing.JTextField awesomeUrl;
    private javax.swing.JCheckBox awesomeWait;
    private javax.swing.JTextField awesomeWhois;
    private javax.swing.JButton browseButtonAlexa;
    private javax.swing.JButton browseOutputAlexa;
    private javax.swing.JButton clearOutput;
    private javax.swing.JButton clearResults;
    private javax.swing.JButton clearUpdates;
    private javax.swing.JButton copyCountry;
    private javax.swing.JButton copyGlobal;
    private javax.swing.JButton copySite;
    private javax.swing.JTextArea countryOutput;
    private javax.swing.JCheckBox countryRanking;
    private javax.swing.JRadioButton csvFile;
    private javax.swing.JRadioButton csvOutput;
    private javax.swing.JLabel currentSite;
    private javax.swing.JTextArea currentStatusAlexa;
    private javax.swing.JRadioButton displayResults;
    private javax.swing.JTextField fromFile;
    private javax.swing.JTextArea globalOutput;
    private javax.swing.JCheckBox globalRanking;
    private javax.swing.JButton hostingBrowseInput;
    private javax.swing.JButton hostingBrowseOutput;
    private javax.swing.JTextField hostingCity;
    private javax.swing.JTextField hostingCountry;
    private javax.swing.JTextField hostingCsvIn;
    private javax.swing.JTextField hostingCsvOut;
    private javax.swing.JRadioButton hostingCsvRadio;
    private javax.swing.JRadioButton hostingDisplay;
    private javax.swing.JTextField hostingDomain;
    private javax.swing.JTextField hostingHost;
    private javax.swing.JTextField hostingIP;
    private javax.swing.ButtonGroup hostingInput;
    private javax.swing.JTextArea hostingMultiple;
    private javax.swing.JRadioButton hostingMultipleRadio;
    private javax.swing.ButtonGroup hostingOutput;
    private javax.swing.JRadioButton hostingOutputCsv;
    private javax.swing.JTextArea hostingResults;
    private javax.swing.JButton hostingRun;
    private javax.swing.JTextField hostingSingle;
    private javax.swing.JRadioButton hostingSingleRadio;
    private javax.swing.JTextField hostingState;
    private javax.swing.JTextArea hostingUpdates;
    private javax.swing.ButtonGroup inputAlexa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
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
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton manual;
    private javax.swing.JTextField outPath;
    private javax.swing.JCheckBox preview;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JButton runAlexa;
    private javax.swing.JTextArea siteOutput;
    private javax.swing.JFileChooser sniperInFile;
    private javax.swing.JFileChooser sniperOutFile;
    private javax.swing.JCheckBox sniperWait;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton stopAlexa;
    private javax.swing.JButton stopSniper;
    private javax.swing.JLabel usedLabel;
    private javax.swing.JTextField waitMax;
    private javax.swing.JTextField waitMin;
    private javax.swing.JCheckBox whoisCheck;
    private javax.swing.JTextArea whoisText;
    // End of variables declaration//GEN-END:variables
}
