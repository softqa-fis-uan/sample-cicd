import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask

plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.27.0"
    checkstyle
    pmd
    jacoco
    id("com.github.spotbugs") version "6.5.13"
}

group = "com.example.greetings"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs the API integration tests."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTest)
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/../greetings.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.path)
    apiPackage.set("com.example.greetings.presentation.api")
    modelPackage.set("com.example.greetings.presentation.api.model")
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot4" to "true",
            "useJackson3" to "true",
            "useSpringBuiltInValidation" to "true",
            "useTags" to "true",
            "openApiNullable" to "false",
        )
    )
	globalProperties.set(mapOf("apis" to "", "models" to ""))
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/java"))
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

// ---------- Static analysis ----------

val generatedPath = "${File.separator}build${File.separator}generated${File.separator}"

checkstyle {
    toolVersion = "12.3.1"
    configFile = file("config/checkstyle/checkstyle.xml")
    maxErrors = 0                   // any 'error' rule fails the build
    maxWarnings = Int.MAX_VALUE     // 'warning' rules are only reported
}

pmd {
    toolVersion = "7.22.0"
    ruleSets = listOf()             // drop Gradle's default ruleset
    ruleSetFiles = files("config/pmd/ruleset.xml")
    isConsoleOutput = true
}

spotbugs {
    toolVersion.set("4.9.8")
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.MEDIUM)
    excludeFilter.set(file("config/spotbugs/exclude.xml"))
}

tasks.withType<Checkstyle>().configureEach {
    exclude { it.file.path.contains(generatedPath) }     // skip generated code
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<Pmd>().configureEach {
    exclude { it.file.path.contains(generatedPath) }
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<SpotBugsTask>().configureEach {
    reports {
        create("html") { required.set(true) }
        create("xml") { required.set(true) }
    }
}

// Rules written for production code would only add noise on the tests.
tasks.named("pmdTest") { enabled = false }
tasks.named("spotbugsTest") { enabled = false }

// ---------- Coverage ----------

jacoco {
    toolVersion = "0.8.15"
}

// Minimum line coverage as a ratio. Override with: ./gradlew check -PcoverageMin=0.85
val coverageMin = providers.gradleProperty("coverageMin")
    .map { it.toBigDecimal().movePointLeft(2) }   // 80 -> 0.80
    .orElse("0.80".toBigDecimal())

val coverageClassDirs = files(
    sourceSets.main.get().output.classesDirs.files.map {
        fileTree(it) {
            exclude(
                "com/example/greetings/presentation/api/**",   // generated
                "com/example/greetings/demo/**",               // course demo code
                "**/*Application.class",                       // Spring Boot main class
            )
        }
    }
)

tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest)
    executionData(tasks.test.get(), integrationTest.get())
    classDirectories.setFrom(coverageClassDirs)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test, integrationTest)
    executionData(tasks.test.get(), integrationTest.get())
    classDirectories.setFrom(coverageClassDirs)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = coverageMin.get()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}