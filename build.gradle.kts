plugins {
	id("dev.yumi.gradle.licenser") version "2.1.+"
	`java-gradle-plugin`

	kotlin("jvm") version "2.0.0"

	`maven-publish`
}

group = "dev.lambdaurora"
version = "1.5.3"
val javaVersion = 21

repositories {
	mavenCentral()
	maven {
		name = "Fabric"
		url = uri("https://maven.fabricmc.net/")
	}
}

gradlePlugin {
	website = "https://github.com/LambdAurora/LambdaMCDev"
	vcsUrl = "https://github.com/LambdAurora/LambdaMCDev"

	plugins {
		create("lambdamcdev") {
			id = "dev.lambdaurora.mcdev"
			displayName = "Lambda MC Dev"
			implementationClass = "dev.lambdaurora.mcdev.LambdaMcDevGradlePlugin"
		}
	}
}

dependencies {
	api(libs.jetbrains.annotations)
	api(libs.yumi.commons.core)
	api(libs.accesswidener)
	implementation(libs.gradle.loom)
	implementation(libs.mappingio)
	implementation(libs.gson)
	// Use JUnit Jupiter for testing.
	testImplementation(platform(libs.junit.bom))
	testImplementation(libs.junit.jupiter)
	testRuntimeOnly(libs.junit.launcher)
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaVersion))
	}

	withSourcesJar()
	withJavadocJar()

	testResultsDir.set(layout.buildDirectory.dir("junit-xml"))
}

kotlin {
	// Require explicit visibility/type definitions for public types, among other things
	explicitApi()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.release.set(javaVersion)
}

tasks.withType<Javadoc>().configureEach {
	options {
		this as StandardJavadocDocletOptions

		addStringOption("Xdoclint:all,-missing", "-quiet")
	}
}

tasks.jar {
	val archivesName = base.archivesName.get()

	from("LICENSE") {
		rename { "${it}_${archivesName}" }
	}
}

license {
	rule(file("codeformat/HEADER"))
	exclude("scenarios/**")
}

tasks.withType<Test>().configureEach {
	// Using JUnitPlatform for running tests
	useJUnitPlatform()

	testLogging {
		events("passed")
	}
}

publishing {
	repositories {
		mavenLocal()

		val pluginMaven = System.getenv("LMCD_MAVEN")
		if (pluginMaven != null) {
			maven {
				name = "LambdaMCDevMaven"
				url = uri(pluginMaven)
				credentials {
					username = (project.findProperty("gpr.user") as? String) ?: System.getenv("MAVEN_USERNAME")
					password = (project.findProperty("gpr.key") as? String) ?: System.getenv("MAVEN_PASSWORD")
				}
			}
		}
	}
}
