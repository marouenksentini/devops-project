pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = "maroune6"
        IMAGE_NAME = "${DOCKER_HUB_USER}/devops-project"
        IMAGE_TAG = "${BUILD_NUMBER}"
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
                    sh 'mvn clean package -DskipTests'
                }
                echo "JAR built successfully"
            }
        }

        stage('Test') {
            steps {
                dir('backend') {
                    sh 'mvn test'
                }
                echo "Tests passed"
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}", "./backend")
                }
                echo "Docker image built: ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Tag Latest') {
            steps {
                sh "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
                echo "Tagged as latest"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                        docker.image("${IMAGE_NAME}:latest").push()
                    }
                }
                echo "Pushed to Docker Hub"
            }
        }

        stage('Trigger Render Deploy') {
            steps {
                sh '''
                    curl -f -X POST "$RENDER_DEPLOY_HOOK"
                '''
                echo "Render redeploy triggered - it will pull ${IMAGE_NAME}:latest"
            }
        }

        stage('Local sanity check (optional)') {
            steps {
                sh '''
                    docker-compose down || true
                    docker-compose up -d --build
                    sleep 10
                    curl -f http://localhost:8080/api/health || exit 1
                '''
            }
        }
    }

    post {
        always {
            echo "Pipeline completed - Build #${BUILD_NUMBER}"
            cleanWs()
        }
        success {
            echo "SUCCESS! Image pushed: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed!"
        }
    }
}
