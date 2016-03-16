package org.ioora;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;

import com.jcraft.jsch.Session;

public class Staging {

	@SuppressWarnings("resource")
	public int cloneOrPull(String baseDir, String repoName, String url)
			throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException,
			InvalidRemoteException, CanceledException, RefNotFoundException, RefNotAdvertisedException, NoHeadException,
			TransportException, GitAPIException {

		/*
		 * SSH Authentication.
		 */

		SshSessionFactory.setInstance(new JschConfigSessionFactory() {

			@Override
			protected void configure(Host host, Session session) {

				session.setPassword("admin");
				session.setConfig("StrictHostKeyChecking", "no");
			}
		});

		File repoLocation = new File(baseDir + "/" + repoName);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setWorkTree(repoLocation).build();
		Git git = new Git(repository);

		boolean isAlreadyCloned = true;
		if (!repoLocation.exists()) {

			repoLocation.mkdirs();
			isAlreadyCloned = false;
		}

		if (isAlreadyCloned) {
			
				
			System.out.println("Repo already cloned");
			
			// Perform fetch and return true if fetch is successful.
			PullCommand pull = git.pull();
			PullResult pullResult = pull.call();
			MergeResult mergeResult = pullResult.getMergeResult();

			System.out.println("FetchStatus: " + mergeResult.getMergeStatus().toString());

			boolean mergeresult = mergeResult.getMergeStatus().isSuccessful(); // true
																				// or
																				// false
			if (mergeresult) {

				return 1; // fetch passed with repoa lready cloned.
			} else {

				return 0; // fetch failed with repo already cloed
			}
		} else {

			// Perform clone and return true if clone is successful.
			System.out.println("Repo not cloned");
			ListBranchCommand branchList = Git.cloneRepository().setDirectory(repoLocation).setURI(url).call()
					.branchList();
			if (branchList.toString() != null) {

				return 2;
			}

			return 0;
		}

	}

	public List<File> countBpmn(File dir, String[] extension) {
		System.out.println("Counting files in project: "+ dir.getAbsolutePath());
		List<File> files = (List<File>) FileUtils.listFiles(dir, extension, true);
		return files;
	}

	public boolean prepareBpmn(String baseLocation, String ou, String repo, String project, String processName,
			int signale) throws IOException {

		System.out.println("Preparing bpmn files");
		
		File sourcebpmn = new File("/ioora/apache-tomcat-8.0.32/webapps/IooraRestAPI02/WEB-INF/Samples/Format.bpmn2");
		File destinationbpmn = new File(
				"/ioora/apache-tomcat-8.0.32/webapps/IooraRestAPI02/WEB-INF/Samples/" + processName + ".bpmn2");
		File sourcewid = new File(
				"/ioora/apache-tomcat-8.0.32/webapps/IooraRestAPI02/WEB-INF/Samples/WorkDefinitions.wid");

		FileUtils.copyFile(sourcebpmn, destinationbpmn);
		Path path = Paths.get(destinationbpmn.toString());
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll("ScriptedProject.Something", project + "." + processName);
		Files.write(path, content.getBytes(charset));

		if (destinationbpmn.exists() && !destinationbpmn.isDirectory()) {

			File finalLocationbpmn = new File(baseLocation + "/" + repo + "/" + project + "/src/main/resources/"
					+ ou.toLowerCase() + "/" + project.toLowerCase() + "/" + processName + ".bpmn2");
			if (signale == 0) {
				FileUtils.moveFile(destinationbpmn, finalLocationbpmn);
				System.out.println("Files copied heading to commit cnd push");
				return true;
			} else {

				FileUtils.moveFile(destinationbpmn, finalLocationbpmn);
				File widlocation = new File(baseLocation + "/" + repo + "/" + project + "/" + "/src/main/resources/"
						+ ou.toLowerCase() + "/" + project.toLowerCase() + "/WorkDefinitions.wid");
				FileUtils.copyFile(sourcewid, widlocation);
				System.out.println("Files copied heading to commit cnd push");
				return true;
			}
		}

		return false;

	}

	@SuppressWarnings("resource")
	public boolean commitAndPush(String baseLocation, String repo)
			throws IOException, NoFilepatternException, GitAPIException {
		
		File cmdExecutionPath = new File(baseLocation + "/" + repo);
		
		System.out.println("inside commit and push. executing commands in " + cmdExecutionPath.getAbsolutePath().toString());
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setWorkTree(cmdExecutionPath).build();
		Git git = new Git(repository);

		// git add .

		AddCommand ac = git.add();
		ac.addFilepattern(".");
		ac.call();

		// git commit -a -m "message"

		CommitCommand commit = git.commit();
		commit.setCommitter("ioora", "iooraadmin@cgi.com").setMessage("new bpmn file added.");
		commit.call();

		System.out.println("completed commit ");
		SshSessionFactory.setInstance(new JschConfigSessionFactory() {

			@Override
			protected void configure(Host host, Session session) {

				session.setPassword("admin");
				session.setConfig("StrictHostKeyChecking", "no");
			}
		});

		// git push origin

		System.out.println("starting push");
		
		PushCommand push = git.push();
		push.setForce(true);
		push.setPushAll();
		push.setRemote("origin");
		Iterable<PushResult> results = push.call();

		String message = null;
		for (PushResult result : results) {

			message = result.getMessages();
			System.out.println("Push message" + result.getMessages());
		}
		
		if (message != null && message.trim().length() > 0) {

			return false;
		}

		
		return true;

	}

}
