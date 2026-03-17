pipeline {
    agent any

    parameters {
        booleanParam(name: 'RUN_UI_SMOKE', defaultValue: false, description: 'Run headless Selenium smoke UI tests in Jenkins')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME = 'wealth-api-demo'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('UI Smoke Tests') {
            when {
                expression { params.RUN_UI_SMOKE }
            }
            steps {
                sh 'mvn -B -pl api -am -DskipTests install'
                sh 'mvn -B -pl ui-tests verify -Dit.test=SmokeUiIT -Dui.headless=true'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'ui-tests/target/cucumber-reports/**,ui-tests/target/allure-results/**,ui-tests/target/site/allure-maven-plugin/**'
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'api/target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed. Check test report and logs.'
        }
    }
}
