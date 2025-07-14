# Frontend Deployment Guide

This guide explains how to configure the frontend for deployment to S3 or other static hosting services.

## GitHub Repository Variables

The frontend build process uses GitHub repository variables to configure the backend URL. This approach allows you to change the configuration without modifying code.

### Setting up Repository Variables

1. Navigate to your GitHub repository
2. Go to **Settings** → **Secrets and variables** → **Actions**
3. Click on the **Variables** tab
4. Add the following repository variables:

| Variable Name | Description | Example Value |
|---------------|-------------|---------------|
| `BACKEND_URL` | The backend API server URL | `https://api.robotwars.com` |

### Example Configuration

For different deployment environments, you might use:

#### Development/Testing
```
BACKEND_URL=http://localhost:8080
```

#### Staging
```
BACKEND_URL=https://staging-api.robotwars.com
```

#### Production
```
BACKEND_URL=https://api.robotwars.com
```

## Build Process

When you push code to the main branch:

1. GitHub Actions runs the frontend CI workflow
2. The workflow sets `REACT_APP_BACKEND_URL` to the value of `BACKEND_URL` repository variable
3. The build process runs `npm run generate-config` which creates `public/config.js`
4. The React app is built with the configuration baked in
5. The build artifacts are ready for deployment to S3 or other static hosting

## Deployment to S3

After the build completes:

1. Upload the contents of the `build/` directory to your S3 bucket
2. Configure S3 for static website hosting
3. Set up CloudFront (optional) for better performance and custom domain
4. The frontend will automatically use the configured backend URL

## Troubleshooting

### Configuration Not Applied

If the frontend is not using the correct backend URL:

1. Check that the `BACKEND_URL` repository variable is set correctly
2. Verify that the CI workflow completed successfully
3. Check the build logs for the configuration output
4. Ensure `public/config.js` is present in the build artifacts

### Development vs Production

- **Development**: Uses `http://localhost:8080` by default
- **Production**: Uses the `BACKEND_URL` repository variable value
- **Fallback**: If no configuration is found, defaults to `http://localhost:8080`

## Security Considerations

- Repository variables are visible to repository collaborators
- Don't put sensitive information in repository variables
- Use HTTPS URLs for production backend endpoints
- Consider using environment-specific repositories for different deployment stages
