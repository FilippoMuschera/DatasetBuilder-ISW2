package it.muschera.entities;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public interface ProjectInterface {


    Repository getRepository();

    Git getGit();

}
