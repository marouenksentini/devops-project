# DevOps_Project

Full-stack demo: Java Spring Boot backend (with a persistent SQLite click/visitor counter and
a live Tunis-time endpoint), a plain HTML/CSS/JS frontend served by nginx, Docker Compose to
run both together, and a Jenkins pipeline that builds the image and pushes it to Docker Hub.

## Project structure

```
DevOps_Project/
├── backend/
│   ├── src/main/java/com/example/demo/
│   │   ├── DemoApplication.java
│   │   ├── controller/HelloController.java
│   │   └── service/StatsService.java
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   └── index.html
├── docker-compose.yml
├── docker-compose.local.yml
├── nginx.conf
├── Jenkinsfile
├── .github/workflows/deploy-pages.yml
└── README.md
```

## API endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/hello` | GET | Simple greeting, used for the "Check Backend" button |
| `/api/health` | GET | Plain health check |
| `/api/time` | GET | Current date/time in `Africa/Tunis` |
| `/api/click` | GET / POST | Read / increment the SQLite click counter |
| `/api/visit` | GET | Records a visit (by IP) and returns the running total |
| `/api/visit/count` | GET | Reads the visitor total without recording a new visit |

Click and visitor counts are stored in a SQLite file at `/app/data/app.db` inside the backend
container. In `docker-compose.yml` that path is mounted on a named volume
(`devops-project-sqlite-data`) so the counts survive container restarts and redeploys.

## Run locally without conflicting with Jenkins

Jenkins is installed on your own machine, and the pipeline's last stage also runs
`docker-compose up`. If you also run `docker-compose up` manually for testing, both
will fight over the same container names (`devops-project-backend`, `devops-project-frontend`) and the
same host ports (`8080`, `80`) — whichever runs second will error out or stop the
other's containers.

To avoid that, use `docker-compose.local.yml` for your own manual testing. It gives
your local run different container names, different host ports, and a different
Compose "project name", so it's completely isolated from anything Jenkins does:

```bash
# start your own local copy, on ports 9090 (backend) and 9000 (frontend)
docker-compose -f docker-compose.yml -f docker-compose.local.yml -p devops-project-local up -d --build

# test it
curl http://localhost:9090/api/hello
# open http://localhost:9000 in your browser

# stop it when done
docker-compose -p devops-project-local down
```

Meanwhile Jenkins keeps using the plain `docker-compose.yml` (ports 8080/80,
default project name) in its own pipeline run — the two never touch the same
container names or ports, so you can leave your local copy running while Jenkins
builds, or trigger a Jenkins build while testing locally, with no conflicts either way.

## GitHub → Jenkins webhook

1. In your GitHub repo: **Settings → Webhooks → Add webhook**
2. Payload URL: `http://YOUR_JENKINS_URL:8080/github-webhook/`
3. Content type: `application/json`
4. Events: just `push`
5. Active: checked

In Jenkins, create a **Pipeline** job:
- Check **"GitHub hook trigger for GITScm polling"**
- Pipeline script from SCM → Git → your repo URL, script path `Jenkinsfile`

## Jenkins prerequisites

Install these plugins (Manage Jenkins → Plugins):
- Docker Pipeline
- Docker Commons
- GitHub Integration
- Pipeline
- Credentials Binding

Add these credentials (Manage Jenkins → Credentials):
- `docker-hub-username` — Secret text, your Docker Hub username
- `docker-hub-credentials` — Username/password type, your Docker Hub login (used to push images)

The Jenkins host itself needs Docker and docker-compose installed and the `jenkins` user added
to the `docker` group so pipeline stages can run `docker build` / `docker-compose up`.

## Pipeline stages

`Checkout → Build JAR → Test → Build Docker Image → Tag latest → Push to Docker Hub → Deploy with Docker Compose → Verify Deployment`

Each successful push publishes `YOUR_DOCKERHUB_USER/devops-project:<build-number>` and
`:latest` to Docker Hub.

## Deploy for free: GitHub Pages (frontend) + Render (backend)

GitHub Pages only serves static files — it cannot run Java, Docker, or SQLite. So the
frontend and backend deploy to two different places, and the frontend calls the backend
over the internet instead of through nginx.

### 1. Backend on Render

1. Push this repo to GitHub if you haven't already.
2. Go to [render.com](https://render.com) → New → **Web Service** → connect your repo.
3. Root directory: `backend`
4. Render will detect the `Dockerfile` automatically, or set:
   - Build command: `docker build -t app .`
   - Start command: `docker run -p 8080:8080 app`
5. Under the service's **Settings → Deploy Hook**, copy the deploy hook URL — you'll need it
   for Jenkins in step 3.
6. Once deployed, note your service URL, e.g. `https://devops-project-xxxx.onrender.com`.

Alternative: instead of building from source, point Render at your Docker Hub image
(`maroune6/devops-project:latest`) under **New → Web Service → Deploy an existing image**.
That way Jenkins builds and pushes the image, and Render just pulls it — no source build on
Render's side.

Note: Render's free tier has an ephemeral filesystem, so the SQLite click/visitor counts
reset on every redeploy unless you attach a persistent disk (paid tier) or a Render Postgres
add-on instead of SQLite.

### 2. Frontend on GitHub Pages

1. In `frontend/index.html`, replace `YOUR-APP-NAME.onrender.com` with your real Render URL
   from step 1.
2. In your GitHub repo: **Settings → Pages → Build and deployment → Source: GitHub Actions**.
3. The included workflow `.github/workflows/deploy-pages.yml` publishes the `frontend/`
   folder automatically on every push to `main`. Your site will be live at
   `https://YOUR_USERNAME.github.io/YOUR_REPO/`.

### 3. Jenkins → Render handoff

Add one more Jenkins credential (Manage Jenkins → Credentials):
- Kind: Secret text → ID: `render-deploy-hook` → value: the deploy hook URL from Render step 1.5

The updated `Jenkinsfile` now ends with a **Trigger Render Deploy** stage that calls this
hook after pushing the new image to Docker Hub, so Render redeploys the latest image
automatically — no manual step needed after the first setup.

### CORS note

The backend's `HelloController` already has `@CrossOrigin(origins = "*")`, so it will accept
requests from your `github.io` domain without extra configuration.

## Other free backend hosts (if you don't want Render)

| Platform | Notes |
|---|---|
| **Railway** | Deploy from GitHub, generous free tier, detects the Dockerfile |
| **Fly.io** | `fly launch` in `backend/`, then `fly deploy`, has free persistent volumes |
| **Koyeb** | Connect GitHub repo, auto-deploys on push |

Netlify is frontend/static-only like GitHub Pages, so it's a straight swap for the Pages step
above if you'd rather use it — just connect the repo, set the publish directory to
`frontend`, and skip the GitHub Actions workflow.

