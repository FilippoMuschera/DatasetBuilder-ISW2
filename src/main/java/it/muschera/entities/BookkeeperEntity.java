package it.muschera.entities;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.System.*;

public class BookkeeperEntity {

    private static BookkeeperEntity singletonEntity = null;

    private Repository repository = null;


    private Git git = null;

    private BookkeeperEntity() {
        try (InputStream input = new FileInputStream("src/main/resources/bookkeeper.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            String bookkeeperRepoPath = properties.getProperty("bk.path");
            this.repository = new RepositoryBuilder().setGitDir(new File(bookkeeperRepoPath)).build();
            this.git = new Git(this.repository);
        } catch (IOException e) {
            err.println("Path of Repository may be incorrect");
            e.printStackTrace();
        }
    }

    public static BookkeeperEntity getInstance() {
        if (singletonEntity == null) {
            singletonEntity = new BookkeeperEntity();
        }
        return singletonEntity;
    }

    public Repository getRepository() {
        return repository;
    }

    public Git getGit() {
        return git;
    }


}
