/*
 * Copyright 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.lambdaurora.mcdev;

import dev.lambdaurora.mcdev.api.LambdaMcDevGradleExtension;
import dev.lambdaurora.mcdev.ext.LambdaMcDevGradleExtensionImpl;
import dev.lambdaurora.mcdev.task.CheckLanguageResourcesTask;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.jetbrains.annotations.NotNull;

public class LambdaMcDevGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(@NotNull Project project) {
		var javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
		var loomExt = project.getExtensions().findByName("loom");
		project.getExtensions().create(
				TypeOf.typeOf(LambdaMcDevGradleExtension.class),
				"lambdamcdev",
				LambdaMcDevGradleExtensionImpl.class,
				project,
				javaExt,
				loomExt
		);

		project.getPlugins().withType(JavaBasePlugin.class).configureEach(plugin -> {
			var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

			sourceSets.all(sourceSet -> {
				project.getTasks().register(
						getLanguageTaskName("check", sourceSet), CheckLanguageResourcesTask.class
				).configure(CheckLanguageResourcesTask.configureDefault(project, sourceSet.getResources(), sourceSet.getName()));
			});
		});

		var globalCheck = this.registerGroupedTask(project, "check", task -> {
			task.dependsOn(project.getTasks().withType(CheckLanguageResourcesTask.class));

			task.setDescription("Checks whether language resource files in every source set are valid");
			task.setGroup("verification");
		});

		project.getPlugins().withType(LifecycleBasePlugin.class).configureEach(_ -> {
			project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(task -> {
				task.dependsOn(globalCheck);
			});
		});
	}

	/**
	 * {@return the task name of a given action for a given source set}
	 *
	 * @param action the task action
	 * @param sourceSet the source set the task is applied to
	 */
	public static String getLanguageTaskName(String action, SourceSet sourceSet) {
		if (sourceSet.getName().equals("main")) {
			return action + "LanguageResources" + "Main";
		} else {
			return sourceSet.getTaskName(action + "LanguageResources", null);
		}
	}

	private TaskProvider<Task> registerGroupedTask(Project project, String action, Action<Task> consumer) {
		var task = project.getTasks().register(action + "LanguageResources");
		task.configure(consumer);
		return task;
	}
}
