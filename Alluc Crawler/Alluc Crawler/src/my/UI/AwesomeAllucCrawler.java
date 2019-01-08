/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


//Copyright 2016, Brandon Coleman, All rights reserved.

package my.UI;

import java.awt.Color;
import java.io.IOException;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.*;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import jxl.*;
import jxl.write.*;

/**
 *
 * @author colemanb
 */
public class AwesomeAllucCrawler extends javax.swing.JFrame {

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy.hhmma");
    String date = sdf.format(new Date());
    int freetvcounter = 0;
    java.lang.Boolean onlyRunOnce = false;
    java.lang.Boolean siteLoaded = false;

    List<String> titles = new ArrayList<>();
    List<String> hosts = new ArrayList<>();
    List<String> links = new ArrayList<>();
    List<String> domains = new ArrayList<>();
    List<String> createds = new ArrayList<>();
    List<String> fileSizes = new ArrayList<>();

    String contactInfo = "";

    int linkCount;
    int linkCountTotal;
    int maxLinkCount = 0;
    String linksPulledToday;

    String COKey;

    int conCounter = 1;
    int loop = 2100;

    String currentSearch;
    String currentSite;

    private allucWorker worker;

    public class allucResult {

        String title;
        String hostername;
        List<lockerURL> hosterurls;
        String sizeinternal;
        String created;
    }

    public class Page {

        List<allucResult> result;
        String resultcount;
        String fetchedtoday;
    }

    public class lockerURL {

        String url;
    }

    class allucWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws Exception {
            beginRun();
            List<String> urls = generateUrls();
            String lockerStringTarget = "";
            String pullFrom = lockerFrom.getText();
            String pullCount = lockerCount.getText();
            int modFrom = Integer.parseInt(pullFrom);
            int modCount = Integer.parseInt(pullCount);
            String searchType = "";

            String url = "";

            if (!allHostsRadio.isSelected()) {
                if (streamingHostRadio.isSelected()) {
                    lockerStringTarget = streamingHosts.getSelectedItem().toString();
                    searchType = "stream";
                }

                if (downloadHostRadio.isSelected()) {
                    searchType = "download";
                    lockerStringTarget = downloadHosts.getSelectedItem().toString();
                }
            } else {
                if (streamingRadio.isSelected()) {
                    searchType = "stream";
                }

                if (downloadRadio.isSelected()) {
                    searchType = "download";
                }
            }

            if (vmExport.isSelected()) {
                Document authDoc = Connection("", "COAuth");
                sessionKey(authDoc);
                Document doc = Connection(lockerStringTarget, "CentralOpsDomain");
                regName(doc);
                coContacts(doc);
            }

            for (int u = 0; u <= urls.size() - 1; u++) {

                //for (int q = 0; q < 50; q++) {

                    if (waitCheck.isSelected()) {
                        sleep();
                    }

                    int counter = 1;
                    if (allHostsRadio.isSelected()) {
                        url = "http://www.alluc.ee/api/search/" + searchType + "/?apikey=" + getKey() + "&query=" + urls.get(u) + "&count=" + modCount + "&from=" + modFrom + "&getmeta=0";
                    } else {
                        url = "http://www.alluc.ee/api/search/" + searchType + "/?apikey=" + getKey() + "&query=" + urls.get(u) + "+host%3A" + lockerStringTarget + "&count=" + modCount + "&from=" + modFrom + "&getmeta=0";
                    }
                    currentSite = url;
                    currentSearch = urls.get(u);
                    generatedUrls.append(url + "\n");
                    String document = jsonConnection(url);
                    Gson gson = new Gson();
                    Page page = gson.fromJson(document, Page.class);

                    linkCount = Integer.parseInt(page.resultcount);
                    linkCountTotal = linkCountTotal + linkCount;
                    linksPulledToday = page.fetchedtoday;
                    if (maxLinkCount < linkCount) {
                        maxLinkCount = linkCount;
                    }
                    for (allucResult i : page.result) {
                        domains.add(lockerStringTarget);
                        java.lang.Boolean runOnce = false;
                        updateProgress(counter + ") Title: " + i.title);
                        String mod = i.title;
                        mod = fixUpComma(mod);
                        titles.add(mod);
                        updateProgress(counter + ") Host: " + i.hostername);
                        hosts.add(i.hostername);
                        for (lockerURL o : i.hosterurls) {
                            if (runOnce.equals(false)) {
                                updateProgress(counter + ") CyberLocker URL: " + o.url);
                                links.add(o.url);
                            }
                            runOnce = true;
                        }
                        updateProgress(counter + ") Created: " + i.created);
                        createds.add(i.created);
                        updateProgress(counter + ") File Size: " + i.sizeinternal);
                        fileSizes.add(i.sizeinternal);

                        updateProgress("------------------------");
                        counter++;
                    }
                    loop = loop + 100;
                //}
            }

            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            endRun();
        }
    }

    //----------------Methods to crawl Alluc-------------------------------
    public String jsonConnection(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            updateProgress(conCounter + ") Searching for: " + currentSearch);
            updateProgress("------------------------");
            conCounter++;

            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void writeWb() throws IOException, WriteException {
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath() + "/";

            if (vmExport.isSelected()) {
                WritableWorkbook wb = Workbook.createWorkbook(new File(path + outFileNameTextTarget.getText() + " " + date + ".xls"));
                WritableCellFormat cf = new WritableCellFormat();
                cf.setWrap(true);
                WritableSheet sheet = wb.createSheet("Visa/Mastercard", 0);
                Label label = new Label(0, 0, "Domain");
                Label label1 = new Label(1, 0, "Contact Info");
                Label label2 = new Label(2, 0, "Title");
                Label label3 = new Label(3, 0, "Link");
                Label label4 = new Label(4, 0, "Created");
                Label label5 = new Label(5, 0, "File Size");
                sheet.addCell(label);
                sheet.addCell(label1);
                sheet.addCell(label2);
                sheet.addCell(label3);
                sheet.addCell(label4);
                sheet.addCell(label5);

                int rowCounter = 1;

                for (int i = 0; i <= titles.size() - 1; i++) {
                    Label d = new Label(0, rowCounter, domains.get(i));
                    Label c = new Label(1, rowCounter, contactInfo, cf);
                    Label f = new Label(2, rowCounter, titles.get(i));
                    Label g = new Label(3, rowCounter, links.get(i));
                    Label z = new Label(4, rowCounter, createds.get(i));
                    Label x = new Label(5, rowCounter, fileSizes.get(i));
                    sheet.addCell(d);
                    sheet.addCell(c);
                    sheet.addCell(f);
                    sheet.addCell(g);
                    sheet.addCell(z);
                    sheet.addCell(x);
                    rowCounter++;
                }
                wb.write();
                wb.close();
            } else {
                WritableWorkbook wb = Workbook.createWorkbook(new File(path + outFileNameTextTarget.getText() + " " + date + ".xls"));
                WritableSheet sheet = wb.createSheet("Alluc Crawls", 0);
                Label label = new Label(0, 0, "Title");
                Label label1 = new Label(1, 0, "Host");
                Label label2 = new Label(2, 0, "Cyberlocker Link");
                Label label3 = new Label(3, 0, "Created");
                Label label4 = new Label(4, 0, "File Size");
                sheet.addCell(label);
                sheet.addCell(label1);
                sheet.addCell(label2);
                sheet.addCell(label3);
                sheet.addCell(label4);
                
                int rc = 1;

                    for (int i = 0; i <= titles.size() - 1; i++) {
                        Label a = new Label(0, rc, titles.get(i));
                        Label b = new Label(1, rc, hosts.get(i));
                        Label c = new Label(2, rc, links.get(i));
                        Label d = new Label(3, rc, createds.get(i));
                        Label f = new Label(4, rc, fileSizes.get(i));
                        sheet.addCell(a);
                        sheet.addCell(b);
                        sheet.addCell(c);
                        sheet.addCell(d);
                        sheet.addCell(f);
                        rc++;
                    }
                    wb.write();
                    wb.close();                
            }
            updateProgress("File saved as " + path + outFileNameTextTarget.getText() + " " + date + ".xls");

        }

    }

    public List<String> generateUrls() {
        List<String> urlList = new ArrayList<>();
        String[] stringList = searchTarget.getText().split("\\n");

        for (int i = 0; i <= stringList.length - 1; i++) {
            String mod = stringList[i];
            mod = mod.replace(" ", "+");
            urlList.add(mod);
        }
        return urlList;
    }

    public Document Connection(String domain, String tool) throws IOException {

        String uri = "http://hexillion.com/rf/xml/1.0/whois/?query=" + domain + "&sessionKey=" + COKey;
        String uriAuth = "https://hexillion.com/rf/xml/1.0/auth/?username=KellyMorgan&password=Starlord22";
        String ua = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

        if (tool.equals("COAuth")) {
            try {
                generatedUrls.append(uriAuth + "\n");
                Document doc = (Document) Jsoup.connect(uriAuth)
                        .userAgent(ua)
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .timeout(0)
                        .get();
                return doc;
            } catch (HttpStatusException ex) {
                updateProgress("Received error opening Central Ops Auth.. trying again");
                Connection("COAuth", domain);
            }
        }

        if (tool.equals("CentralOpsDomain")) {
            try {
                generatedUrls.append(uri + "\n");
                Document doc = (Document) Jsoup.connect(uri)
                        .userAgent(ua)
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .timeout(0)
                        .get();
                return doc;
            } catch (HttpStatusException ex) {
                updateProgress("Received error opening Central Ops Auth.. trying again");
                Connection("CentralOpsDomain", domain);
            }
        }
        return null;
    }

    //----------------------Methods to update GUI-----------------------
    //disables all go buttons
    public void disableButtons() {
        GoTarget.setEnabled(false);
    }

    //provides updates to GUI
    public void updateProgress(String text) {
        currentStatus.append(text + "\n");
        if (autoScroll.isSelected()) {
            int len = currentStatus.getDocument().getLength();
            currentStatus.setCaretPosition(len);
        }
    }

    public void sleep() throws InterruptedException {
        if (randomRadio.isSelected()) {
            int min = Integer.parseInt(random1.getText());
            int max = Integer.parseInt(random2.getText());
            Thread.sleep(randNum(min, max));
        }

        if (staticRadio.isSelected()) {
            String waitTime = staticWait.getText();
            updateProgress("Waiting " + waitTime + " second(s)");
            int wait = (int) Long.parseLong(waitTime);
            wait = wait * 1000;
            Thread.sleep(wait);
        }
    }

    public int randNum(int min, int max) {
        Random rand = new Random();
        int x = rand.nextInt((max - min) + 1) + min;
        updateProgress("Waiting " + x + " second(s)");
        x = x * 1000;
        return x;
    }

    //adds dashes to updateProgress
    public void hyphens() {
        updateProgress("----------------------------------------------------\n");
    }

    public void beginRun() {
        GoTarget.setEnabled(false);
        searchTarget.setEditable(false);
        allHostsRadio.setEnabled(false);
        streamingHostRadio.setEnabled(false);
        downloadHostRadio.setEnabled(false);
        streamingRadio.setEnabled(false);
        downloadRadio.setEnabled(false);
        outFileNameTextTarget.setEditable(false);
        lockerFrom.setEditable(false);
        lockerCount.setEditable(false);
    }

    //Enables disabled buttons and displays file saved message
    public void endRun() {
        GoTarget.setEnabled(true);
        searchTarget.setEditable(true);
        allHostsRadio.setEnabled(true);
        streamingHostRadio.setEnabled(true);
        downloadHostRadio.setEnabled(true);
        if (allHostsRadio.isSelected()) {
            streamingRadio.setEnabled(true);
            downloadRadio.setEnabled(true);
        }
        outFileNameTextTarget.setEditable(true);
        lockerFrom.setEditable(true);
        lockerCount.setEditable(true);

        int rem = (2000 - Integer.parseInt(linksPulledToday));
        int lockerCounter = Integer.parseInt(lockerCount.getText());
        int maxPages = (maxLinkCount / lockerCounter);

        conCounter = 1;

        updateProgress(linkCountTotal + " links returned.");
        updateProgress("Highest link count: " + maxLinkCount);
        updateProgress("Estimated pages to crawl at current count: " + maxPages);
        updateProgress(linksPulledToday + " links have been crawled today.");
        updateProgress(rem + " link crawls remaining.");
        updateProgress("------------------------");

        maxLinkCount = 0;
    }

    //Removes comma for CSV outputting purposes
    public String fixUpComma(String string) {
        string = string.replace(",", "");

        return string;
    }

    //properly formats search terms
    public String searchFixUp(String string) {
        string = string.replace(" ", "%20");
        string = string.replace(":", "");
        string = string.replace(",", "");

        return string;
    }

    //properly formats URLs
    public String formatUrl(String string) {

        if (string.startsWith("www.")) {
            string = "http://" + string;
        }
        return string;
    }

    public String getKey() {
        if (key1.isSelected()) {
            return "b62753452467ead71e3460c2a424416d";
        }
        if (key2.isSelected()) {
            return "c14fca999754e3f173c8346cde13415d";
        }
        return null;
    }

    public void coContacts(Document doc) {
        String docAsString = doc.toString();
        if (docAsString.contains("admincontact")) {
            Element e = doc.select("queryresult > whoisrecord > admincontact > *").first();
            if (e.tagName().equals("name")) {
                contactInfo = contactInfo.concat("\n" + e.text());
            }

            if (e.tagName().equals("address")) {
                if (!e.text().isEmpty()) {
                    contactInfo = contactInfo.concat("\n" + e.text());
                }
            }
            if (e.tagName().equals("city")) {
                contactInfo = contactInfo.concat("\n" + e.text());
            }
            if (e.tagName().equals("stateprovince")) {
                contactInfo = contactInfo.concat(", " + e.text());
            }

            if (e.tagName().equals("postalcode")) {
                contactInfo = contactInfo.concat(" " + e.text());
            }
            if (e.tagName().equals("country")) {
                contactInfo = contactInfo.concat("\n" + e.text());
            }

            if (e.tagName().equals("email")) {
                contactInfo = contactInfo.concat("\n" + e.text());
            }


            /*if (adminContact.equals("")) {
             StringSelection selection = new StringSelection("No contact");
             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
             clipboard.setContents(selection, selection);
             } else {
             StringSelection selection = new StringSelection(adminContact);
             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
             clipboard.setContents(selection, selection);
             }*/
        } else {

        }
    }

    public void regName(Document doc) {
        String docAsString = doc.toString();

        if (!docAsString.contains("<registrar>")) {
            updateProgress("CentralOps - No information available...");
            //          awesomeRegName.setText(noDataFound);
        } else {
            String name = doc.select("registrar > name").first().text();
            name = name.replace(",", "");
            updateProgress("CentralOps - Retrieving Registrar Name...");
            contactInfo = contactInfo.concat(name);
            //          awesomeRegName.setText(name);
        }
    }

    public void sessionKey(Document doc) {
        String key = doc.select("sessionkey").text();
        COKey = key;
    }

    public void goToUrl() throws URISyntaxException, IOException {
        String url = currentSite;
        if (url.equals("")) {
            updateProgress("no site loaded.");
        } else {
            Desktop d = Desktop.getDesktop();
            d.browse(new URI(url));
        }
    }

    /**
     * Creates new form frontEndUI
     */
    public AwesomeAllucCrawler() {

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

        fileChooser = new javax.swing.JFileChooser();
        searchOptions = new javax.swing.ButtonGroup();
        searchSources = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        streamingHosts = new javax.swing.JComboBox();
        allHostsRadio = new javax.swing.JRadioButton();
        streamingHostRadio = new javax.swing.JRadioButton();
        downloadHostRadio = new javax.swing.JRadioButton();
        downloadHosts = new javax.swing.JComboBox();
        GoTarget = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        outFileNameTextTarget = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        autofill = new javax.swing.JButton();
        jPanel19 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        searchTarget = new javax.swing.JTextArea();
        jPanel20 = new javax.swing.JPanel();
        lockerCount = new javax.swing.JTextField();
        lockerFrom = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        streamingRadio = new javax.swing.JRadioButton();
        downloadRadio = new javax.swing.JRadioButton();
        vmExport = new javax.swing.JCheckBox();
        CsvExport = new javax.swing.JButton();
        clearLists = new javax.swing.JButton();
        autoScroll = new javax.swing.JCheckBox();
        key1 = new javax.swing.JRadioButton();
        key2 = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        randomRadio = new javax.swing.JRadioButton();
        staticRadio = new javax.swing.JRadioButton();
        random1 = new javax.swing.JTextField();
        random2 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        staticWait = new javax.swing.JTextField();
        waitCheck = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        generatedUrls = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        goToSite = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        currentStatus = new javax.swing.JTextArea();

        fileChooser.setCurrentDirectory(new java.io.File("C:\\crawler\\alluc crawls"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Source"));

        streamingHosts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "abc.go.com", "abcfamily.go.com", "abcnews.go.com", "allmyvideos.com", "allmyvideos.net", "altervideo.net", "amazon.com", "americanidol.com", "ardmediathek.de", "arte.tv", "bbc.co.uk", "bestreams.net", "blinkbox.com", "bloomberg.com", "clicktoview.org", "clicktowatch.net", "cliplocker.net", "clipshouse.com", "cloudnes.com", "cloudnos.com", "cloudtime.to", "cloudvids.net", "cloudyvideos.com", "comcast.net", "coovideo.com", "crackle.com", "daclips.com", "daclips.in", "dailymotion.com", "divxhosting.net", "divxpress.com", "divxstage.eu", "divxstage.net", "divxstage.to", "divxstream.net", "donevideo.com", "dramafever.com", "dropvideo.com", "dropvideos.net", "duckstreaming.com", "edition.cnn.com", "epornik.com", "espn.go.com", "fast-vids.com", "faststream.in", "fastvideo.in", "filestube.com", "filmon.com", "flashx.tv", "fleon.me", "fox.com", "freshvideo.net", "gamovideo.com", "gorillavid.com", "gorillavid.in", "happystreams.net", "hqq.tv", "hulu.com", "indieflix.com", "indiereign.com", "ishared.eu", "itunes.apple.com", "jetvideo.so", "junkyvideo.com", "kabeleins.de", "kevinspacey.com", "layoverfilm.com", "letthestream.in", "letwatch.us", "limevideo.net", "matesharing.com", "mega-videos.net", "mega-vids.com", "megavid.co", "megavideoz.eu", "meuvideos.com", "mooshare.biz", "movdivx.com", "movpod.in", "movpod.net", "movreel.com", "movshare.net", "movzap.com", "myvideo.de", "netu.tv", "nosvideo.com", "nosxxx.com", "novamov.com", "nowvideo.ch", "nowvideo.co", "nowvideo.eu", "nowvideo.sx", "pivotshare.com", "play.google.com", "played.to", "playreplay.net", "pornxtube.tv", "powvideo.net", "primeshare.tv", "prosieben.de", "putstream.com", "rapidvideo.ws", "realvid.net", "rocvideo.tv", "seetheinterview.com", "shared.sx", "sharevid.co", "sharevid.org", "sharexvid.com", "sixx.de", "skyvids.net", "slickvid.com", "southparkstudios.com", "speedvideo.net", "stagevu.com", "stream4k.to", "streamcloud.eu", "streamin.eu", "streamin.to", "streamratio.com", "sundancenow.com", "teamcoco.com", "ted.com", "thefile.me", "thevideo.me", "thewatchbox.com", "topvideo.cc", "topvideo.tv", "traileraddict.com", "tubecloud.net", "tumi.tv", "turbovideos.net", "uploadtube.net", "uptostream.com", "upvideo.tv", "v-vids.com", "veehd.com", "veoh.com", "vevo.com", "vhx.tv", "vid.gg", "vidbox.eu", "vidbull.com", "vidbux.com", "videla.org", "video.pw", "video.tt", "video4every.com", "videobam.com", "videofox.net", "videofrog.eu", "videomega.tv", "videomeh.com", "videopremium.net", "videopremium.tv", "videoslasher.com", "videoslim.net", "videoweed.com", "videoweed.es", "videowood.tv", "videozed.net", "vidhog.com", "vidpaid.com", "vidplay.net", "vids.bz", "vidshok.com", "vidspot.net", "vidstation.net", "vidstream.in", "vidto.me", "vidup.me", "vidx.to", "vidxden.com", "vidzbeez.com", "vidzi.tv", "viewster.com", "vimeo.com", "vishare.us", "vivo.sx", "vk.com", "vodlocker.com", "vreer.com", "vshare.eu", "vureel.com", "watchmybit.com", "wavymotion.com", "wilbi.net", "xvidstage.com", "yavideo.tv", "yourvideohost.com", "youtube.com", "youwatch.org", "zalaa.com", "zdf.de", "url", "putlocker.com", "sockshare.com", "promptfile.com", "filenuke.com", "2gb-hosting.com", "filebox.com", "muchshare.net", "sharesix.com", "ginbig.com", "hostingbulk.com", "cyberlocker.ch", "putme.org", "180upload.com", "files.to", "freakshare.com", "freakshare.net", "lumfile.com", "epicshare.net", "grifthost.com", "mightyupload.com", "2downloadz.com", "queenshare.com", "billionuploads.com", "amonshare.com", "miloyski.com", "lumfile.se", "lemuploads.com", "firedrive.com", "quickyshare.net", "filescrate.com", "filehoot.com", "linkembed.net", "gigabyteupload.com", "pandapla.net", "uploadscenter.com", "cloudzilla.to", "megadrive.tv", "upafile.com", "uploadc.com", "mrfile.me", "vidxden.to", "thefilebay.com", "zettahost.tv", "ilook.to", "movshare.ag", "sat1.de", "voooh.com", "docs.google.com", "filelocker.guru", "speedvid.net", "openload.io", "epixhd.com", "neodrive.co", "streamable.ch" }));

        searchSources.add(allHostsRadio);
        allHostsRadio.setSelected(true);
        allHostsRadio.setText("All Hosts");
        allHostsRadio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                allHostsRadioMouseClicked(evt);
            }
        });

        searchSources.add(streamingHostRadio);
        streamingHostRadio.setText("Streaming");
        streamingHostRadio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                streamingHostRadioMouseClicked(evt);
            }
        });

        searchSources.add(downloadHostRadio);
        downloadHostRadio.setText("Download");
        downloadHostRadio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                downloadHostRadioMouseClicked(evt);
            }
        });

        downloadHosts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1fichier.com", "2shared.com", "4fastfile.com", "4shared.com", "albafile.com", "anafile.com", "asfile.com", "backin.net", "bankupload.com", "bayfiles.com", "bayfiles.net", "bezvadata.cz", "bitload.it", "bitshare.com", "bonanzashare.com", "box.com", "box.net", "cepzo.com", "clicknupload.com", "cloudzer.net", "clz.to", "creafile.net", "crocko.com", "datafile.com", "datei.to", "ddlstorage.com", "depfile.com", "depositfiles.com", "dfiles.eu", "dizzcloud.com", "dropbox.com", "easybytez.com", "edisk.cz", "edisk.eu", "egofiles.com", "ex-load.com", "expressleech.com", "extabit.com", "eyesfile.ca", "fastshare.cz", "filecubic.com", "filedap.com", "filedropper.com", "filefactory.com", "fileflare.in", "filefolks.com", "filegag.com", "filehostpro.com", "fileloby.com", "fileparadox.in", "filepost.com", "filerio.in", "filesbomb.biz", "filesbomb.com", "filesflash.com", "fileshare.ro", "filesin.com", "filesmonster.com", "filestorm.to", "filevice.com", "filewinds.com", "filezy.net", "flashdrive.it", "flashdrive.uk.com", "flyfiles.net", "free-uploading.com", "gigapeta.com", "gigasize.com", "go4up.com", "goldfile.eu", "hellupload.com", "hipfile.com", "hitfile.net", "hotfile.com", "hugefiles.net", "hulkfile.eu", "hulkshare.com", "ifile.it", "ifile.ws", "igetfile.com", "inclouddrive.com", "instaupload.com", "ipithos.to", "jumbofiles.com", "junocloud.me", "k2s.cc", "katzfiles.com", "keep2share.cc", "kingfiles.net", "letitbit.net", "likeupload.net", "likeupload.org", "livefile.org", "load.to", "luckyshare.net", "mediafire.com", "mega.co.nz", "megafiles.se", "megarelease.org", "megashares.com", "mightyload.com", "mp3box.to", "mysite.com", "neonshare.com", "neoxfiles.com", "netload.in", "nirafile.com", "nitroflare.com", "noslocker.com", "novafile.com", "nowdownload.ag", "nowdownload.ch", "nowdownload.eu", "ntupload.to", "oteupload.com", "privatefiles.com", "project-free-upload.com", "rapidbox.me", "rapidgator.net", "rapidshare.com", "rarefile.net", "rg.to", "rockdizfile.com", "rodfile.com", "roshare.info", "ryushare.com", "safelinking.net", "sanshare.com", "secureupload.eu", "sendmyway.com", "sendspace.com", "sendspace.pl", "sharebeast.com", "sharedbit.net", "sharefiles.co", "shareflare.net", "share-online.biz", "sharerepo.com", "slingfile.com", "solidfiles.com", "speedshare.eu", "speedyshare.com", "swankshare.com", "terafile.co", "turbobit.net", "tusfiles.net", "ufox.com", "ul.to", "uloz.to", "ultrafile.me", "ultramegabit.com", "uncapped-downloads.com", "uploadable.ch", "uploadbaz.com", "uploaded.net", "uploaded.to", "uploadinc.com", "uploading.com", "uploadrocket.net", "uppit.com", "upstore.net", "uptobox.com", "usaupload.net", "usefile.com", "vip-file.com", "wooupload.com", "wupfile.com", "x7files.com", "xerver.co", "zefile.com", "ziddu.com", "zinwa.com", "zippyshare.com" }));

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel17Layout.createSequentialGroup()
                                .addComponent(streamingHostRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(streamingHosts, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(allHostsRadio))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(downloadHostRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(downloadHosts, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(allHostsRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(streamingHostRadio)
                    .addComponent(streamingHosts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadHostRadio)
                    .addComponent(downloadHosts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GoTarget.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        GoTarget.setText("GO");
        GoTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoTargetActionPerformed(evt);
            }
        });

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/UI/mpaa.gif"))); // NOI18N

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

        jLabel18.setText("File Name");

        autofill.setText("Autofill");
        autofill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autofillActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(outFileNameTextTarget)
                        .addContainerGap())
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(autofill))
                        .addGap(0, 606, Short.MAX_VALUE))))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(outFileNameTextTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(autofill)
                .addGap(20, 20, 20))
        );

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

        jLabel20.setText("Search for:");

        searchTarget.setColumns(20);
        searchTarget.setRows(5);
        jScrollPane3.setViewportView(searchTarget);

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder("Search Options"));

        lockerCount.setText("5");

        lockerFrom.setText("0");

        jLabel14.setText("From");

        jLabel15.setText("Count");

        searchOptions.add(streamingRadio);
        streamingRadio.setSelected(true);
        streamingRadio.setText("Streaming");

        searchOptions.add(downloadRadio);
        downloadRadio.setText("Download");

        vmExport.setText("Visa/Mastercard");

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(vmExport)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lockerCount)
                            .addComponent(lockerFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(streamingRadio, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(downloadRadio, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lockerFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(streamingRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lockerCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel15))
                    .addComponent(downloadRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(vmExport)
                .addContainerGap())
        );

        CsvExport.setText("Export");
        CsvExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CsvExportActionPerformed(evt);
            }
        });

        clearLists.setText("Clear");
        clearLists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearListsActionPerformed(evt);
            }
        });

        autoScroll.setText("Auto Scroll");

        buttonGroup1.add(key1);
        key1.setSelected(true);
        key1.setText("Key 1");

        buttonGroup1.add(key2);
        key2.setText("Key 2");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(key1)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel17))))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(CsvExport, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clearLists)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(GoTarget, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                        .addComponent(key2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(autoScroll))
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(key1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(key2)
                            .addComponent(autoScroll)))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(65, 65, 65)
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(GoTarget)
                    .addComponent(CsvExport, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearLists))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Alluc", jPanel16);

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/my/UI/mpaa.gif"))); // NOI18N

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Pause Settings"));

        randomRadio.setSelected(true);
        randomRadio.setText("Random");

        staticRadio.setText("Static");

        random1.setText("3");

        random2.setText("6");

        jLabel7.setText("to");

        waitCheck.setText("Enabled");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(randomRadio)
                            .addComponent(staticRadio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(random1, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                            .addComponent(staticWait))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(random2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(waitCheck))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addComponent(waitCheck)
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(randomRadio)
                    .addComponent(random1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(random2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(staticRadio)
                    .addComponent(staticWait, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        generatedUrls.setEditable(false);
        generatedUrls.setColumns(20);
        generatedUrls.setRows(5);
        jScrollPane1.setViewportView(generatedUrls);

        jLabel1.setText("Generated URLs");

        goToSite.setText("Go to URL");
        goToSite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToSiteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 207, Short.MAX_VALUE)
                        .addComponent(jLabel13))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(goToSite)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(goToSite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(185, Short.MAX_VALUE))
        );

        jPanel9.getAccessibleContext().setAccessibleName("");

        jTabbedPane1.addTab("Config", jPanel4);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Updates"));

        currentStatus.setEditable(false);
        currentStatus.setColumns(20);
        currentStatus.setRows(5);
        jScrollPane2.setViewportView(currentStatus);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 726, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void GoTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoTargetActionPerformed
        worker = new allucWorker();
        worker.execute();
    }//GEN-LAST:event_GoTargetActionPerformed

    private void CsvExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CsvExportActionPerformed
        try {
            writeWb();
        } catch (IOException ex) {
            Logger.getLogger(AwesomeAllucCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WriteException ex) {
            Logger.getLogger(AwesomeAllucCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_CsvExportActionPerformed

    private void allHostsRadioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allHostsRadioMouseClicked
        if (allHostsRadio.isSelected()) {
            streamingRadio.setEnabled(true);
            downloadRadio.setEnabled(true);
        }
        if (streamingHostRadio.isSelected()) {
            streamingRadio.setEnabled(false);
            downloadRadio.setEnabled(false);
        }
        if (downloadHostRadio.isSelected()) {
            streamingRadio.setEnabled(false);
            downloadRadio.setEnabled(false);
        }
    }//GEN-LAST:event_allHostsRadioMouseClicked

    private void streamingHostRadioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_streamingHostRadioMouseClicked
        if (allHostsRadio.isSelected()) {
            streamingRadio.setEnabled(true);
            downloadRadio.setEnabled(true);
        }
        if (streamingHostRadio.isSelected()) {
            streamingRadio.setEnabled(false);
            downloadRadio.setEnabled(false);
        }
        if (downloadHostRadio.isSelected()) {
            streamingRadio.setEnabled(false);
            downloadRadio.setEnabled(false);
        }
    }//GEN-LAST:event_streamingHostRadioMouseClicked

    private void downloadHostRadioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadHostRadioMouseClicked
        if (allHostsRadio.isSelected()) {
            streamingRadio.setEnabled(true);
            downloadRadio.setEnabled(true);
        }
        if (streamingHostRadio.isSelected()) {
            streamingRadio.setEnabled(false);
            downloadRadio.setEnabled(false);
        }
        if (downloadHostRadio.isSelected()) {
            streamingRadio.setEnabled(false);
            downloadRadio.setEnabled(false);
        }
    }//GEN-LAST:event_downloadHostRadioMouseClicked

    private void clearListsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearListsActionPerformed
        titles.clear();
        hosts.clear();
        links.clear();
        updateProgress("List to crawl reset.");
    }//GEN-LAST:event_clearListsActionPerformed

    private void autofillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autofillActionPerformed
        String search = "";

        if (allHostsRadio.isSelected()) {
            search = "";
        }

        if (streamingHostRadio.isSelected()) {
            search = " - " + streamingHosts.getSelectedItem().toString();
        }

        if (downloadHostRadio.isSelected()) {
            search = " - " + downloadHosts.getSelectedItem().toString();
        }

        outFileNameTextTarget.setText(searchTarget.getText() + search);
    }//GEN-LAST:event_autofillActionPerformed

    private void goToSiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToSiteActionPerformed
        try {
            goToUrl();
        } catch (URISyntaxException ex) {
            Logger.getLogger(AwesomeAllucCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AwesomeAllucCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_goToSiteActionPerformed

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String args[]) throws IOException {
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
            java.util.logging.Logger.getLogger(AwesomeAllucCrawler.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AwesomeAllucCrawler.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AwesomeAllucCrawler.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AwesomeAllucCrawler.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AwesomeAllucCrawler UI = new AwesomeAllucCrawler();

                UI.setVisible(true);

            }

        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CsvExport;
    private javax.swing.JButton GoTarget;
    private javax.swing.JRadioButton allHostsRadio;
    private javax.swing.JCheckBox autoScroll;
    private javax.swing.JButton autofill;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton clearLists;
    private javax.swing.JTextArea currentStatus;
    private javax.swing.JRadioButton downloadHostRadio;
    private javax.swing.JComboBox downloadHosts;
    private javax.swing.JRadioButton downloadRadio;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JTextArea generatedUrls;
    private javax.swing.JButton goToSite;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    public javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton key1;
    private javax.swing.JRadioButton key2;
    private javax.swing.JTextField lockerCount;
    private javax.swing.JTextField lockerFrom;
    public javax.swing.JTextField outFileNameTextTarget;
    private javax.swing.JTextField random1;
    private javax.swing.JTextField random2;
    private javax.swing.JRadioButton randomRadio;
    private javax.swing.ButtonGroup searchOptions;
    private javax.swing.ButtonGroup searchSources;
    private javax.swing.JTextArea searchTarget;
    private javax.swing.JRadioButton staticRadio;
    private javax.swing.JTextField staticWait;
    private javax.swing.JRadioButton streamingHostRadio;
    private javax.swing.JComboBox streamingHosts;
    private javax.swing.JRadioButton streamingRadio;
    private javax.swing.JCheckBox vmExport;
    private javax.swing.JCheckBox waitCheck;
    // End of variables declaration//GEN-END:variables
}
