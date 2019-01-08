/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lolstuff;

import com.google.gson.Gson;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.sound.sampled.*;




/**
 *
 * @author bcole_000
 */
public class leagueGameAssist extends javax.swing.JFrame {

    String key = "993d24c8-e3a8-4783-b827-bbce76a07146";

    private riotWorker worker;
    private timerWorker tWorker;

    class riotWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws Exception {
            runCurrentGame();
            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {

        }
    }

    class timerWorker extends SwingWorker<Void, Void> {

        /**
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws Exception {
            while (minimapButton.isSelected()) {
                minimapReminder();
            }
            return null;
        }

        /**
         * Executed in event dispatching thread
         */
        @Override
        public void done() {

        }
    }

    public String jsonConnection(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            Thread.sleep(1000);
            URL url = new URL(urlString);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.connect();
            int rc = huc.getResponseCode();
            if (rc == 404) {
                return ("No data available.");
            } else {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuffer buffer = new StringBuffer();
                int read;
                char[] chars = new char[1024];
                while ((read = reader.read(chars)) != -1) {
                    buffer.append(chars, 0, read);
                }
                return buffer.toString();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void update(String str) {
        output.append(str + "\n");
    }

    public void hyphens() {
        update("--------------------------");
        String something = "1";
        int number = 1;
        double numberdecimal = 1.0000111;
        
    }

    public void updateHyphens(String text) {
        update("----------" + text + "----------");
    }

    public void updateCT(int index, String text) {
        if (index == 1) {
            champText1.append(text + "\n");
        }
        if (index == 2) {
            champText2.append(text + "\n");
        }
        if (index == 3) {
            champText3.append(text + "\n");
        }
        if (index == 4) {
            champText4.append(text + "\n");
        }
        if (index == 5) {
            champText5.append(text + "\n");
        }
        if (index == 6) {
            champText6.append(text + "\n");
        }
        if (index == 7) {
            champText7.append(text + "\n");
        }
        if (index == 8) {
            champText8.append(text + "\n");
        }
        if (index == 9) {
            champText9.append(text + "\n");
        }
        if (index == 10) {
            champText10.append(text + "\n");
        }
    }

    //Get Champion URL
    public String champData(String id) throws Exception {
        String document = jsonConnection("https://global.api.pvp.net/api/lol/static-data/na/v1.2/champion/" + id + "?champData=all&api_key=" + key);
        return document;
    }

    //Get Champion data
    public class champData {

        String name;
        List<champSpells> spells;
    }

    //Get Summoner info URL
    public String summData(String id) throws Exception {
        String document = jsonConnection("https://na.api.pvp.net/api/lol/na/v1.3/stats/by-summoner/" + id + "/ranked?season=SEASON2015&api_key=" + key);
        document = document.replace("\"stats\":{", "\"stats\":[{");
        document = document.replace("}}", "}]}");
        return document;
    }

    //Get Summoner info
    public class summoner {

        List<champInfo> champions;
    }

    //Summoner Champs
    public class champInfo {

        String id;
        List<champStats> stats;
    }

    public class champStats {

        int totalDeathsPerSession;
        int totalSessionsPlayed;
        int totalDoubleKills;
        int totalQuadraKills;
        int totalTripleKills;
        int totalPentaKills;
        int totalMinionKills;
        int totalFirstBlood;
        int mostChampionKillsPerSession;
        int totalAssists;
        int totalChampionKills;
        int totalGoldEarned;
        int totalSessionsWon;
        int totalSessionsLost;
        int totalTurretsKilled;
    }

    //Champion Spells
    public class champSpells {

        String name;
        String cooldownBurn;
        String costBurn;
        String costType;
        String description;
    }

    //Current game
    public void runCurrentGame() throws Exception {
        String doc = currentGame();
        Gson gson = new Gson();
        currentGameResult cgr = gson.fromJson(doc, currentGameResult.class);
        updateHyphens("Current Game");
        update("Game mode: " + cgr.gameMode);
        update("Game type: " + cgr.gameType);
        hyphens();
        int champCounter = 1;

        //Banned champs
        updateHyphens("Banned Champs");
        updateHyphens("Team 1");
        for (bannedChamps i : cgr.bannedChampions) {
            Gson gson1 = new Gson();
            String champ = champData(i.championId);
            champData cd = gson1.fromJson(champ, champData.class);
            if (i.teamId.equals("100")) {
                update(i.pickTurn + ") " + cd.name);
            }
        }
        updateHyphens("Team 2");
        for (bannedChamps i : cgr.bannedChampions) {
            Gson gson1 = new Gson();
            String champ = champData(i.championId);
            champData cd = gson1.fromJson(champ, champData.class);
            if (i.teamId.equals("200")) {
                update(i.pickTurn + ") " + cd.name);
            }
        }

        //Prepare to output data
        for (champsInGame i : cgr.participants) {
            summoner sum;
            String name = "";
            String id = "";
            Gson gson1 = new Gson();
            String champ = champData(i.championId);
            champData cd = gson1.fromJson(champ, champData.class);
            Gson gson2 = new Gson();
            String summData = summData(i.summonerId);

            //Output data
            if (!summData.equals("No data available.")) {
                sum = gson2.fromJson(summData, summoner.class);
                name = cd.name;
                id = i.championId;
                System.out.println(sum.champions);
                if (name.equals(null)) {
                    System.out.println("null");
                }
                if (name.isEmpty()) {
                    System.out.println("empty");
                }
                if (name.matches("")) {
                    System.out.println("matches");
                }
                if (champCounter == 1) {
                    champName1.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {
                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }

                if (champCounter == 2) {
                    champName2.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {
                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 3) {
                    champName3.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 4) {
                    champName4.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 5) {
                    champName5.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 6) {
                    champName6.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 7) {
                    champName7.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 8) {
                    champName8.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 9) {
                    champName9.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
                if (champCounter == 10) {
                    champName10.setText(name);
                    for (champInfo o : sum.champions) {
                        String sid = o.id;
                        for (champStats q : o.stats) {

                            if (id.equals(sid)) {
                                updateCT(champCounter, "Total Games Played: " + q.totalSessionsPlayed);
                                updateCT(champCounter, "W/L: " + q.totalSessionsWon + "/" + q.totalSessionsLost);
                                updateCT(champCounter, "Highest Kill Count: " + q.mostChampionKillsPerSession);
                                updateCT(champCounter, "Average CS: " + (q.totalMinionKills / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average KDA: " + (q.totalChampionKills / q.totalSessionsPlayed) + "/" + (q.totalDeathsPerSession / q.totalSessionsPlayed) + "/" + (q.totalAssists / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Turrets Killed: " + (q.totalTurretsKilled / q.totalSessionsPlayed));
                                updateCT(champCounter, "Average Gold: " + (q.totalGoldEarned / q.totalSessionsPlayed));
                                updateCT(champCounter, "Total Champ Kills: " + q.totalChampionKills);
                                updateCT(champCounter, "Total First Bloods: " + q.totalFirstBlood);
                                updateCT(champCounter, "Total Double Kills: " + q.totalDoubleKills);
                                updateCT(champCounter, "Total Triple Kills: " + q.totalTripleKills);
                                updateCT(champCounter, "Total Quadra Kills: " + q.totalQuadraKills);
                                updateCT(champCounter, "Total Penta Kills: " + q.totalPentaKills);

                                updateCT(champCounter, "--------Spell Info--------");
                                for (champSpells p : cd.spells) {
                                    updateCT(champCounter, "Name: " + p.description);
                                    updateCT(champCounter, "Description: " + p.description);
                                    updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                                    updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                                    updateCT(champCounter, "--------------------------");
                                }
                            }
                        }
                    }
                }
            } else {
                if (champCounter == 1) {
                    champName1.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }

                if (champCounter == 2) {
                    champName2.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 3) {
                    champName3.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 4) {
                    champName4.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 5) {
                    champName5.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 6) {
                    champName6.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 7) {
                    champName7.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 8) {
                    champName8.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 9) {
                    champName9.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
                if (champCounter == 10) {
                    champName10.setText(name);
                    updateCT(champCounter, "--------Spell Info--------");
                    for (champSpells p : cd.spells) {
                        updateCT(champCounter, "Name: " + p.description);
                        updateCT(champCounter, "Description: " + p.description);
                        updateCT(champCounter, "Costs: " + p.costBurn + " " + p.costType);
                        updateCT(champCounter, "Cooldown: " + p.cooldownBurn);
                        updateCT(champCounter, "--------------------------");
                    }
                }
            }
            champCounter++;
        }

        champCounter = 0;
    }

    public String currentGame() throws Exception {
        String sumID = summonerID.getText();
        update("Opening current game...");
        String document = jsonConnection("https://na.api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/NA1/" + sumID + "?api_key=" + key);
        return document;

    }

    public class currentGameResult {

        String gameMode;
        String mapId;
        String gameType;
        List<bannedChamps> bannedChampions;
        List<champsInGame> participants;
    }

    public class bannedChamps {

        String pickTurn;
        String championId;
        String teamId;
    }

    public class champsInGame {

        String summonerName;
        String championId;
        String teamId;
        String summonerId;

        List<mastery> masteries;
        List<rune> runes;
    }

    public class mastery {

        String rank;
        String masteryId;
    }

    public class rune {

        String count;
        String runeId;
    }

    public void minimapReminder() throws InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
        int timer = Integer.parseInt(minimap.getText());
        timer = timer * 1000;
        File soundFile = new File("ding.wav");
        AudioInputStream audioStream;
        AudioFormat audioFormat;
        SourceDataLine sourceLine;
        audioStream = AudioSystem.getAudioInputStream(soundFile);
        audioFormat = audioStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceLine.open(audioFormat);

        Thread.sleep(timer);
        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[128000];
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }

    /**
     * Creates new form leaguepoop
     */
    public leagueGameAssist() {
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        output = new javax.swing.JTextArea();
        strategyText = new javax.swing.JScrollPane();
        jTextArea6 = new javax.swing.JTextArea();
        summonerID = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        champText1 = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        champText2 = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        champText3 = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        champText4 = new javax.swing.JTextArea();
        jScrollPane9 = new javax.swing.JScrollPane();
        champText6 = new javax.swing.JTextArea();
        jScrollPane10 = new javax.swing.JScrollPane();
        champText9 = new javax.swing.JTextArea();
        jScrollPane11 = new javax.swing.JScrollPane();
        champText7 = new javax.swing.JTextArea();
        jScrollPane12 = new javax.swing.JScrollPane();
        champText8 = new javax.swing.JTextArea();
        champName1 = new javax.swing.JLabel();
        champName6 = new javax.swing.JLabel();
        champName2 = new javax.swing.JLabel();
        champName7 = new javax.swing.JLabel();
        champName3 = new javax.swing.JLabel();
        champName8 = new javax.swing.JLabel();
        champName4 = new javax.swing.JLabel();
        champName9 = new javax.swing.JLabel();
        champName5 = new javax.swing.JLabel();
        champName10 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        champText5 = new javax.swing.JTextArea();
        jScrollPane14 = new javax.swing.JScrollPane();
        champText10 = new javax.swing.JTextArea();
        minimap = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        minimapButton = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        output.setEditable(false);
        output.setColumns(20);
        output.setRows(5);
        jScrollPane1.setViewportView(output);

        jTextArea6.setColumns(20);
        jTextArea6.setRows(5);
        strategyText.setViewportView(jTextArea6);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(strategyText)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .addComponent(strategyText))
                .addContainerGap())
        );

        summonerID.setText("59230524");

        jLabel1.setText("Summoner ID");

        runButton.setText("Go");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        champText1.setColumns(20);
        champText1.setRows(5);
        jScrollPane3.setViewportView(champText1);

        champText2.setColumns(20);
        champText2.setRows(5);
        jScrollPane4.setViewportView(champText2);

        champText3.setColumns(20);
        champText3.setRows(5);
        jScrollPane5.setViewportView(champText3);

        champText4.setColumns(20);
        champText4.setRows(5);
        jScrollPane6.setViewportView(champText4);

        champText6.setColumns(20);
        champText6.setRows(5);
        jScrollPane9.setViewportView(champText6);

        champText9.setColumns(20);
        champText9.setRows(5);
        jScrollPane10.setViewportView(champText9);

        champText7.setColumns(20);
        champText7.setRows(5);
        jScrollPane11.setViewportView(champText7);

        champText8.setColumns(20);
        champText8.setRows(5);
        jScrollPane12.setViewportView(champText8);

        champText5.setColumns(20);
        champText5.setRows(5);
        jScrollPane8.setViewportView(champText5);

        champText10.setColumns(20);
        champText10.setRows(5);
        jScrollPane14.setViewportView(champText10);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(105, 105, 105)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(champName1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(champName6, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(228, 228, 228)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(champName2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(champName7, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(231, 231, 231)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(champName3, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(champName8, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane9))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                            .addComponent(jScrollPane12))))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                            .addComponent(jScrollPane6))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(champName4, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(champName9, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(123, 123, 123)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(champName10, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(champName5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(116, 116, 116))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addComponent(jScrollPane6)
                    .addComponent(jScrollPane4)
                    .addComponent(jScrollPane5)
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(champName1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(champName2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(champName3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(champName6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(champName7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(champName8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(champName4, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(champName9, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                            .addComponent(jScrollPane11)
                            .addComponent(jScrollPane12)
                            .addComponent(jScrollPane10)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(champName5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(champName10, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        minimap.setText("6");

        jLabel2.setText("Timer");

        minimapButton.setText("Minimap Reminder");
        minimapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimapButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(runButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(summonerID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minimap, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(minimapButton)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summonerID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runButton)
                    .addComponent(minimap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minimapButton))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        worker = new riotWorker();
        worker.execute();
    }//GEN-LAST:event_runButtonActionPerformed

    private void minimapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimapButtonActionPerformed
        tWorker = new timerWorker();
        tWorker.execute();
    }//GEN-LAST:event_minimapButtonActionPerformed

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
            java.util.logging.Logger.getLogger(leagueGameAssist.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(leagueGameAssist.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(leagueGameAssist.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(leagueGameAssist.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new leagueGameAssist().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel champName1;
    private javax.swing.JLabel champName10;
    private javax.swing.JLabel champName2;
    private javax.swing.JLabel champName3;
    private javax.swing.JLabel champName4;
    private javax.swing.JLabel champName5;
    private javax.swing.JLabel champName6;
    private javax.swing.JLabel champName7;
    private javax.swing.JLabel champName8;
    private javax.swing.JLabel champName9;
    private javax.swing.JTextArea champText1;
    private javax.swing.JTextArea champText10;
    private javax.swing.JTextArea champText2;
    private javax.swing.JTextArea champText3;
    private javax.swing.JTextArea champText4;
    private javax.swing.JTextArea champText5;
    private javax.swing.JTextArea champText6;
    private javax.swing.JTextArea champText7;
    private javax.swing.JTextArea champText8;
    private javax.swing.JTextArea champText9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextArea jTextArea6;
    private javax.swing.JTextField minimap;
    private javax.swing.JToggleButton minimapButton;
    private javax.swing.JTextArea output;
    private javax.swing.JButton runButton;
    private javax.swing.JScrollPane strategyText;
    private javax.swing.JTextField summonerID;
    // End of variables declaration//GEN-END:variables
}
