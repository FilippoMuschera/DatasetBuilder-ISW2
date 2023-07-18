package it.muschera.inforetriver;

import it.muschera.entities.BookkeeperEntity;
import it.muschera.entities.OpenJPAEntity;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import it.muschera.util.JSONUtil;
import it.muschera.util.ReleaseFinder;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import static it.muschera.util.JSONUtil.readJsonFromUrl;
import static java.lang.System.*;

public class JiraInfoRetriever {

    private Map<LocalDateTime, String> releaseNames;
    private Map<LocalDateTime, String> releaseID;
    private List<LocalDateTime> releases;

    private static JiraTicket createTicketInstance(Integer i, JSONArray issues, List<Release> releasesList) throws ParseException {

        JiraTicket ticket = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        try {
            String key = (issues.getJSONObject(i % 1000).get("key").toString());
            JSONObject fields = issues.getJSONObject(i % 1000).getJSONObject("fields");

            String resolutionDateStr = fields.get("resolutiondate").toString();
            String creationDateStr = fields.get("created").toString();
            JSONArray listAV = fields.getJSONArray("versions");

            Date resolutionDate = formatter.parse(resolutionDateStr);
            Date creationDate = formatter.parse(creationDateStr);
            ArrayList<Release> affectedVersionsList = new ArrayList<>();

            for (int k = 0; k < listAV.length(); k++) {
                Release affectedVersion = ReleaseFinder.findByName(listAV.getJSONObject(k).get("name").toString(), releasesList);

                if (affectedVersion != null) {
                    affectedVersionsList.add(affectedVersion);
                }

            }
            Release openVersion = ReleaseFinder.findByDate(creationDate, releasesList);
            Release fixVersion = ReleaseFinder.findByDate(resolutionDate, releasesList);

            if (openVersion != null && fixVersion != null) {
                ticket = new JiraTicket(key, openVersion, fixVersion, affectedVersionsList);


            }

        } catch (JSONException e) {
            /*
             * Se uno dei ticket non ha i campi che ci servono lo scartiamo perchè non è utilizzabile
             */

        }

        return ticket;

    }

    public List<Release> getJiraVersions(String projName) throws IOException, JSONException, ParseException, GitAPIException {

        //Fills the arraylist with releases dates and orders them
        //Ignores releases with missing dates
        releases = new ArrayList<>();
        int i;
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName.toUpperCase();
        JSONObject json = readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        releaseNames = new HashMap<>();
        releaseID = new HashMap<>();
        for (i = 0; i < versions.length(); i++) {
            String name = "";
            String id = "";
            if (versions.getJSONObject(i).has("releaseDate") && versions.getJSONObject(i).get("released").equals(true)) {
                if (versions.getJSONObject(i).has("name"))
                    name = versions.getJSONObject(i).get("name").toString();
                if (versions.getJSONObject(i).has("id"))
                    id = versions.getJSONObject(i).get("id").toString();
                addRelease(versions.getJSONObject(i).get("releaseDate").toString(),
                        name, id);
            }
        }
        // order releases by date
        //@Override
        releases.sort(Comparable::compareTo);

        List<Release> releasesList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date firstDate = formatter.parse("1990-01-01"); //lower bound sulla data per la prima release
        for (i = 0; i < releases.size(); i++) {
            Date lastDate = formatter.parse(String.valueOf(releases.get(i)));
            //La seconda condizione serve a scartare quella versione, che ha durata di soli 3 giorni e non contiene nessun commit associato
            if (projName.equals("BOOKKEEPER")) {
                releasesList.add(new Release(
                        i + 1,
                        Integer.parseInt(releaseID.get(releases.get(i))),
                        releaseNames.get(releases.get(i)),
                        firstDate,
                        lastDate,
                        BookkeeperEntity.getInstance()
                ));
            } else if (projName.equals("OPENJPA")) {
                releasesList.add(new Release(
                        i + 1,
                        Integer.parseInt(releaseID.get(releases.get(i))),
                        releaseNames.get(releases.get(i)),
                        firstDate,
                        lastDate,
                        OpenJPAEntity.getInstance()
                ));
            } else {

                //Cold start case
                releasesList.add(new Release(
                        i + 1,
                        Integer.parseInt(releaseID.get(releases.get(i))),
                        releaseNames.get(releases.get(i)),
                        firstDate,
                        lastDate
                ));

            }
            firstDate = lastDate;
        }
        this.writeToCsv(releasesList, projName);

        return releasesList;
    }

    private void writeToCsv(List<Release> releasesList, String projName) {

        if (!projName.equalsIgnoreCase("BOOKKEEPER") && !projName.equalsIgnoreCase("OPENJPA"))
            return; //Non voglio il csv dei progetti con cui faccio solo cold start

        String outName = projName + "VersionInfo.csv";
        try (
                FileWriter fileWriter = new FileWriter(outName)

        ) {

            //Name of CSV for output
            fileWriter.append("Index,Version ID,Version Name,FirstDate,LastDate");
            fileWriter.append("\n");
            int i = 1;
            for (Release release : releasesList) {

                fileWriter.append(Integer.toString(i));
                fileWriter.append(",");
                fileWriter.append(Integer.toString(release.getId()));
                fileWriter.append(",");
                fileWriter.append(release.getName());
                fileWriter.append(",");
                fileWriter.append(release.getFirstDate().toString());
                fileWriter.append(",");
                fileWriter.append(release.getLastDate().toString());
                fileWriter.append("\n");
                i++;
            }
            fileWriter.flush();

        } catch (Exception e) {
            out.println("Error in csv writer");
            Logger logger = Logger.getLogger(JiraInfoRetriever.class.getName());
            logger.info(e.getMessage());
        }

    }

    public void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();
        if (!releases.contains(dateTime))
            releases.add(dateTime);
        releaseNames.put(dateTime, name);
        releaseID.put(dateTime, id);
    }

    public List<JiraTicket> getAllJiraTickets(List<Release> releasesList, String projName) throws JSONException, IOException, ParseException {

        List<JiraTicket> ticketsList = new ArrayList<>();

        Integer i = 0;
        int j = 0;
        int total = 1;

        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs > 1000
            j = i + 1000;

            /* The query in Jira is:
             * project = <projKey> AND issuetype = Bug AND (status = Closed OR status = Resolved) AND resolution = Fixed */
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName.toUpperCase() + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j;

            JSONObject json = JSONUtil.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (; i < total && i < j; i++) {

                JiraTicket ticket = createTicketInstance(i, issues, releasesList);
                if (ticket != null) {
                    ticketsList.add(ticket);
                }

            }

        } while (i < total);

        return ticketsList;

    }

}
