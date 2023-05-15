package it.muschera.util;

import it.muschera.filescreators.EnumFileType;
import it.muschera.model.JavaClass;
import it.muschera.model.JiraTicket;
import it.muschera.model.Release;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.*;

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

    public static void markAsBuggy(List<JavaClass> javaClassList, String modifiedClass, JiraTicket ticket, EnumFileType type) {

        //cerco la classe modificata tra la mia lista di classi
        for (JavaClass javaClass : javaClassList) {
            //Trovata la classe, se è quella corrispondente a una versione iv <= versioneClasse < fv => la marco come buggy
            if (javaClass.getName().equals(modifiedClass) && (javaClass.getRelease().getIndex() >= ticket.getInjectedVersion().getIndex() && javaClass.getRelease().getIndex() < ticket.getFixVersion().getIndex())) {

                if (type.equals(EnumFileType.TRAINING)) {
                    javaClass.setBuggyRealistic(true);
                    javaClass.setnFix(javaClass.getnFix() + 1);
                } else if (type.equals(EnumFileType.TESTING))
                    javaClass.setBuggyPrecise(true);

            }
        }

    }

    public static JavaClass cloneJavaClass(JavaClass javaClass) {
        JavaClass newJavaClass = new JavaClass(
                javaClass.getName(),
                javaClass.getContent(),
                javaClass.getRelease()
        );

        newJavaClass.setCommitsInvolved(javaClass.getCommitsInvolved());
        return newJavaClass;

    }

    public static void computeCommitsOfClass(List<JavaClass> allJavaClassList, List<Release> releaseList, Repository repository) throws IOException {

        Map<RevCommit, Integer> commitAndRelease = new HashMap<>();
        for (Release release : releaseList) {
            for (RevCommit commit : release.getReleaseCommits().getCommits()) {
                commitAndRelease.put(commit, release.getIndex());
            }
        }
        for (Map.Entry<RevCommit, Integer> entry : commitAndRelease.entrySet()) {

            List<String> modifiedClasses = JavaClassFinder.getClassesModifiedByCommit(entry.getKey(), repository);

            for (String modifClass : modifiedClasses) {
                JavaClassFinder.updateJavaClassCommits(allJavaClassList, modifClass, entry.getValue(), entry.getKey());


            }

        }

    }

    public static List<String> getClassesModifiedByCommit(RevCommit commit, Repository repo) throws IOException {
        List<String> modifiedClasses = new ArrayList<>();    //Here there will be the names of the classes that have been modified by the commit

        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
             ObjectReader reader = repo.newObjectReader()) {

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);

            RevCommit commitParent = commit.getParent(0);    //It's the previous commit of the commit we are considering
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = commitParent.getTree();
            oldTreeIter.reset(reader, oldTree);

            diffFormatter.setRepository(repo);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            //Every entry contains info for each file involved in the commit (old path name, new path name, change type (that could be MODIFY, ADD, RENAME, etc.))
            for (DiffEntry entry : entries) {
                //We are keeping only Java classes that are not involved in tests
                if (entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                    modifiedClasses.add(entry.getNewPath());
                }

            }

        } catch (ArrayIndexOutOfBoundsException e) {
            //commit has no parents: skip this commit, return an empty list and go on

        }

        return modifiedClasses;
    }


    public static void updateJavaClassCommits(List<JavaClass> javaClasses, String className, Integer associatedRelease, RevCommit commit) {

        for (JavaClass javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to the same release of commit, then add commit to javaClass.commits
            if (javaClass.getName().equals(className) && javaClass.getRelease().getIndex() == associatedRelease && !javaClass.getCommitsInvolved().contains(commit)) {
                javaClass.getCommitsInvolved().add(commit);

            }

        }

    }

}
