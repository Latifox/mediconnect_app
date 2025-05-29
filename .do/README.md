# DigitalOcean Deployment Configuration

This directory contains configuration files for deploying MediConnect to DigitalOcean App Platform.

## Important: Before Using the "Deploy to DigitalOcean" Button

Before using the one-click deploy button in the main README, make sure to:

1. **Update Repository Information**:
   - In `.do/app.yaml`, replace `yourusername/mediconnect` with your actual GitHub username and repository name
   - Example: `username/mediconnect` → `latifox/mediconnect_app`

2. **Update the Deploy Button URL**:
   - In `README.md`, update the "Deploy to DigitalOcean" button URL with your actual repository URL
   - Example: `https://github.com/yourusername/mediconnect` → `https://github.com/latifox/mediconnect_app`

3. **Customize Environment Variables**:
   - Review and adjust environment variables in `app.yaml` as needed
   - Consider changing database names and other settings

## Additional Configuration

You can customize the `app.yaml` file to:

- Change the deployment region
- Adjust instance sizes for production deployments
- Add custom domains
- Configure environment-specific settings

For more information, see the [DigitalOcean App Platform documentation](https://docs.digitalocean.com/products/app-platform/). 