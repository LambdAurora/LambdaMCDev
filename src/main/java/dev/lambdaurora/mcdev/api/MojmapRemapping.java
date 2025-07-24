package dev.lambdaurora.mcdev.api;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;

public interface MojmapRemapping {
	SourceSet sourceSet();

	Configuration mappingsConfiguration();

	void setJarArtifact(Object artifact);

	void setSourcesArtifact(Object artifact);
}
