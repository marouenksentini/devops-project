pipeline {
    agent any

    environment {
    DOCKER_HUB_USER = "marouen6"
    IMAGE_NAME = "${DOCKER_HUB_USER}/devops-project"
    RENDER_DEPLOY_HOOK = credentials('render-deploy-hook')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Code checked out from GitHub"
            }
        }

        stage('Build JAR') {
            steps {
                dir('backend') {
                    bat 'mvn clean package -DskipTests'
                }
                echo "JAR built successfully"
            }
        }

        stage('Test') {
            steps {
                dir('backend') {
                    bat 'mvn test'
                }
                echo "Tests passed"
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${IMAGE_NAME}:latest", "./backend")
                }
                echo "Docker image built: ${IMAGE_NAME}:latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        docker.image("${IMAGE_NAME}:latest").push()
                    }
                }
                echo "Pushed to Docker Hub"
            }
        }

        stage('Trigger Render Deploy') {
            steps {
                bat 'curl -f -X POST "%RENDER_DEPLOY_HOOK%"'
                echo "Render redeploy triggered - it will pull ${IMAGE_NAME}:latest"
            }
        }

        stage('Local sanity check (optional)') {
            steps {
                bat 'docker-compose down'
                bat 'docker-compose up -d --build'
                bat 'ping -n 11 127.0.0.1 > nul'
                bat 'curl -f http://localhost:8080/api/health'
            }
        }
    }

    post {
        always {
            echo "Pipeline completed - Build #${BUILD_NUMBER}"
            deleteDir()
        }
        success {
            echo "SUCCESS! Image pushed: ${IMAGE_NAME}:latest"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}