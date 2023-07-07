package it.muschera.inforetriver;

import it.muschera.model.Release;
import it.muschera.model.ReleaseCommits;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GitInfoRetriever {


    private static RevCommit getLastCommit(List<RevCommit> commitsList) {

        if (commitsList.isEmpty()) {
            return null;
        }
        RevCommit lastCommit = commitsList.get(0);

        for (RevCommit commit : commitsList) {
            //se commitDate > lastCommitDate refresh del lastCommit
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


        for (Ref branch : allBranches) {
            Iterable<RevCommit> commitsList = git.log().add(repository.resolve(branch.getName())).call();

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

            //Se firstDate < commitDate <= lastDate aggiungo il commit
            if (commitDate.after(firstDate) && (commitDate.before(lastDate) || commitDate.equals(lastDate))) {
                matchingCommits.add(commit);

            }

        }
        RevCommit lastCommit = getLastCommit(matchingCommits);

        return new ReleaseCommits(matchingCommits, lastCommit, release);

    }


}
