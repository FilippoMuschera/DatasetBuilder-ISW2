package it.muschera.inforetriver;

import it.muschera.entities.BookkeeperEntity;
import it.muschera.model.JavaClass;
import it.muschera.model.Release;
import it.muschera.model.ReleaseCommits;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitInfoRetriever {

    private final Repository repo;

    public GitInfoRetriever(Repository repository) {this.repo = repository;}

    private static RevCommit getLastCommit(List<RevCommit> commitsList, Release release) {

        RevCommit lastCommit;
        try {
            lastCommit = commitsList.get(0);
        } catch (IndexOutOfBoundsException e){
            System.out.println(release.getId() + " --- " + release.getName());
            return null;
        }
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
    public List<RevCommit> getAllCommits() throws GitAPIException, IOException {
        Git git = BookkeeperEntity.getInstance().getGit();
        Repository repo = BookkeeperEntity.getInstance().getRepository();
        List<RevCommit> allCommits = new ArrayList<>();
        List<Ref> allBranches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        for (Ref branch : allBranches) {
            Iterable<RevCommit> commitsList = git.log().add(repo.resolve(branch.getName())).call();
            for (RevCommit commit : commitsList) {

                if (!allCommits.contains(commit)) {
                    allCommits.add(commit);
                }

            }

        }

        return allCommits;
    }

    public ReleaseCommits getCommitsOfRelease(List<RevCommit> commitsList, Release release) throws IOException {

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
        RevCommit lastCommit = getLastCommit(matchingCommits, release);

        return new ReleaseCommits(matchingCommits, lastCommit, release);

    }


    public void computeInvolvedCommits(JavaClass javaClass) {

        List<RevCommit> commitsThatModifiedClass = new ArrayList<>();


        for (RevCommit commit : javaClass.getRelease().getReleaseCommits().getCommits()) {

            try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                 ObjectReader reader = this.repo.newObjectReader()) {

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                ObjectId newTree = commit.getTree();
                newTreeIter.reset(reader, newTree);

                RevCommit commitParent = commit.getParent(0);    //commit precedente a quello attuale del ciclo for
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                ObjectId oldTree = commitParent.getTree();
                oldTreeIter.reset(reader, oldTree);

                diffFormatter.setRepository(this.repo);
                List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter); //diff tra i due commit

                for (DiffEntry entry : entries) {
                    //Come da specifica, solo .java (scartiamo i test)
                    //controlliamo che il "diff" sia per una modifica e non un add/rename/delete ecc...
                    //controlliamo che la classe modificata sia uguale a quella che ci viene passata come parametro
                    if (entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/") && (entry.getNewPath().equals(javaClass.getName()))){
                            commitsThatModifiedClass.add(commit);

                    }

                }

            } catch (ArrayIndexOutOfBoundsException | IOException e) {
                //se il commit non ne ha uno precedente lo salto
            }
        }

        javaClass.setCommitsInvolved(commitsThatModifiedClass);
    }
}
