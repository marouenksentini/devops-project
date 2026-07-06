# 🚀 Showcase du Projet DevOps

Une application web full-stack comprenant des flux CI/CD automatisés, des environnements conteneurisés et un suivi des métriques en temps réel.

**🔗 Site Web Live :** [https://marouenksentini.github.io/devops-project/](https://marouenksentini.github.io/devops-project/)

---

## 🛠️ Présentation du Système

Ce projet met en œuvre un cycle de vie d'infrastructure entièrement automatisé :

- **Frontend :** Interface statique HTML/CSS/JS hébergée localement via Nginx ou distribuée en production sur **GitHub Pages**.
- **Backend :** Application Java Spring Boot connectée à une base de données persistante **SQLite** pour le suivi des compteurs de clics et de visiteurs.
- **Persistance des Données :** Les compteurs sont stockés dans un fichier SQLite situé à l'emplacement `/app/data/app.db` à l'intérieur du conteneur backend, monté sur le volume nommé `devops-project-sqlite-data` pour survivre aux redémarrages.

---

## 📂 Structure du Projet

```
DevOps_Project/
├── backend/                       # Application Java Spring Boot
│   ├── src/main/java/...          # Contrôleurs, services & logique SQLite
│   ├── src/main/resources/        # Profils de configuration
│   ├── pom.xml                    # Gestion des dépendances Maven
│   └── Dockerfile                 # Emballage de l'image multi-stages
├── frontend/                      # Fichiers sources de l'interface
│   └── index.html                 # Tableau de bord de l'application
├── .github/workflows/             # Automatisation GitHub Actions
│   └── deploy-pages.yml           # Pipeline de déploiement du frontend statique
├── docker-compose.yml             # Configuration principale de production
├── docker-compose.local.yml       # Profil d'orchestration pour les tests locaux
├── nginx.conf                     # Fichier de configuration Nginx
└── Jenkinsfile                    # Cycle de vie du pipeline backend automatique
```

---

## 🏗️ Pipeline Core Backend (Jenkins)

Voici l'enchaînement visuel des étapes exécutées automatiquement par Jenkins à chaque commit :

```
 ┌──────────┐     ┌───────────┐     ┌────────────┐     ┌──────────────┐
 │ Checkout │ ──> │ Build JAR │ ──> │ Unit Tests │ ──> │ Docker Build │
 └──────────┘     └───────────┘     └────────────┘     └──────────────┘
                                                              │
 ┌──────────────┐     ┌─────────────┐     ┌──────────────┐    │
 │ Render Hook  │ <── │ Docker Push │ <── │  Image Tag   | <──┘
 └──────────────┘     └─────────────┘     └──────────────┘
```

---

## 🚀 Exécution & Workflows d'Automatisation

### 1. Sandbox de Test Local Isolé

Pour tester l'application sur votre machine sans entrer en conflit de ports avec vos outils de production (`8080`/`80`), utilisez ce profil dédié :

```bash
# Lancer les conteneurs locaux isolés sur les ports 9090 (Backend) et 9000 (Frontend)
docker-compose -f docker-compose.yml -f docker-compose.local.yml -p devops-project-local up -d --build

# Vérifier la réponse du backend
curl http://localhost:9090/api/hello

# Arrêter et supprimer l'infrastructure de test local
docker-compose -p devops-project-local down
```

### 2. Automatisation et Déploiement Cloud

- **Flux Frontend :** Entièrement pris en charge par **GitHub Actions** qui déploie automatiquement les fichiers statiques du dossier `frontend/` vers GitHub Pages lors d'un push sur `main`.
- **Flux Backend :** GitHub notifie Jenkins via un webhook (`http://YOUR_JENKINS_URL:8080/github-webhook/`). Après avoir compilé et poussé l'image sur Docker Hub (`marouen6/devops-project:latest`), Jenkins déclenche un webhook de déploiement sécurisé sur **Render** pour mettre à jour l'application instantanément et sans coupure.

---

## 🔧 Configuration Jenkins

### Prérequis Plugins

Installer ces plugins (Manage Jenkins → Plugins) :

- Docker Pipeline
- Docker Commons
- GitHub Integration
- Pipeline
- Credentials Binding

### Credentials

Ajouter ces credentials (Manage Jenkins → Credentials) :

| ID  | Type | Description |
| --- | --- | --- |
| `docker-hub-username` | Secret text | Votre username Docker Hub |
| `docker-hub-credentials` | Username/Password | Login Docker Hub pour push |
| `render-deploy-hook` | Secret text | URL du deploy hook Render |

### Webhook GitHub

1. Dans votre repo GitHub : **Settings → Webhooks → Add webhook**
2. Payload URL : `http://YOUR_JENKINS_URL:8080/github-webhook/`
3. Content type : `application/json`
4. Events : `push`
5. Active : ✅

---

## 🌍 Déploiement Cloud

### GitHub Pages (Frontend)

1. Dans `frontend/index.html`, remplacer `YOUR-APP-NAME.onrender.com` par votre URL Render réelle
2. GitHub repo : **Settings → Pages → Source: GitHub Actions**
3. Le workflow `.github/workflows/deploy-pages.yml` publie automatiquement le dossier `frontend/` sur chaque push vers `main`
4. URL : `https://marouenksentini.github.io/devops-project/`

### Render (Backend)

1. Aller sur [render.com](https://render.com) → New → **Web Service**
2. Connecter votre repo GitHub
3. Root directory : `backend`
4. Render détecte automatiquement le `Dockerfile`
5. Alternative : pointer vers l'image Docker Hub `marouen6/devops-project:latest`

> **Note :** Le tier gratuit de Render a un filesystem éphémère. Les compteurs SQLite se réinitialisent à chaque redéploiement sauf si vous ajoutez un disque persistant (payant) ou utilisez Render Postgres.

---

## 📝 API Endpoints

| Endpoint | Méthode | Description |
| --- | --- | --- |
| `/api/hello` | GET | Message de bienvenue |
| `/api/health` | GET | Vérification de santé |
| `/api/time` | GET | Heure actuelle en Tunisie (dd/MM/yyyy HH:mm:ss) |
| `/api/click` | GET / POST | Lire / incrémenter le compteur de clics |
| `/api/click/{buttonId}` | GET / POST | Lire / incrémenter un compteur spécifique |
| `/api/visit` | GET | Enregistrer une visite et retourner le total |
| `/api/visit/count` | GET | Lire le total des visiteurs |
| `/api/visitors/total` | GET | Total et visiteurs uniques |
| `/api/stats` | GET | Toutes les statistiques du site |

---

## 🎯 Objectif du Projet

Automatiser le build, test, containerisation et déploiement d'une application full-stack en utilisant les outils DevOps modernes et les meilleures pratiques.

---

**Développeur :** marouenksentini  
**Docker Hub :** marouen6  
**Repository :** devops-project

Made with ❤️ in Tunisia 🇹🇳
