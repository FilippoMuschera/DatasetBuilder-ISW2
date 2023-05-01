package it.muschera.util;

import it.muschera.model.JavaClass;
import it.muschera.model.JiraTicket;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.*;

public class JavaClassFinder {

    private JavaClassFinder() {
        //tutti metodi statici
    }

    public static List<String> getModifiedClasses(RevCommit commit, Repository repo) {

        List<String> modifiedClasses = new ArrayList<>();
        List<DiffEntry> entries = getDiffs(commit, repo);



            //Every entry contains info for each file involved in the commit (old path name, new path name, change type (that could be MODIFY, ADD, RENAME, etc.))
        assert entries != null;
        for (DiffEntry entry : entries) {
                //We are keeping only Java classes that are not involved in tests
                if (entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    modifiedClasses.add(entry.getNewPath());
                }

            }
        return modifiedClasses;

        }



    public static List<DiffEntry> getDiffs(RevCommit commit, Repository repo) {
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
             ObjectReader reader = repo.newObjectReader()) {

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId treeOfCommit = commit.getTree();
            newTreeIter.reset(reader, treeOfCommit);

            RevCommit commitParent = commit.getParent(0);    //It's the previous commit of the commit we are considering
            CanonicalTreeParser parentTree = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            parentTree.reset(reader, oldTree);

            diffFormatter.setRepository(repo);

            return diffFormatter.scan(parentTree, newTreeIter);
        } catch (ArrayIndexOutOfBoundsException e) {
            //commit has no parents: skip this commit, return an empty list and go on

        } catch (IOException e) {
            err.println("Errore nel diff tra i commit [JavaClassFinder]");
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static void markAsBuggy(List<JavaClass> javaClassList, String modifiedClass, JiraTicket ticket) {

        //cerco la classe modificata tra la mia lista di classi
        for (JavaClass javaClass : javaClassList) {
            //Trovata la classe, se è quella corrispondente a una versione iv <= versioneClasse < fv => la marco come buggy
            if (javaClass.getName().equals(modifiedClass) && (javaClass.getRelease().getIndex() >= ticket.getInjectedVersion().getIndex() && javaClass.getRelease().getIndex() < ticket.getFixVersion().getIndex())) {

                    javaClass.setBuggy(true);

            }
        }

    }
}
