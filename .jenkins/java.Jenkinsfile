pipeline {
    agent any

    parameters {
        string(name: 'COVERAGE_MIN', defaultValue: '80',
               description: 'Minimum line coverage (%). Build fails below this.')
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        GRADLE_USER_HOME = '/var/jenkins_home/.gradle'
        GRADLE_OPTS      = '-Dorg.gradle.daemon=false'
    }

    stages {
        stage('Static analysis') {
            steps {
                dir('java') {
                    // catchError: mark the build failed but still run the test stage
                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        sh './gradlew checkstyleMain checkstyleTest pmdMain spotbugsMain --continue --console=plain'
                    }
                }
            }
            post {
                always {
                    // PMD and SpotBugs are blocking: Gradle already failed the stage on any violation.
                    recordIssues(
                        enabledForFailure: true,
                        tools: [
                            pmdParser(id: 'pmd', name: 'PMD', pattern: 'java/build/reports/pmd/main.xml'),
                            spotBugs(id: 'spotbugs', name: 'SpotBugs', pattern: 'java/build/reports/spotbugs/main.xml')
                        ]
                    )
                    // Checkstyle: 'error' rules already failed the stage. If we get here with
                    // findings left, they are 'warning' rules, so mark the build UNSTABLE (yellow).
                    recordIssues(
                        enabledForFailure: true,
                        tools: [checkStyle(id: 'checkstyle', name: 'Checkstyle',
                                           pattern: 'java/build/reports/checkstyle/*.xml')],
                        qualityGates: [[threshold: 1, type: 'TOTAL', criticality: 'UNSTABLE']]
                    )
                }
            }
        }

        stage('Tests & coverage') {
            steps {
                dir('java') {
                    sh """
                        ./gradlew test integrationTest jacocoTestReport jacocoTestCoverageVerification \
                            -PcoverageMin=${params.COVERAGE_MIN} --continue --console=plain
                    """
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'java/build/test-results/**/*.xml'
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: 'java/build/reports/jacoco/test/jacocoTestReport.xml']],
                        sourceDirectories: [[path: 'java/src/main/java']],
                        // Soft target: below 90% marks the build UNSTABLE. The hard limit is COVERAGE_MIN.
                        qualityGates: [[threshold: 90.0, metric: 'LINE', baseline: 'PROJECT', criticality: 'UNSTABLE']]
                    )
                    publishHTML(target: [
                        reportName: 'Coverage',
                        reportDir: 'java/build/reports/jacoco/test/html',
                        reportFiles: 'index.html',
                        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true
                    ])
                    publishHTML(target: [
                        reportName: 'Unit tests',
                        reportDir: 'java/build/reports/tests/test',
                        reportFiles: 'index.html',
                        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true
                    ])
                    publishHTML(target: [
                        reportName: 'Integration tests',
                        reportDir: 'java/build/reports/tests/integrationTest',
                        reportFiles: 'index.html',
                        keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true
                    ])
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'java/build/reports/**', allowEmptyArchive: true
        }
    }
}
