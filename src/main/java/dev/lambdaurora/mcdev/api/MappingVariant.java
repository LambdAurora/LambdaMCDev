package dev.lambdaurora.mcdev.api;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

public interface MappingVariant extends Named {
	Attribute<MappingVariant> ATTRIBUTE = Attribute.of("net.minecraft.mappings", MappingVariant.class);

	String INTERMEDIARY = "intermediary";

	String MOJMAP = "mojmap";
}
