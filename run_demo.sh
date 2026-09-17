#!/usr/bin/env bash

set -e

GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}================================================================${NC}"
echo -e "${CYAN}  Kafka-Based Microservices Ordering System (Spring Boot + React) ${NC}"
echo -e "${CYAN}================================================================${NC}"

echo ""
echo -e "${GREEN}Select an action to perform:${NC}"
echo "  1) Deploy Entire Fleet with Docker Compose (Kafka, SR, Gateway, Microservices, React)"
echo "  2) Run Maven Unit Tests (All Microservices)"
echo "  3) Run React Frontend in Local Dev Mode (Vite on :3000)"
echo "  4) Stop All Running Docker Containers"
echo "  5) Exit"
echo ""

read -p "Enter choice [1-5]: " choice

case $choice in
    1)
        echo -e "${YELLOW}[*] Building and starting all Docker containers...${NC}"
        docker compose up --build -d
        echo ""
        echo -e "${GREEN}[✓] Full Fleet Deployed Successfully!${NC}"
        echo -e "  🌐 React Frontend:      ${CYAN}http://localhost:3000${NC}"
        echo -e "  🚪 API Gateway:         ${CYAN}http://localhost:8080${NC}"
        echo -e "  📊 Kafka UI Console:    ${CYAN}http://localhost:8090${NC}"
        echo -e "  📜 Schema Registry:     ${CYAN}http://localhost:8081${NC}"
        ;;
    2)
        echo -e "${YELLOW}[*] Running Maven test suite across all modules...${NC}"
        mvn clean test
        ;;
    3)
        echo -e "${YELLOW}[*] Starting Vite development server...${NC}"
        npm --prefix frontend run dev
        ;;
    4)
        echo -e "${YELLOW}[*] Stopping Docker containers...${NC}"
        docker compose down
        echo -e "${GREEN}[✓] Docker containers stopped.${NC}"
        ;;
    5)
        echo "Exiting."
        exit 0
        ;;
    *)
        echo -e "${RED}[!] Invalid choice.${NC}"
        exit 1
        ;;
esac
