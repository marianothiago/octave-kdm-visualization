package br.visualize.octavesourcevisualization.rest;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;

import br.octave.structureKDM.KDMSegment;
import br.visualize.octavesourcevisualization.controller.KDMParseJson;
import br.visualize.octavesourcevisualization.controller.OctaveController;
import br.visualize.octavesourcevisualization.dao.KDMOctaveDAO;

@Path("/kdm")
public class OctaveService {

	@Context
	private HttpServletRequest request;
	
	@GET
	@Path("/importaProjeto/{param}")
	public Response importProject(@PathParam("param")String nameProject, @Encoded @QueryParam("urlGitProject")String urlGitProject) throws Exception {
		String result = "The project wasn't imported";
		AccessGitRepo gitRepo = new AccessGitRepo();
		boolean wasImported = gitRepo.importProject(urlGitProject);
		if(wasImported){
			result = "The project was imported";
			KDMSegment segment = OctaveController.loadSegment(gitRepo.getRepoFile(),nameProject);
//	        request.getSession().setAttribute("kdmSegment", segment);
			gitRepo.deleteDirectory();
			KDMParseJson kdmJsonParse = new KDMParseJson(nameProject, segment);
			JsonObject kdmJson = kdmJsonParse.createJSON();
			KDMOctaveDAO dao = new KDMOctaveDAO();
			dao.save(kdmJson,nameProject);
		}
//		XMIParse xmi = new XMIParse(nameProject, segment);
//		xmi.createXMI();
		return Response.status(200).entity(result).build();
	}

	public static void main(String args[]){
		OctaveService service = new OctaveService();
		try{
//		    service.carregaKDMProject("matlab2tikz-master", "/home/thiago/octave/matlab2tikz-master");
		    service.getTreemap();
		}catch(Exception cause){
			cause.printStackTrace();
		}
	}
	
	public void carregaKDMProject(String nameProject, String path) throws Exception{
		KDMSegment segment = OctaveController.loadSegment(new File(path),nameProject);
		KDMParseJson kdmJsonParse = new KDMParseJson(nameProject, segment);
		JsonObject kdmJson = kdmJsonParse.createJSON();
		KDMOctaveDAO dao = new KDMOctaveDAO();
		dao.save(kdmJson,nameProject);
		System.out.println("OK");
	}
	
	public void getTreemap(){
		KDMOctaveDAO dao = new KDMOctaveDAO();
		dao.getTreemapView();
	}
	
	@GET
	@Path("/create/view")
	public Response createView() throws Exception {
		KDMOctaveDAO d = new KDMOctaveDAO();
		d.criaView();
		return Response.status(200).entity("Views criadas").build();
	}
	
	@GET
	@Path("/treemap")
	public Response treeMap() throws Exception {
		KDMOctaveDAO dao = new KDMOctaveDAO();
		return Response.status(200).entity(dao.getTreemapView()).build();
	}

	@GET
	@Path("/relations")
	public Response relations() {
		KDMOctaveDAO dao = new KDMOctaveDAO();
		return Response.status(200).entity(dao.getRelationsView()).build();
	}
}