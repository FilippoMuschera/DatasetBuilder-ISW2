package it.muschera.inforetriver;

import it.muschera.model.JavaClass;
import it.muschera.model.Release;
import it.muschera.model.ReleaseCommits;
import it.muschera.util.JavaClassFinder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class GitInfoRetriever {

    private final Repository repo;

    public GitInfoRetriever(Repository repository) {
        this.repo = repository;
    }

    private static RevCommit getLastCommit(List<RevCommit> commitsList) {

        if (commitsList.isEmpty()) {
            return null;
        }
        RevCommit lastCommit = commitsList.get(0);

        for (RevCommit commit : commitsList) {
            //if commitDate > lastCommitDate then refresh lastCommit
            if (commit.getCommitterIdent().getWhen().after(lastCommit.getCommitterIdent().getWhen())) {
                lastCommit = commit;

            }

        }
        return lastCommit;

    }


    //Associa tutti i commit relativi a una release alla release

    //Ritorna semplicemente una lista di tutti i commit della repository, analizzando ogni branch
    public List<RevCommit> getAllCommits(Git git, Repository repository) throws GitAPIException, IOException {

        List<RevCommit> allCommits = new ArrayList<>();
        List<Ref> allBranches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        //git.checkout().setName("master");


        for (Ref branch : allBranches) {
            Iterable<RevCommit> commitsList = git.log().add(repository.resolve(branch.getName())).call();
            //Iterable<RevCommit> commitsList = git.log().add(repository.resolve("master")).call();

            for (RevCommit commit : commitsList) {

                if (!allCommits.contains(commit)) {
                    allCommits.add(commit);
                }

            }

        }

        return allCommits;
    }

    public ReleaseCommits getCommitsOfRelease(List<RevCommit> commitsList, Release release) {

        List<RevCommit> matchingCommits = new ArrayList<>();
        Date lastDate = release.getLastDate();
        Date firstDate = release.getFirstDate();

        for (RevCommit commit : commitsList) {
            Date commitDate = commit.getCommitterIdent().getWhen();

            //if firstDate < commitDate <= lastDate then add the commit in matchingCommits list
            if (commitDate.after(firstDate) && (commitDate.before(lastDate) || commitDate.equals(lastDate))) {
                matchingCommits.add(commit);

            }

        }
        RevCommit lastCommit = getLastCommit(matchingCommits);

        return new ReleaseCommits(matchingCommits, lastCommit, release);

    }


    public void computeInvolvedCommits(JavaClass javaClass) {

        List<RevCommit> commitsThatModifiedClass = new ArrayList<>();


        for (RevCommit commit : javaClass.getRelease().getReleaseCommits().getCommits()) {


            for (DiffEntry entry : Objects.requireNonNull(JavaClassFinder.getDiffs(commit, this.repo))) {
                //Come da specifica, solo .java (scartiamo i test)
                //controlliamo che il "diff" sia per una modifica e non un add/rename/delete ecc...
                //controlliamo che la classe modificata sia uguale a quella che ci viene passata come parametro
                if (entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/") && (entry.getNewPath().equals(javaClass.getName()))) {
                    commitsThatModifiedClass.add(commit);

                }

            }


        }

        javaClass.setCommitsInvolved(commitsThatModifiedClass);
    }
}
