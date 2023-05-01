package it.muschera.util;

import it.muschera.model.Release;
import org.eclipse.jgit.revwalk.RevCommit;

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
}
