package it.muschera.model;

import it.muschera.inforetriver.GitInfoRetriever;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Release {

    private final Repository repository;
    private int id;
    private String name;
    private Date lastDate;
    private Date firstDate;
    private ReleaseCommits releaseCommits;


    public Release(int id, String name, Date firstDate, Date lastDate, Repository repository) throws GitAPIException, IOException {
        this.id = id;
        this.name = name;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.repository = repository;
        this.addCommits();
    }

    private void addCommits() throws GitAPIException, IOException {
        //Data la corrente release, gli associamo tutti i commit che la compongono
        GitInfoRetriever gitInfoRetriever = new GitInfoRetriever(this.repository);
        List<RevCommit> allCommits = gitInfoRetriever.getAllCommits();
        this.releaseCommits = gitInfoRetriever.getCommitsOfRelease(allCommits, this);
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public ReleaseCommits getReleaseCommits() {
        return releaseCommits;
    }

    public Date getFirstDate() {
        return firstDate;
    }

    public void setFirstDate(Date firstDate) {
        this.firstDate = firstDate;
    }

    public Repository getRepository() {
        return repository;
    }

}
