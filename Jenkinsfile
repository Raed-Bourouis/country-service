pipeline {
    agent any
    tools {
        maven 'mvn'
    }
    stages {
        stage('Checkout code')
        {
            steps {
                git branch: 'main', url: 'https://github.com/Raed-Bourouis/country-service.git'
            }
        }
        stage('Compile, test code, package in war file and store in maven repo')
        {
            steps {
                sh 'mvn clean install'
            }
            post{
                success{
                    junit allowEmptyResults: true,
                    testResults: '**/target/surfire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis')
        {
            steps{
                withSonarQubeEnv(installationName:'MySonarQubeServer', credentialsId:'country-service'){
                    sh 'mvn sonar:sonar -Dsonar.projectKey=country-service -Dsonar.projectName=country-service'
                }
            }
        }
    }
}