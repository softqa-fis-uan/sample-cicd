pipeline {
    agent {
        docker { image 'ghcr.io/astral-sh/uv:trixie-slim' }
    }

    parameters {
        string(name: 'COVERAGE_MIN', defaultValue: '80',
               description: 'Minimum total coverage (%). Build fails below this.')
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        HOME                  = "${WORKSPACE}"
        UV_CACHE_DIR          = "${WORKSPACE}/.uv-cache"
        UV_PYTHON_INSTALL_DIR = "${WORKSPACE}/.uv-python"
        UV_LINK_MODE          = 'copy'
    }

    stages {
        stage('Setup') {
            steps {
                dir('python') {
                    sh 'mkdir -p build/reports'
                    sh 'uv sync --frozen'   // also downloads Python 3.15 on first run
                }
            }
        }

        stage('Checks') {
            parallel {
                stage('Static analysis') {
                    steps {
                        dir('python') {
                            sh 'uv run ruff check app tests --output-format=sarif > build/reports/ruff.sarif || true'
                            sh 'uv run mypy app --no-pretty > build/reports/mypy.txt || true'
                            sh 'uv run ruff format --check app tests'
                        }
                    }
                    post {
                        always {
                            recordIssues(
                                enabledForFailure: true,
                                sourceDirectories: [[path: 'python']],
                                tools: [
                                    sarif(id: 'ruff', name: 'Ruff', pattern: 'python/build/reports/ruff.sarif'),
                                    myPy(pattern: 'python/build/reports/mypy.txt')
                                ],
                                qualityGates: [[threshold: 1, type: 'TOTAL', criticality: 'FAILURE']]
                            )
                        }
                    }
                }

                stage('Tests & coverage') {
                    steps {
                        dir('python') {
                            sh """
                                uv run pytest \
                                    --junitxml=build/reports/junit.xml \
                                    --cov-report=xml:build/reports/coverage.xml \
                                    --cov-fail-under=${params.COVERAGE_MIN}
                            """
                        }
                    }
                    post {
                        always {
                            junit allowEmptyResults: true, testResults: 'python/build/reports/junit.xml'
                            recordCoverage(
                                sourceDirectories: [[path: 'python']],
                                tools: [[parser: 'COBERTURA', pattern: 'python/build/reports/coverage.xml']]
                            )
                            publishHTML(target: [
                                reportName: 'Coverage',
                                reportDir: 'python/build/reports/coverage',
                                reportFiles: 'index.html',
                                keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true
                            ])
                            publishHTML(target: [
                                reportName: 'Test report',
                                reportDir: 'python/build/reports',
                                reportFiles: 'tests.html',
                                keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true
                            ])
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'python/build/reports/**', allowEmptyArchive: true
        }
    }
}