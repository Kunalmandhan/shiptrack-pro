#!/usr/bin/env bash
# ==========================================
# ShipTrack Pro — Automated Deployment Script
# ==========================================

set -e

echo "🚀 Starting ShipTrack Pro Deployment Automation..."

MODE=${1:-"docker"}

if [ "$MODE" = "docker" ]; then
    echo "📦 Building & Launching Docker Compose Stack..."
    docker-compose down --remove-orphans || true
    docker-compose up -d --build
    echo "✅ Docker Compose stack launched! Access UI at http://localhost:3000"

elif [ "$MODE" = "k8s" ]; then
    echo "☸️ Applying Kubernetes Manifests..."
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/configmap-secrets.yaml
    kubectl apply -f k8s/microservices.yaml
    kubectl apply -f k8s/ingress.yaml
    echo "✅ Kubernetes manifests applied successfully to namespace shiptrack-pro!"

else
    echo "❌ Unknown deployment mode: $MODE"
    echo "Usage: ./deploy.sh [docker|k8s]"
    exit 1
fi
