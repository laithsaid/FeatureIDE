package de.ovgu.featureide.ui.variantimport.migration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.core.io.FeatureModelWriterIFileWrapper;
import de.ovgu.featureide.fm.core.io.xml.XmlFeatureModelWriter;

/**
 * This class implements methods that might be useful in Migrating a Set of
 * Projects into a FeatureIDE Project. Currently this is only implemented for
 * the FeatureHouse composer in {@link VariantsToFeatureHouseSPLMigrator}.
 * 
 * @author Konstantin Tonscheidt
 * 
 */
public class SPLMigrationUtils
{
	/**
	 * Copies all folders and files from {@code source} to {@code destination}.
	 * 
	 * @param source
	 * @param destination
	 * @throws CoreException
	 */
	public static void recursiveCopyFiles(IContainer source, IContainer destination)
			throws CoreException
	{
		final IResource[] members = source.members();
		for (int i = 0; i < members.length; i++)
		{
			final IResource member = members[i];
			final IPath currentPath = new Path(member.getName());

			if (member instanceof IContainer)
			{
				final IFolder subFolder = destination.getFolder(currentPath);
				if (!subFolder.exists())
				{
					member.copy(subFolder.getFullPath(), true, null);
					recursiveCopyFiles((IContainer) member, subFolder);
				}
			} else
				if (member instanceof IFile)
				{
					final IFile copyFile = destination.getFile(currentPath);
					if (!copyFile.exists())
						member.copy(copyFile.getFullPath(), true, null);
				} else
					assert false : "Only expected  Files and Containers to copy";
		}
	}

	/**
	 * creates a new Folder in {@code project} at the given {@code path}.
	 * 
	 * @param project
	 *            the project, the folder is going to be created in.
	 * @param path
	 *            a path relative to the project root.
	 */
	public static void createFolderInProject(IProject project, IPath path)
	{
		if (path == null || path.isEmpty())
			return;

		IFolder newFolder = project.getFolder(path);
		if (newFolder.exists())
		{
			assert false : "Trying to create an already existing folder: " + path;
			System.out.println("Folder " + path + " already exists");
			return;
		}
		try
		{
			newFolder.create(true, true, null);
			project.refreshLocal(IProject.DEPTH_INFINITE, null);
			System.out.println("Creation of Folder " + path + " successful");
		} catch (CoreException e)
		{
			System.out.println("Creation of Folder " + path + " lead to CoreException:"
					+ e.getMessage());
			e.printStackTrace();
		}
		if (!newFolder.exists())
			System.out.println("Folder " + path + " does not exist after creation");

	}

	/**
	 * convenience method for creating folders from a path saved in a
	 * {@link String}.
	 * 
	 * @param project
	 *            the project, the folder is going to be created in.
	 * @param path
	 *            a path relative to the project root.
	 * 
	 * @see {@link #createFolderInProject(IProject, IPath)}
	 */
	public static void createFolderInProject(IProject project, String path)
	{
		IPath newPath = new Path(path);
		System.out.println("Creating Folder at " + path + " in project " + project.getName());
		createFolderInProject(project, newPath);
	}

	/**
	 * Tries to create a new Project in the Workspace and returns it.
	 * 
	 * @param projectName
	 * @return the new {@link IProject} if successful, null if not.
	 */
	public static IProject createProject(String projectName)
	{
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (newProject.exists())
			throw new IllegalArgumentException("Cannot create project " + projectName
					+ " because it already exists.");

		try
		{
			newProject.create(null);
		} catch (CoreException e)
		{
			e.printStackTrace();
			return null;
		}

		return newProject;

	}

	/**
	 * Creates a {@code projectName}.config file containing {@code projectName}
	 * in the projects config folder.
	 * 
	 * @param project
	 * @param configPath
	 * @param projectName
	 * @throws CoreException
	 * @throws UnsupportedEncodingException
	 */
	public static void createConfigFile(IProject project, String configPath, String projectName)
			throws CoreException, UnsupportedEncodingException
	{
		final IFolder configFolder = project.getFolder(configPath);
		final IFile configFile = configFolder.getFile(projectName + ".config");
		InputStream defaultContent = new ByteArrayInputStream(projectName.getBytes("UTF-8"));

		configFile.create(defaultContent, true, null);

	}

	/**
	 * Writes the {@code featureModel} to the default location (
	 * {@code /model.xml}) in {@code featureProject}
	 * 
	 * @param featureProject
	 * @param featureModel
	 */
	public static void writeFeatureModelToDefaultFile(IProject featureProject,
			FeatureModel featureModel)
	{
		FeatureModelWriterIFileWrapper fmWriter = new FeatureModelWriterIFileWrapper(
				new XmlFeatureModelWriter(featureModel));
		IFile featureModelFile = featureProject.getFile("model.xml");

		assert featureModelFile.exists() : "model.xml does not exist";

		try
		{
			fmWriter.writeToFile(featureModelFile);
		} catch (CoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
