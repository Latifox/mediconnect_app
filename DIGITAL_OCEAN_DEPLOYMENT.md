# MediConnect: DigitalOcean Deployment Guide

This guide provides step-by-step instructions for deploying the MediConnect telemedicine platform on DigitalOcean.

## Prerequisites

- A [DigitalOcean](https://www.digitalocean.com/) account
- Your MediConnect repository forked or cloned to your GitHub account
- Basic familiarity with cloud deployment concepts

## Method 1: One-Click Deployment (Recommended)

### 1. Prepare Your Repository

Ensure your repository has the following files:
- `docker-compose.yml` at the root level
- `Dockerfile` in both `mediconnect-backend` and `mediconnect-frontend` directories
- `.dockerignore` files in both project directories

### 2. Deploy Using the Button

1. Click the "Deploy to DigitalOcean" button from the README
2. Log in to your DigitalOcean account
3. The App Platform wizard will open with your repo pre-selected
4. Follow the on-screen instructions

### 3. Configure Resources

In the DigitalOcean App Platform interface:

1. **Select Resource Type**:
   - Choose "Web Service" for both frontend and backend components
   - Choose "Database" for PostgreSQL

2. **Configure Backend**:
   - Source Directory: `mediconnect-backend`
   - HTTP Port: `8080`
   - Resource Size: Basic (1GB RAM / 1 vCPU) or higher
   - Add environment variables:
     ```
     SPRING_PROFILES_ACTIVE=prod
     SPRING_JPA_HIBERNATE_DDL_AUTO=update
     JWT_SECRET=[your-secure-secret]
     JWT_EXPIRATION=86400000
     SPRING_DATASOURCE_URL=${db.DATABASE_URL}
     SPRING_DATASOURCE_USERNAME=${db.USERNAME}
     SPRING_DATASOURCE_PASSWORD=${db.PASSWORD}
     ```

3. **Configure Frontend**:
   - Source Directory: `mediconnect-frontend`
   - Build Command: `npm install && npm run build`
   - Run Command: `npm start -- --port $PORT`
   - Resource Size: Basic (1GB RAM / 1 vCPU)
   - Add environment variables:
     ```
     API_URL=https://${APP_URL}/api
     NODE_ENV=production
     ```

4. **Configure PostgreSQL Database**:
   - Choose "Development Database" (or higher for production)
   - Connect it to your backend service

### 4. Review and Launch

1. Review all settings
2. Choose your preferred region
3. Set your App name (e.g., "mediconnect")
4. Click "Launch App"

Your app will now be deployed. The deployment process typically takes 5-10 minutes.

## Method 2: Manual Deployment with Droplets

For more control over your deployment, you can use DigitalOcean Droplets (virtual machines).

### 1. Create a Droplet

1. Log in to your DigitalOcean account
2. Click "Create" > "Droplets"
3. Choose an image: Ubuntu 22.04 LTS
4. Choose a plan: Standard (2 GB RAM / 1 vCPU) or higher
5. Choose a datacenter region close to your users
6. Add your SSH keys
7. Click "Create Droplet"

### 2. Set Up Your Droplet

Connect to your droplet via SSH:
```bash
ssh root@your-droplet-ip
```

Update your system and install Docker:
```bash
# Update system packages
apt update && apt upgrade -y

# Install required packages
apt install -y apt-transport-https ca-certificates curl software-properties-common

# Add Docker's official GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Set up the Docker repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker and Docker Compose
apt update
apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

### 3. Clone Your Repository

```bash
# Clone the repository
git clone https://github.com/latifox/mediconnect_app.git
cd mediconnect_app
```

### 4. Configure Environment Variables

Create a `.env` file for sensitive information:
```bash
cat > .env << EOL
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your-secure-password
POSTGRES_DB=mediconnect
JWT_SECRET=your-secure-jwt-secret
JWT_EXPIRATION=86400000
EOL
```

### 5. Deploy with Docker Compose

```bash
# Start the application
docker compose up -d
```

### 6. Set Up Nginx as a Reverse Proxy

```bash
# Install Nginx
apt install -y nginx

# Create Nginx configuration
cat > /etc/nginx/sites-available/mediconnect << EOL
server {
    listen 80;
    server_name your-domain-or-ip;

    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_cache_bypass \$http_upgrade;
    }

    location / {
        proxy_pass http://localhost:19000/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_cache_bypass \$http_upgrade;
    }
}
EOL

# Enable the site
ln -s /etc/nginx/sites-available/mediconnect /etc/nginx/sites-enabled/

# Test and restart Nginx
nginx -t
systemctl restart nginx
```

### 7. Set Up SSL with Let's Encrypt

If you have a domain name pointing to your server:

```bash
# Install Certbot
apt install -y certbot python3-certbot-nginx

# Get an SSL certificate
certbot --nginx -d your-domain.com
```

## Advanced Configuration

### Setting Up CI/CD with GitHub Actions

Create a file at `.github/workflows/deploy.yml`:

```yaml
name: Deploy to DigitalOcean

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install doctl
        uses: digitalocean/action-doctl@v2
        with:
          token: ${{ secrets.DIGITALOCEAN_ACCESS_TOKEN }}
          
      - name: Build container image
        run: |
          docker build -t registry.digitalocean.com/mediconnect/backend:$(echo $GITHUB_SHA | head -c7) ./mediconnect-backend
          docker build -t registry.digitalocean.com/mediconnect/frontend:$(echo $GITHUB_SHA | head -c7) ./mediconnect-frontend
      
      - name: Push image to DigitalOcean Container Registry
        run: |
          doctl registry login
          docker push registry.digitalocean.com/mediconnect/backend:$(echo $GITHUB_SHA | head -c7)
          docker push registry.digitalocean.com/mediconnect/frontend:$(echo $GITHUB_SHA | head -c7)
          
      - name: Update deployment file
        run: |
          sed -i 's|registry.digitalocean.com/mediconnect/backend:.*|registry.digitalocean.com/mediconnect/backend:'$(echo $GITHUB_SHA | head -c7)'|' $GITHUB_WORKSPACE/deployment.yml
          sed -i 's|registry.digitalocean.com/mediconnect/frontend:.*|registry.digitalocean.com/mediconnect/frontend:'$(echo $GITHUB_SHA | head -c7)'|' $GITHUB_WORKSPACE/deployment.yml
          
      - name: Deploy to DigitalOcean Kubernetes
        run: |
          doctl kubernetes cluster kubeconfig save mediconnect-cluster
          kubectl apply -f $GITHUB_WORKSPACE/deployment.yml
```

### Monitoring and Logging

1. **Install DigitalOcean Monitoring Agent**:
```bash
curl -sSL https://agent.digitalocean.com/install.sh | sh
```

2. **Set Up Logging with Loki**:
```bash
# Add to your docker-compose.yml
services:
  loki:
    image: grafana/loki:2.5.0
    ports:
      - "3100:3100"
    volumes:
      - loki-data:/loki
    command: -config.file=/etc/loki/local-config.yaml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    depends_on:
      - loki
    volumes:
      - grafana-data:/var/lib/grafana

volumes:
  loki-data:
  grafana-data:
```

## Troubleshooting

### Database Connection Issues
- Check PostgreSQL logs: `docker logs mediconnect-postgres`
- Verify database credentials in environment variables
- Check network connectivity between containers

### Backend API Issues
- Check backend logs: `docker logs mediconnect-backend`
- Verify JWT secret configuration
- Check for Java exceptions in the logs

### Frontend Connection Issues
- Verify API_URL environment variable
- Check for CORS issues in browser developer tools
- Ensure backend is running and accessible

## Scaling Your Application

As your user base grows, consider:

1. **Database Scaling**:
   - Upgrade to a larger managed PostgreSQL instance
   - Implement read replicas for high-traffic scenarios

2. **Backend Scaling**:
   - Use DigitalOcean Load Balancers
   - Implement horizontal scaling with multiple backend instances
   - Consider Kubernetes for orchestration

3. **Frontend Scaling**:
   - Use a CDN for static assets
   - Build and serve native mobile apps from app stores

## Security Recommendations

1. **Enable DigitalOcean Firewalls**
2. **Implement rate limiting**
3. **Use strong, unique passwords**
4. **Regularly update all dependencies**
5. **Enable backups for your database and droplets**
6. **Implement monitoring and alerting**

## Cost Optimization

1. **Right-size your resources** based on actual usage
2. **Use reserved instances** for long-term deployments
3. **Scale down development/staging environments** when not in use
4. **Monitor bandwidth usage** to avoid overage charges
5. **Use object storage** for large files instead of database storage 