package br.visualize.octavesourcevisualization.rest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class AccessGitRepo {

	private String directory = "/tmp/octave-kdm-visualization_"+UUID.randomUUID();
	
	public boolean importProject(String urlProject){
		boolean success = true;
		try {
			cloneRepository(urlProject);
		} catch (InvalidRemoteException e) {
			success = false;
		} catch (TransportException e) {
			success = false;
		} catch (IOException e) {
			success = false;
		} catch (GitAPIException e) {
			success = false;
		}
		return success;
	}
	
	public void cloneRepository(String urlGitProject) throws IOException, InvalidRemoteException, TransportException, GitAPIException{
		Git.cloneRepository().setURI(urlGitProject)
		.setDirectory(new File(directory)).call().getRepository();
	}
	
	public File getRepoFile(){
		return new File(directory);
	}
	
	public void deleteDirectory(){
		File f = new File(directory);
		try{
			if(f.delete())
				System.out.println("projeto apagado");
			else
				System.out.println("projeto não foi apagado");
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}