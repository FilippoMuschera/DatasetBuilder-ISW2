package it.muschera.entities;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import static java.lang.System.*;

public class OpenJPAEntity implements ProjectInterface {

    private static OpenJPAEntity singletonEntity = null;

    private Repository repository = null;


    private Git git = null;

    private OpenJPAEntity() {
        try (InputStream input = new FileInputStream("src/main/resources/openjpa.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            String jpaRepoPath = properties.getProperty("jpa.path");
            this.repository = new RepositoryBuilder().setGitDir(new File(jpaRepoPath)).build();
            this.git = new Git(this.repository);
        } catch (IOException e) {
            err.println("Path of Repository may be incorrect");
            Logger logger = Logger.getLogger(OpenJPAEntity.class.getName());
            logger.info(e.getMessage());
        }
    }

    public static OpenJPAEntity getInstance() {
        if (singletonEntity == null) {
            singletonEntity = new OpenJPAEntity();
        }
        return singletonEntity;
    }

    public Repository getRepository() {
        return repository;
    }

    public Git getGit() {
        return git;
    }


    public void checkInit() {
        if (this.git == null || this.repository == null) {
            throw new IllegalStateException("Impossibile inizializzare la repository di OpenJPA");
        }
    }

}
