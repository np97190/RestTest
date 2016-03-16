package org.ioora;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Path("/iooraops")
public class EntryClass {

	@GET
	@Path("/{repo}/{project}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response scanbpmn(@PathParam("repo") String repository, @PathParam("project") String project)
			throws FileNotFoundException, IOException, ParseException, WrongRepositoryStateException,
			InvalidConfigurationException, DetachedHeadException, InvalidRemoteException, CanceledException,
			RefNotFoundException, RefNotAdvertisedException, NoHeadException, TransportException, GitAPIException {

		Object obj = null;
		JSONParser parser = new JSONParser();
		obj = parser.parse(new FileReader("/TestGitIoora/ServerProperties.json"));
		JSONObject jsonObject = (JSONObject) obj;

		String server = (String) jsonObject.get("Server");
		String port = (String) jsonObject.get("Port");
		String location = (String) jsonObject.get("Location");

		Launcher launcher = new Launcher();
		launcher.setBaseDirName(location);
		launcher.setProjectName(project);
		launcher.setRepoName(repository);
		launcher.setSignal(0);

		String url = "ssh://admin@" + server + ":" + port + "/" + repository;
		launcher.setUrl(url);
		JSONObject result = launcher.mainMethod();
		return Response.status(200).entity(result.toString()).build();
	}

	@GET
	@Path("/{ou}/{repo}/{project}/{process}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBpmnFile(@PathParam("ou") String ou, @PathParam("repo") String repo,
			@PathParam("project") String project, @PathParam("process") String process)
					throws WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException,
					InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException,
					NoHeadException, TransportException, IOException, GitAPIException, ParseException {

		Object obj = null;
		JSONParser parser = new JSONParser();
		obj = parser.parse(new FileReader("/TestGitIoora/ServerProperties.json"));
		JSONObject jsonObject = (JSONObject) obj;

		String server = (String) jsonObject.get("Server");
		String port = (String) jsonObject.get("Port");
		String location = (String) jsonObject.get("Location");

		Launcher launcher = new Launcher();
		launcher.setBaseDirName(location);
		launcher.setOuName(ou);
		launcher.setProcessName(process);
		launcher.setProjectName(project);
		launcher.setRepoName(repo);
		launcher.setSignal(1);
		String url = "ssh://admin@" + server + ":" + port + "/" + repo;
		launcher.setUrl(url);

		JSONObject result = launcher.mainMethod();
		return Response.status(200).entity(result.toString()).build();

	}

}
