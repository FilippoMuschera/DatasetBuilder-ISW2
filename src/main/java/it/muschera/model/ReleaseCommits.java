package it.muschera.model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//Questa classe, che viene referenziata da una classe Release, racchiude tutti i commit di quella Release
public class ReleaseCommits {

    private List<RevCommit> commits;
    private RevCommit lastCommit;
    private Map<String, String> javaClasses;  //Mantiene traccia del nome e del contenuto di tutte le classi presenti al momento della release
    // (aka ultimo commit prima di quella release)

    public ReleaseCommits(List<RevCommit> commits, RevCommit lastCommit, Release release) {
        this.commits = commits;
        this.lastCommit = lastCommit;
        if ((lastCommit != null))
            setJavaClasses(lastCommit, release);
        else
            this.javaClasses = null;

    }


    public List<RevCommit> getCommits() {
        return commits;
    }


    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }


    public RevCommit getLastCommit() {
        return lastCommit;
    }


    public void setLastCommit(RevCommit lastCommit) {
        this.lastCommit = lastCommit;
    }


    public Map<String, String> getJavaClasses() {
        return javaClasses;
    }

    private void setJavaClasses(RevCommit commit, Release release) {

        this.javaClasses = new HashMap<>();

        RevTree tree = commit.getTree();    //Prende la struttura dei file al momento del commit
        try (TreeWalk treeWalk = new TreeWalk(release.getRepository())) {    //We use a TreeWalk to iterate over all files in the Tree recursively
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                //Non vogliamo i test
                if (treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                    //Prendiamo nome della classe e il suo codice
                    javaClasses.put(treeWalk.getPathString(), new String(release.getRepository().open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            Logger logger = Logger.getLogger(ReleaseCommits.class.getName());
            logger.info(e.getMessage());
        }

    }

}
