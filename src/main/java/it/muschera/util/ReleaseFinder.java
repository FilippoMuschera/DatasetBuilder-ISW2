package it.muschera.util;

import it.muschera.model.Release;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReleaseFinder {

    private ReleaseFinder() {
        //solo metodi statici
    }

    public static Release findByName(String releaseName, List<Release> releaseList){

        for (Release release : releaseList){
            if (release.getName().equals(releaseName))
                return release;
        }

        return null;

    }

    public static Release findByDate(Date ticketDate, List<Release> releaseList){

        for (Release release : releaseList){
            if (ticketDate.after(release.getFirstDate()) && ticketDate.before(release.getLastDate()))
                return release;
        }

        return null;
    }

    public static Release findByCommit(RevCommit commit, List<Release> releaseList) {
        for (Release rel : releaseList) {
            if (rel.getReleaseCommits().getCommits().contains(commit)){
                return rel;
            }
        }
        return null;
    }

    public static List<Release> cleanReleaseList(List<Release> releasesList) {
        releasesList.removeIf(r -> r.getReleaseCommits().getLastCommit() == null || r.getReleaseCommits().getJavaClasses() == null);

        return releasesList;
    }

    /*
     * Serve per avere i numeri di release tutti consecutivi
     */
    public static List<Release> refactorReleaseList(List<Release> releaseList) {
        int i = 1;
        List<Release> adjustedReleaseList = new ArrayList<>();
        for (Release release : releaseList) {
            release.setIndex(i);
            adjustedReleaseList.add(release);
        }

        return adjustedReleaseList;
    }
}
