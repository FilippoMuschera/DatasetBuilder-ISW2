package it.muschera.model;

import it.muschera.entities.ProjectInterface;
import it.muschera.inforetriver.GitInfoRetriever;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Release {

    private final Repository repository;


    private int index;
    private int id;
    private String name;
    private Date lastDate; //data in cui la release viene effettivamente rilasciata
    private Date firstDate; //data in cui iniziano i commit di questa release
    private ReleaseCommits releaseCommits;


    public Release(int index, int id, String name, Date firstDate, Date lastDate, ProjectInterface project) throws GitAPIException, IOException {

        this.index = index;
        this.id = id;
        this.name = name;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.repository = project.getRepository();
        this.addCommits(project);
    }

    public Release(int index, int id, String name, Date firstDate, Date lastDate) {

        this.index = index;
        this.id = id;
        this.name = name;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.repository = null;

    }


    private void addCommits(ProjectInterface projectInterface) throws GitAPIException, IOException {
        //Data la corrente release, gli associamo tutti i commit che la compongono
        GitInfoRetriever gitInfoRetriever = new GitInfoRetriever();
        List<RevCommit> allCommits = gitInfoRetriever.getAllCommits(projectInterface.getGit(), projectInterface.getRepository());
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
