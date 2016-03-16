package org.ioora;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Launcher {

	private String url, baseDirName, ouName, repoName, projectName, processName;
	private int signal;

	public Launcher() {

	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setBaseDirName(String baseDirName) {
		this.baseDirName = baseDirName;
	}

	public void setOuName(String ouName) {
		this.ouName = ouName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}

	Staging stage = new Staging();

	// Signal to create bpmn.
	@SuppressWarnings("unchecked")
	public JSONObject mainMethod() throws WrongRepositoryStateException, InvalidConfigurationException,
			DetachedHeadException, InvalidRemoteException, CanceledException, RefNotFoundException,
			RefNotAdvertisedException, NoHeadException, TransportException, IOException, GitAPIException {

		File dir = new File(this.baseDirName + this.repoName);
		String[] extension = new String[] { "bpmn2" };
		
		JSONObject jsonResult = new JSONObject();
		jsonResult.put("Status", "Failed");

		if (this.signal == 0) {

			// do not create bpmn file.

		
			
			int cloneorpull = stage.cloneOrPull(this.baseDirName, this.repoName, this.url);
			if (cloneorpull != 0) {

				List<File> files = stage.countBpmn(dir, extension);
				JSONArray bpmnfiles = new JSONArray();
				for (File file : files) {

					bpmnfiles.add(file.getName());
				}

				jsonResult.put("Status", "Success");
				jsonResult.put("Files", bpmnfiles);
				return jsonResult;
			}

			return jsonResult;
		} else {

			int cloneorpull = stage.cloneOrPull(this.baseDirName, this.repoName, this.url);
			boolean preparebpmn = false;
			if (cloneorpull == 2) {
				preparebpmn = stage.prepareBpmn(this.baseDirName, this.ouName, this.repoName, this.projectName,
						this.processName, 1);
			}
			if (cloneorpull == 1) {
				preparebpmn = stage.prepareBpmn(this.baseDirName, this.ouName, this.repoName, this.projectName,
						this.processName, 0);
			}
			if (preparebpmn) {

				boolean commitandpush = stage.commitAndPush(this.baseDirName, this.repoName);
				if (commitandpush) {

					List<File> files = stage.countBpmn(dir, extension);
					JSONArray bpmnfiles = new JSONArray();
					for (File file : files) {

						bpmnfiles.add(file.getName());
					}
					jsonResult.put("Status", "Success");
					jsonResult.put("Files", bpmnfiles);
					return jsonResult;
				}
				jsonResult.put("Status", "Failed");
				return jsonResult;
			}

			return jsonResult;
		}

	}

}
