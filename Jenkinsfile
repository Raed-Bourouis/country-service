pipeline {
    agent any

    tools {
        maven 'mvn'
    }

    stages {
        stage('Checkout code') {
            steps {
                git branch: 'main', url: 'https://github.com/Raed-Bourouis/country-service.git'
            }
        }

        stage('Compile, test code, package in war file and store in maven repo') {
            steps {
                sh 'mvn clean install'
            }
            post {
                success {
                    junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Deploy using Ansible playbook') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'ansible-vault', variable: 'VAULT_PASS')]) {
                        sh '''
                        ansible-playbook playbook.yml --vault-password-file < (echo "$VAULT_PASS")
                    '''
                    }
                }
            }
        }
    }

    post {
        always {
            cleansWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check console output for details.'
        }
    }
}
